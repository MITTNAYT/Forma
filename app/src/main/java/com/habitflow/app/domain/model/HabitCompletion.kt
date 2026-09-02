package com.habitflow.app.domain.model

import com.habitflow.app.core.util.SyncStatus
import java.util.UUID

data class HabitCompletion(
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val date: String, // YYYY-MM-DD
    val completedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
