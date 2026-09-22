package com.forma.app.domain.model

import androidx.compose.runtime.Immutable
import com.forma.app.core.util.SyncStatus
import java.util.UUID

@Immutable
data class HabitCompletion(
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val date: String, // YYYY-MM-DD
    val completedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
