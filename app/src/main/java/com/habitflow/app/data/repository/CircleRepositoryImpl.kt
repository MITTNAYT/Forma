package com.habitflow.app.data.repository

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.habitflow.app.domain.model.Circle
import com.habitflow.app.domain.model.CircleCheckIn
import com.habitflow.app.domain.model.CircleMember
import com.habitflow.app.domain.model.CircleReaction
import com.habitflow.app.domain.model.CircleReactionType
import com.habitflow.app.domain.model.CircleRole
import com.habitflow.app.domain.repository.CircleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CircleRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CircleRepository {

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

    override suspend fun createCircle(
        name: String,
        description: String,
        themes: List<String>
    ): Result<Circle> = withContext(Dispatchers.IO) {
        val user = auth?.currentUser
            ?: return@withContext Result.failure(IllegalStateException("Must be signed in to create a circle."))
        val db = firestore
            ?: return@withContext Result.failure(IllegalStateException("Firestore is not available."))

        try {
            val circleId = UUID.randomUUID().toString()
            val inviteCode = circleId.take(6).uppercase()
            val now = System.currentTimeMillis()

            val circleData = mapOf(
                "id" to circleId,
                "name" to name,
                "description" to description,
                "inviteCode" to inviteCode,
                "ownerId" to user.uid,
                "habitThemes" to themes,
                "memberCount" to 1,
                "memberIds" to listOf(user.uid),
                "createdAt" to now
            )

            val memberData = mapOf(
                "userId" to user.uid,
                "displayName" to (user.displayName ?: user.email ?: "Sanctuary Seeker"),
                "photoUrl" to (user.photoUrl?.toString()),
                "role" to CircleRole.OWNER.name,
                "joinedAt" to now,
                "currentStreak" to 1,
                "todayCompletedCount" to 0
            )

            db.collection("circles").document(circleId).set(circleData).await()
            db.collection("circles").document(circleId)
                .collection("members").document(user.uid).set(memberData).await()

            val created = Circle(
                id = circleId,
                name = name,
                description = description,
                inviteCode = inviteCode,
                ownerId = user.uid,
                habitThemes = themes,
                memberCount = 1,
                createdAt = now
            )
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinCircle(inviteCode: String): Result<Circle> = withContext(Dispatchers.IO) {
        val user = auth?.currentUser
            ?: return@withContext Result.failure(IllegalStateException("Must be signed in to join a circle."))
        val db = firestore
            ?: return@withContext Result.failure(IllegalStateException("Firestore is not available."))

        try {
            val querySnap = db.collection("circles")
                .whereEqualTo("inviteCode", inviteCode)
                .limit(1)
                .get()
                .await()

            if (querySnap.isEmpty) {
                return@withContext Result.failure(IllegalArgumentException("No circle found with code '$inviteCode'."))
            }

            val doc = querySnap.documents.first()
            val circleId = doc.id
            val now = System.currentTimeMillis()

            val memberData = mapOf(
                "userId" to user.uid,
                "displayName" to (user.displayName ?: user.email ?: "Mindful Member"),
                "photoUrl" to (user.photoUrl?.toString()),
                "role" to CircleRole.MEMBER.name,
                "joinedAt" to now,
                "currentStreak" to 0,
                "todayCompletedCount" to 0
            )

            db.collection("circles").document(circleId)
                .collection("members").document(user.uid).set(memberData, SetOptions.merge()).await()

            // Append memberId to array and increment count
            val memberIds = (doc.get("memberIds") as? List<*>)?.filterIsInstance<String>()?.toMutableSet() ?: mutableSetOf()
            memberIds.add(user.uid)
            db.collection("circles").document(circleId)
                .update(
                    mapOf(
                        "memberIds" to memberIds.toList(),
                        "memberCount" to memberIds.size
                    )
                ).await()

            val themes = (doc.get("habitThemes") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            val circle = Circle(
                id = circleId,
                name = doc.getString("name") ?: "Shared Circle",
                description = doc.getString("description") ?: "",
                inviteCode = doc.getString("inviteCode") ?: inviteCode,
                ownerId = doc.getString("ownerId") ?: "",
                habitThemes = themes,
                memberCount = memberIds.size,
                createdAt = doc.getLong("createdAt") ?: now
            )
            Result.success(circle)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveCircle(circleId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = auth?.currentUser
            ?: return@withContext Result.failure(IllegalStateException("Must be signed in."))
        val db = firestore
            ?: return@withContext Result.failure(IllegalStateException("Firestore is not available."))

        try {
            db.collection("circles").document(circleId)
                .collection("members").document(user.uid).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeUserCircles(): Flow<List<Circle>> = callbackFlow {
        val user = auth?.currentUser
        val db = firestore

        if (user == null || db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("circles")
            .whereArrayContains("memberIds", user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val circles = snapshot?.documents?.mapNotNull { doc ->
                    val themes = (doc.get("habitThemes") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    Circle(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        inviteCode = doc.getString("inviteCode") ?: "",
                        ownerId = doc.getString("ownerId") ?: "",
                        habitThemes = themes,
                        memberCount = (doc.getLong("memberCount") ?: 1L).toInt(),
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } ?: emptyList()
                trySend(circles)
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    override fun observeCircleMembers(circleId: String): Flow<List<CircleMember>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("circles").document(circleId)
            .collection("members")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val members = snapshot?.documents?.mapNotNull { doc ->
                    CircleMember(
                        userId = doc.getString("userId") ?: doc.id,
                        displayName = doc.getString("displayName") ?: "Presence",
                        photoUrl = doc.getString("photoUrl"),
                        role = try { CircleRole.valueOf(doc.getString("role") ?: "MEMBER") } catch (_: Exception) { CircleRole.MEMBER },
                        joinedAt = doc.getLong("joinedAt") ?: System.currentTimeMillis(),
                        currentStreak = (doc.getLong("currentStreak") ?: 0L).toInt(),
                        todayCompletedCount = (doc.getLong("todayCompletedCount") ?: 0L).toInt()
                    )
                } ?: emptyList()
                trySend(members)
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    override fun observeCircleCheckIns(circleId: String): Flow<List<CircleCheckIn>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("circles").document(circleId)
            .collection("checkins")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(40)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val checkIns = snapshot?.documents?.mapNotNull { doc ->
                    CircleCheckIn(
                        id = doc.id,
                        circleId = circleId,
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "Member",
                        habitName = doc.getString("habitName") ?: "",
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        note = doc.getString("note") ?: ""
                    )
                } ?: emptyList()
                trySend(checkIns)
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    override fun observeCircleReactions(circleId: String): Flow<List<CircleReaction>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("circles").document(circleId)
            .collection("reactions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val reactions = snapshot?.documents?.mapNotNull { doc ->
                    val typeStr = doc.getString("type") ?: "CALM"
                    CircleReaction(
                        id = doc.id,
                        circleId = circleId,
                        senderId = doc.getString("senderId") ?: "",
                        senderName = doc.getString("senderName") ?: "Friend",
                        recipientId = doc.getString("recipientId") ?: "",
                        type = try { CircleReactionType.valueOf(typeStr) } catch (_: Exception) { CircleReactionType.CALM },
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    )
                } ?: emptyList()
                trySend(reactions)
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    override suspend fun postCheckIn(
        circleId: String,
        habitName: String,
        note: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = auth?.currentUser
            ?: return@withContext Result.failure(IllegalStateException("Must be signed in to post check-ins."))
        val db = firestore
            ?: return@withContext Result.failure(IllegalStateException("Firestore is not available."))

        try {
            val checkInId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val data = mapOf(
                "id" to checkInId,
                "circleId" to circleId,
                "userId" to user.uid,
                "userName" to (user.displayName ?: user.email ?: "Seeker"),
                "habitName" to habitName,
                "timestamp" to now,
                "note" to note
            )

            db.collection("circles").document(circleId)
                .collection("checkins").document(checkInId).set(data).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendReaction(
        circleId: String,
        recipientId: String,
        type: CircleReactionType
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = auth?.currentUser
            ?: return@withContext Result.failure(IllegalStateException("Must be signed in to react."))
        val db = firestore
            ?: return@withContext Result.failure(IllegalStateException("Firestore is not available."))

        try {
            val reactionId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val data = mapOf(
                "id" to reactionId,
                "circleId" to circleId,
                "senderId" to user.uid,
                "senderName" to (user.displayName ?: user.email ?: "Friend"),
                "recipientId" to recipientId,
                "type" to type.name,
                "timestamp" to now
            )

            db.collection("circles").document(circleId)
                .collection("reactions").document(reactionId).set(data).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
