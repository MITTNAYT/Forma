package com.forma.app.domain.usecase.circle

import com.forma.app.domain.model.Circle
import com.forma.app.domain.model.CircleCheckIn
import com.forma.app.domain.model.CircleMember
import com.forma.app.domain.model.CircleReaction
import com.forma.app.domain.model.CircleReactionType
import com.forma.app.domain.repository.CircleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateCircleUseCase @Inject constructor(
    private val repository: CircleRepository
) {
    suspend operator fun invoke(name: String, description: String, themes: List<String>): Result<Circle> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Circle name cannot be blank."))
        }
        return repository.createCircle(name.trim(), description.trim(), themes)
    }
}

class JoinCircleUseCase @Inject constructor(
    private val repository: CircleRepository
) {
    suspend operator fun invoke(inviteCode: String): Result<Circle> {
        val sanitized = inviteCode.trim().uppercase()
        if (sanitized.length < 4) {
            return Result.failure(IllegalArgumentException("Invite code must be at least 4 characters."))
        }
        return repository.joinCircle(sanitized)
    }
}

class LeaveCircleUseCase @Inject constructor(
    private val repository: CircleRepository
) {
    suspend operator fun invoke(circleId: String): Result<Unit> {
        return repository.leaveCircle(circleId)
    }
}

class ObserveUserCirclesUseCase @Inject constructor(
    private val repository: CircleRepository
) {
    operator fun invoke(): Flow<List<Circle>> {
        return repository.observeUserCircles()
    }
}

class ObserveCircleMembersUseCase @Inject constructor(
    private val repository: CircleRepository
) {
    operator fun invoke(circleId: String): Flow<List<CircleMember>> {
        return repository.observeCircleMembers(circleId)
    }
}

class ObserveCircleCheckInsUseCase @Inject constructor(
    private val repository: CircleRepository
) {
    operator fun invoke(circleId: String): Flow<List<CircleCheckIn>> {
        return repository.observeCircleCheckIns(circleId)
    }
}

class ObserveCircleReactionsUseCase @Inject constructor(
    private val repository: CircleRepository
) {
    operator fun invoke(circleId: String): Flow<List<CircleReaction>> {
        return repository.observeCircleReactions(circleId)
    }
}

class PostCircleCheckInUseCase @Inject constructor(
    private val repository: CircleRepository
) {
    suspend operator fun invoke(circleId: String, habitName: String, note: String = ""): Result<Unit> {
        if (habitName.isBlank()) {
            return Result.failure(IllegalArgumentException("Habit name cannot be blank."))
        }
        return repository.postCheckIn(circleId, habitName.trim(), note.trim())
    }
}

class SendCircleReactionUseCase @Inject constructor(
    private val repository: CircleRepository
) {
    suspend operator fun invoke(circleId: String, recipientId: String, type: CircleReactionType): Result<Unit> {
        return repository.sendReaction(circleId, recipientId, type)
    }
}
