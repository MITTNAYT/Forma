package com.forma.app.domain.model

import androidx.compose.runtime.Immutable
import com.forma.app.core.util.DateUtils
import com.forma.app.core.util.SyncStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@Immutable
data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: String = "target",
    val colorTag: String = "#4E6542", // Default to Matcha Green
    val timeOfDay: TimeOfDay = TimeOfDay.ANYTIME,
    val energyLevel: EnergyLevel = EnergyLevel.MEDIUM,
    val repeatDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7), // 1 = Monday, 7 = Sunday
    val createdAt: Long = System.currentTimeMillis(),
    val archived: Boolean = false,
    val reminderTimeMinutes: Int? = null, // e.g. 540 for 09:00 AM (minutes from midnight)
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val stackedAfterHabitId: String? = null, // ID of the precursor habit in cue chain
    val stackedCueText: String? = null, // e.g. "After I pour morning tea"
    val isWintering: Boolean = false, // Guilt-free seasonal pause/hibernation
    val startDate: String? = null, // e.g. "2026-09-13"
    val endDate: String? = null, // e.g. "2026-12-31"
    val isIndefinite: Boolean = true, // Infinity ongoing or fixed duration
    val subtasks: List<Subtask> = emptyList()
) {
    /**
     * Resolves the exact local start date from startDate string, or falls back to createdAt epoch.
     */
    val effectiveStartLocalDate: LocalDate
        get() = try {
            startDate?.let { LocalDate.parse(it) }
        } catch (_: Exception) { null } ?: run {
            Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
        }

    /**
     * Determines whether this habit is scheduled to be active on a given date.
     * Habits only start appearing and tracking from the day the user sets them.
     */
    fun isScheduledOnDate(date: LocalDate): Boolean {
        if (archived) return false
        val start = effectiveStartLocalDate
        if (date.isBefore(start)) return false
        if (endDate != null) {
            val end = try { LocalDate.parse(endDate) } catch (_: Exception) { null }
            if (end != null && date.isAfter(end)) return false
        }
        val dayOfWeek = DateUtils.getDayOfWeekInt(date)
        return repeatDays.isEmpty() || repeatDays.contains(dayOfWeek)
    }
}


