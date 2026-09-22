package com.forma.app.core.util

import android.content.Context
import com.forma.app.data.local.dao.DailyReflectionDao
import com.forma.app.data.local.dao.HabitCompletionDao
import com.forma.app.data.local.dao.HabitDao
import com.forma.app.data.local.dao.TimelineDao
import com.forma.app.data.local.entity.DailyReflectionEntity
import com.forma.app.data.local.entity.HabitCompletionEntity
import com.forma.app.data.local.entity.HabitEntity
import com.forma.app.data.local.entity.TimelineItemEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao,
    private val timelineDao: TimelineDao,
    private val dailyReflectionDao: DailyReflectionDao
) {

    suspend fun exportDataToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 2)
        root.put("timestamp", System.currentTimeMillis())
        root.put("appName", "Forma")

        // 1. Habits
        val habits = habitDao.getAllHabits().first()
        val habitsArray = JSONArray()
        for (h in habits) {
            val hObj = JSONObject().apply {
                put("id", h.id)
                put("name", h.name)
                put("icon", h.icon)
                put("colorTag", h.colorTag)
                put("timeOfDay", h.timeOfDay)
                put("energyLevel", h.energyLevel)
                put("repeatDays", h.repeatDays)
                put("createdAt", h.createdAt)
                put("archived", h.archived)
                put("reminderTimeMinutes", h.reminderTimeMinutes ?: -1)
                put("updatedAt", h.updatedAt)
                put("syncStatus", h.syncStatus)
                put("stackedAfterHabitId", h.stackedAfterHabitId ?: "")
                put("stackedCueText", h.stackedCueText ?: "")
                put("isWintering", h.isWintering)
            }
            habitsArray.put(hObj)
        }
        root.put("habits", habitsArray)

        // 2. Completions
        val completions = habitCompletionDao.getAllCompletions().first()
        val completionsArray = JSONArray()
        for (c in completions) {
            val cObj = JSONObject().apply {
                put("id", c.id)
                put("habitId", c.habitId)
                put("date", c.date)
                put("completedAt", c.completedAt)
                put("updatedAt", c.updatedAt)
                put("syncStatus", c.syncStatus)
            }
            completionsArray.put(cObj)
        }
        root.put("completions", completionsArray)

        // 3. Timeline Items
        val timelineItems = timelineDao.getAllTimelineItems().first()
        val timelineArray = JSONArray()
        for (t in timelineItems) {
            val tObj = JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("date", t.date)
                put("startTime", t.startTime ?: "")
                put("endTime", t.endTime ?: "")
                put("icon", t.icon)
                put("colorTag", t.colorTag)
                put("notes", t.notes)
                put("subtasksRaw", t.subtasksRaw)
                put("isRecurring", t.isRecurring)
                put("repeatDays", t.repeatDays)
                put("reminderMinutesBefore", t.reminderMinutesBefore ?: -1)
                put("completed", t.completed)
                put("updatedAt", t.updatedAt)
                put("syncStatus", t.syncStatus)
            }
            timelineArray.put(tObj)
        }
        root.put("timelineItems", timelineArray)

        // 4. Daily Reflections
        val reflections = dailyReflectionDao.getRecentReflections().first()
        val reflectionsArray = JSONArray()
        for (r in reflections) {
            val rObj = JSONObject().apply {
                put("date", r.date)
                put("keystoneIntentions", r.keystoneIntentions)
                put("gratitudeNote", r.gratitudeNote)
                put("mindfulnessScore", r.mindfulnessScore)
                put("energyLevel", r.energyLevel)
                put("isMorningCompleted", r.isMorningCompleted)
                put("isEveningCompleted", r.isEveningCompleted)
                put("updatedAt", r.updatedAt)
            }
            reflectionsArray.put(rObj)
        }
        root.put("reflections", reflectionsArray)

        root.toString(2)
    }

    suspend fun importDataFromJson(jsonStr: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonStr)
            var restoredItemsCount = 0

            // 1. Habits
            if (root.has("habits")) {
                val habitsArr = root.getJSONArray("habits")
                val habitList = mutableListOf<HabitEntity>()
                for (i in 0 until habitsArr.length()) {
                    val obj = habitsArr.getJSONObject(i)
                    val reminderMins = obj.optInt("reminderTimeMinutes", -1)
                    habitList.add(
                        HabitEntity(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            icon = obj.optString("icon", "leaf"),
                            colorTag = obj.optString("colorTag", "#485938"),
                            timeOfDay = obj.optString("timeOfDay", "ANYTIME"),
                            energyLevel = obj.optString("energyLevel", "MEDIUM"),
                            repeatDays = obj.optString("repeatDays", "1,2,3,4,5,6,7"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            archived = obj.optBoolean("archived", false),
                            reminderTimeMinutes = if (reminderMins >= 0) reminderMins else null,
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                            syncStatus = obj.optString("syncStatus", "SYNCED"),
                            stackedAfterHabitId = obj.optString("stackedAfterHabitId", "").takeIf { it.isNotBlank() },
                            stackedCueText = obj.optString("stackedCueText", "").takeIf { it.isNotBlank() },
                            isWintering = obj.optBoolean("isWintering", false)
                        )
                    )
                }
                habitDao.insertHabits(habitList)
                restoredItemsCount += habitList.size
            }

            // 2. Completions
            if (root.has("completions")) {
                val compArr = root.getJSONArray("completions")
                val compList = mutableListOf<HabitCompletionEntity>()
                for (i in 0 until compArr.length()) {
                    val obj = compArr.getJSONObject(i)
                    compList.add(
                        HabitCompletionEntity(
                            id = obj.getString("id"),
                            habitId = obj.getString("habitId"),
                            date = obj.getString("date"),
                            completedAt = obj.optLong("completedAt", System.currentTimeMillis()),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                            syncStatus = obj.optString("syncStatus", "SYNCED")
                        )
                    )
                }
                habitCompletionDao.insertCompletions(compList)
                restoredItemsCount += compList.size
            }

            // 3. Timeline Items
            if (root.has("timelineItems")) {
                val timeArr = root.getJSONArray("timelineItems")
                val timelineList = mutableListOf<TimelineItemEntity>()
                for (i in 0 until timeArr.length()) {
                    val obj = timeArr.getJSONObject(i)
                    val start = obj.optString("startTime", "")
                    val end = obj.optString("endTime", "")
                    val rem = obj.optInt("reminderMinutesBefore", -1)

                    timelineList.add(
                        TimelineItemEntity(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            date = obj.getString("date"),
                            startTime = if (start.isNotBlank()) start else null,
                            endTime = if (end.isNotBlank()) end else null,
                            icon = obj.optString("icon", "pin"),
                            colorTag = obj.optString("colorTag", "#485938"),
                            notes = obj.optString("notes", ""),
                            subtasksRaw = obj.optString("subtasksRaw", ""),
                            isRecurring = obj.optBoolean("isRecurring", false),
                            repeatDays = obj.optString("repeatDays", "1,2,3,4,5,6,7"),
                            reminderMinutesBefore = if (rem >= 0) rem else null,
                            completed = obj.optBoolean("completed", false),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                            syncStatus = obj.optString("syncStatus", "SYNCED")
                        )
                    )
                }
                timelineDao.insertTimelineItems(timelineList)
                restoredItemsCount += timelineList.size
            }

            // 4. Daily Reflections
            if (root.has("reflections")) {
                val refArr = root.getJSONArray("reflections")
                for (i in 0 until refArr.length()) {
                    val obj = refArr.getJSONObject(i)
                    dailyReflectionDao.upsertReflection(
                        DailyReflectionEntity(
                            date = obj.getString("date"),
                            keystoneIntentions = obj.optString("keystoneIntentions", ""),
                            gratitudeNote = obj.optString("gratitudeNote", ""),
                            mindfulnessScore = obj.optInt("mindfulnessScore", 5),
                            energyLevel = obj.optString("energyLevel", "MEDIUM"),
                            isMorningCompleted = obj.optBoolean("isMorningCompleted", false),
                            isEveningCompleted = obj.optBoolean("isEveningCompleted", false),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                    restoredItemsCount++
                }
            }

            Result.success(restoredItemsCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
