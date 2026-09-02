package com.habitflow.app.domain.model

import com.habitflow.app.core.util.SyncStatus
import java.util.UUID

data class TimelineItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val date: String, // YYYY-MM-DD
    val startTime: String? = null, // HH:mm
    val endTime: String? = null, // HH:mm
    val icon: String = "pin",
    val colorTag: String = "#EB5757",
    val notes: String = "",
    val subtasks: List<Subtask> = emptyList(),
    val isRecurring: Boolean = false,
    val repeatDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
    val reminderMinutesBefore: Int? = null, // e.g. 15 minutes before start
    val completed: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
