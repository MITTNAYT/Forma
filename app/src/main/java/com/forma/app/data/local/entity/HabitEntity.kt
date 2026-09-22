package com.forma.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.forma.app.core.util.SyncStatus
import com.forma.app.domain.model.EnergyLevel
import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.TimeOfDay

@Entity(
    tableName = "habits",
    indices = [
        Index(value = ["archived"]),
        Index(value = ["timeOfDay"])
    ]
)
data class HabitEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val icon: String,
    val colorTag: String,
    val timeOfDay: String, // MORNING, AFTERNOON, EVENING, ANYTIME
    val energyLevel: String, // LOW, MEDIUM, HIGH
    val repeatDays: String, // Comma separated, e.g. "1,2,3,4,5,6,7"
    val createdAt: Long,
    val archived: Boolean,
    val reminderTimeMinutes: Int?,
    val updatedAt: Long,
    val syncStatus: String, // PENDING, SYNCED, FAILED
    val stackedAfterHabitId: String? = null,
    val stackedCueText: String? = null,
    val isWintering: Boolean = false,
    val startDate: String? = null,
    val endDate: String? = null,
    val isIndefinite: Boolean = true
) {
    fun toDomain(): Habit {
        val days = if (repeatDays.isBlank()) emptySet() else repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        return Habit(
            id = id,
            name = name,
            icon = icon,
            colorTag = colorTag,
            timeOfDay = try { TimeOfDay.valueOf(timeOfDay) } catch (_: Exception) { TimeOfDay.ANYTIME },
            energyLevel = try { EnergyLevel.valueOf(energyLevel) } catch (_: Exception) { EnergyLevel.MEDIUM },
            repeatDays = days,
            createdAt = createdAt,
            archived = archived,
            reminderTimeMinutes = reminderTimeMinutes,
            updatedAt = updatedAt,
            syncStatus = try { SyncStatus.valueOf(syncStatus) } catch (_: Exception) { SyncStatus.SYNCED },
            stackedAfterHabitId = stackedAfterHabitId,
            stackedCueText = stackedCueText,
            isWintering = isWintering,
            startDate = startDate,
            endDate = endDate,
            isIndefinite = isIndefinite
        )
    }

    companion object {
        fun fromDomain(habit: Habit): HabitEntity {
            return HabitEntity(
                id = habit.id,
                name = habit.name,
                icon = habit.icon,
                colorTag = habit.colorTag,
                timeOfDay = habit.timeOfDay.name,
                energyLevel = habit.energyLevel.name,
                repeatDays = habit.repeatDays.joinToString(","),
                createdAt = habit.createdAt,
                archived = habit.archived,
                reminderTimeMinutes = habit.reminderTimeMinutes,
                updatedAt = habit.updatedAt,
                syncStatus = habit.syncStatus.name,
                stackedAfterHabitId = habit.stackedAfterHabitId,
                stackedCueText = habit.stackedCueText,
                isWintering = habit.isWintering,
                startDate = habit.startDate,
                endDate = habit.endDate,
                isIndefinite = habit.isIndefinite
            )
        }
    }
}


