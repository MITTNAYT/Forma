package com.habitflow.app.data.repository

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.habitflow.app.data.local.dao.HabitCompletionDao
import com.habitflow.app.data.local.dao.HabitDao
import com.habitflow.app.data.local.dao.TimelineDao
import com.habitflow.app.data.local.entity.HabitCompletionEntity
import com.habitflow.app.data.local.entity.HabitEntity
import com.habitflow.app.data.local.entity.TimelineItemEntity
import com.habitflow.app.domain.repository.SyncRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val habitDao: HabitDao,
    private val timelineDao: TimelineDao,
    private val habitCompletionDao: HabitCompletionDao
) : SyncRepository {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncedAt = MutableStateFlow<Long?>(null)
    override val lastSyncedAt: StateFlow<Long?> = _lastSyncedAt.asStateFlow()

    override suspend fun syncAll(): Result<Unit> = withContext(Dispatchers.IO) {
        val user = auth?.currentUser ?: return@withContext Result.failure(IllegalStateException("No authenticated user to sync with."))
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firebase Firestore is not initialized."))

        _isSyncing.value = true
        try {
            val uid = user.uid

            // 1. Sync Habits
            val localHabits = habitDao.getAllHabitsList()
            val remoteHabitsSnap = db.collection("users").document(uid).collection("habits").get().await()
            val remoteHabitsMap = remoteHabitsSnap.documents.associateBy { it.id }

            // Upload or resolve newer local habits
            for (local in localHabits) {
                val remoteDoc = remoteHabitsMap[local.id]
                if (remoteDoc == null) {
                    val data = habitEntityToMap(local)
                    db.collection("users").document(uid).collection("habits").document(local.id)
                        .set(data, SetOptions.merge()).await()
                } else {
                    val remoteUpdatedAt = remoteDoc.getLong("updatedAt") ?: 0L
                    if (local.updatedAt >= remoteUpdatedAt) {
                        val data = habitEntityToMap(local)
                        db.collection("users").document(uid).collection("habits").document(local.id)
                            .set(data, SetOptions.merge()).await()
                    } else {
                        val updatedLocal = mapToHabitEntity(remoteDoc.id, remoteDoc.data ?: emptyMap())
                        habitDao.insertHabit(updatedLocal)
                    }
                }
            }

            // Download new remote habits not in local
            val localHabitIds = localHabits.map { it.id }.toSet()
            for ((docId, doc) in remoteHabitsMap) {
                if (docId !in localHabitIds) {
                    val entity = mapToHabitEntity(docId, doc.data ?: emptyMap())
                    habitDao.insertHabit(entity)
                }
            }

            // 2. Sync Timeline Items
            val localTasks = timelineDao.getAllTimelineItemsList()
            val remoteTasksSnap = db.collection("users").document(uid).collection("timeline").get().await()
            val remoteTasksMap = remoteTasksSnap.documents.associateBy { it.id }

            for (local in localTasks) {
                val remoteDoc = remoteTasksMap[local.id]
                if (remoteDoc == null) {
                    val data = timelineEntityToMap(local)
                    db.collection("users").document(uid).collection("timeline").document(local.id)
                        .set(data, SetOptions.merge()).await()
                } else {
                    val remoteUpdatedAt = remoteDoc.getLong("updatedAt") ?: 0L
                    if (local.updatedAt >= remoteUpdatedAt) {
                        val data = timelineEntityToMap(local)
                        db.collection("users").document(uid).collection("timeline").document(local.id)
                            .set(data, SetOptions.merge()).await()
                    } else {
                        val updatedLocal = mapToTimelineEntity(remoteDoc.id, remoteDoc.data ?: emptyMap())
                        timelineDao.insertTimelineItem(updatedLocal)
                    }
                }
            }

            val localTaskIds = localTasks.map { it.id }.toSet()
            for ((docId, doc) in remoteTasksMap) {
                if (docId !in localTaskIds) {
                    val entity = mapToTimelineEntity(docId, doc.data ?: emptyMap())
                    timelineDao.insertTimelineItem(entity)
                }
            }

            // 3. Sync Completions
            val localCompletions = habitCompletionDao.getAllCompletionsList()
            val remoteCompletionsSnap = db.collection("users").document(uid).collection("completions").get().await()
            val remoteCompletionsMap = remoteCompletionsSnap.documents.associateBy { it.id }

            for (local in localCompletions) {
                val docKey = "${local.habitId}_${local.date}"
                if (docKey !in remoteCompletionsMap) {
                    val data = mapOf(
                        "id" to local.id,
                        "habitId" to local.habitId,
                        "date" to local.date,
                        "completedAt" to local.completedAt,
                        "updatedAt" to local.updatedAt
                    )
                    db.collection("users").document(uid).collection("completions").document(docKey)
                        .set(data, SetOptions.merge()).await()
                }
            }

            val localCompletionKeys = localCompletions.map { "${it.habitId}_${it.date}" }.toSet()
            for ((docKey, doc) in remoteCompletionsMap) {
                if (docKey !in localCompletionKeys) {
                    val id = doc.getString("id") ?: "${doc.getString("habitId")}_${doc.getString("date")}"
                    val habitId = doc.getString("habitId") ?: ""
                    val date = doc.getString("date") ?: ""
                    val completedAt = doc.getLong("completedAt") ?: System.currentTimeMillis()
                    val updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                    if (habitId.isNotBlank() && date.isNotBlank()) {
                        habitCompletionDao.insertCompletion(
                            HabitCompletionEntity(
                                id = id,
                                habitId = habitId,
                                date = date,
                                completedAt = completedAt,
                                updatedAt = updatedAt,
                                syncStatus = "SYNCED"
                            )
                        )
                    }
                }
            }

            _lastSyncedAt.value = System.currentTimeMillis()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isSyncing.value = false
        }
    }

    override suspend fun uploadLocalToCloud(): Result<Unit> = syncAll()
    override suspend fun downloadCloudToLocal(): Result<Unit> = syncAll()

    private fun habitEntityToMap(h: HabitEntity): Map<String, Any?> = mapOf(
        "name" to h.name,
        "icon" to h.icon,
        "colorTag" to h.colorTag,
        "timeOfDay" to h.timeOfDay,
        "energyLevel" to h.energyLevel,
        "repeatDays" to h.repeatDays,
        "createdAt" to h.createdAt,
        "archived" to h.archived,
        "reminderTimeMinutes" to h.reminderTimeMinutes,
        "updatedAt" to h.updatedAt,
        "stackedAfterHabitId" to h.stackedAfterHabitId,
        "stackedCueText" to h.stackedCueText,
        "isWintering" to h.isWintering,
        "startDate" to h.startDate,
        "endDate" to h.endDate,
        "isIndefinite" to h.isIndefinite
    )

    private fun mapToHabitEntity(id: String, m: Map<String, Any?>): HabitEntity = HabitEntity(
        id = id,
        name = m["name"] as? String ?: "",
        icon = m["icon"] as? String ?: "spa",
        colorTag = m["colorTag"] as? String ?: "#4E6542",
        timeOfDay = m["timeOfDay"] as? String ?: "ANYTIME",
        energyLevel = m["energyLevel"] as? String ?: "MEDIUM",
        repeatDays = m["repeatDays"] as? String ?: "1,2,3,4,5,6,7",
        createdAt = (m["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        archived = m["archived"] as? Boolean ?: false,
        reminderTimeMinutes = (m["reminderTimeMinutes"] as? Number)?.toInt(),
        updatedAt = (m["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        syncStatus = "SYNCED",
        stackedAfterHabitId = m["stackedAfterHabitId"] as? String,
        stackedCueText = m["stackedCueText"] as? String,
        isWintering = m["isWintering"] as? Boolean ?: false,
        startDate = m["startDate"] as? String,
        endDate = m["endDate"] as? String,
        isIndefinite = m["isIndefinite"] as? Boolean ?: true
    )

    private fun timelineEntityToMap(t: TimelineItemEntity): Map<String, Any?> = mapOf(
        "title" to t.title,
        "date" to t.date,
        "startTime" to t.startTime,
        "endTime" to t.endTime,
        "icon" to t.icon,
        "colorTag" to t.colorTag,
        "notes" to t.notes,
        "subtasksRaw" to t.subtasksRaw,
        "isRecurring" to t.isRecurring,
        "repeatDays" to t.repeatDays,
        "reminderMinutesBefore" to t.reminderMinutesBefore,
        "completed" to t.completed,
        "updatedAt" to t.updatedAt
    )

    private fun mapToTimelineEntity(id: String, m: Map<String, Any?>): TimelineItemEntity = TimelineItemEntity(
        id = id,
        title = m["title"] as? String ?: "",
        date = m["date"] as? String ?: "",
        startTime = m["startTime"] as? String,
        endTime = m["endTime"] as? String,
        icon = m["icon"] as? String ?: "check",
        colorTag = m["colorTag"] as? String ?: "#4E6542",
        notes = m["notes"] as? String ?: "",
        subtasksRaw = m["subtasksRaw"] as? String ?: "",
        isRecurring = m["isRecurring"] as? Boolean ?: false,
        repeatDays = m["repeatDays"] as? String ?: "",
        reminderMinutesBefore = (m["reminderMinutesBefore"] as? Number)?.toInt(),
        completed = m["completed"] as? Boolean ?: false,
        updatedAt = (m["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        syncStatus = "SYNCED"
    )
}
