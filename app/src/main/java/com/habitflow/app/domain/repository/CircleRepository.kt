package com.habitflow.app.domain.repository

import com.habitflow.app.domain.model.Circle
import com.habitflow.app.domain.model.CircleCheckIn
import com.habitflow.app.domain.model.CircleMember
import com.habitflow.app.domain.model.CircleReaction
import com.habitflow.app.domain.model.CircleReactionType
import kotlinx.coroutines.flow.Flow

interface CircleRepository {
    suspend fun createCircle(name: String, description: String, themes: List<String>): Result<Circle>
    suspend fun joinCircle(inviteCode: String): Result<Circle>
    suspend fun leaveCircle(circleId: String): Result<Unit>
    fun observeUserCircles(): Flow<List<Circle>>
    fun observeCircleMembers(circleId: String): Flow<List<CircleMember>>
    fun observeCircleCheckIns(circleId: String): Flow<List<CircleCheckIn>>
    fun observeCircleReactions(circleId: String): Flow<List<CircleReaction>>
    suspend fun postCheckIn(circleId: String, habitName: String, note: String): Result<Unit>
    suspend fun sendReaction(circleId: String, recipientId: String, type: CircleReactionType): Result<Unit>
}
