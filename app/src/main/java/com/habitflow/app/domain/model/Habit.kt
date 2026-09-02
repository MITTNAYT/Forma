package com.habitflow.app.domain.model

import com.habitflow.app.core.util.SyncStatus
import java.util.UUID

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: String = "target",
    val colorTag: String = "#EB5757",
    val timeOfDay: TimeOfDay = TimeOfDay.ANYTIME,
    val energyLevel: EnergyLevel = EnergyLevel.MEDIUM,
    val repeatDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7), // 1 = Monday, 7 = Sunday
    val createdAt: Long = System.currentTimeMillis(),
    val archived: Boolean = false,
    val reminderTimeMinutes: Int? = null, // e.g. 540 for 09:00 AM (minutes from midnight)
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
