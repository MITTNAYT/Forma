package com.habitflow.app.domain.model

data class Circle(
    val id: String,
    val name: String,
    val description: String,
    val inviteCode: String,
    val ownerId: String,
    val habitThemes: List<String>,
    val memberCount: Int,
    val createdAt: Long
)

data class CircleMember(
    val userId: String,
    val displayName: String,
    val photoUrl: String? = null,
    val role: CircleRole = CircleRole.MEMBER,
    val joinedAt: Long,
    val currentStreak: Int = 0,
    val todayCompletedCount: Int = 0
)

enum class CircleRole {
    OWNER,
    MEMBER
}

data class CircleCheckIn(
    val id: String,
    val circleId: String,
    val userId: String,
    val userName: String,
    val habitName: String,
    val timestamp: Long,
    val note: String = ""
)

data class CircleReaction(
    val id: String,
    val circleId: String,
    val senderId: String,
    val senderName: String,
    val recipientId: String,
    val type: CircleReactionType,
    val timestamp: Long
)

enum class CircleReactionType(val emoji: String, val label: String) {
    CALM("🍵", "Send Calm"),
    PRESENCE("🌿", "Send Presence"),
    ENERGY("⚡", "Send Energy"),
    CLAP("👏", "Celebrate")
}
