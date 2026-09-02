package com.habitflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.habitflow.app.core.util.SyncStatus
import com.habitflow.app.domain.model.EnergyLevel
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.TimeOfDay

@Entity(tableName = "habits")
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
    val syncStatus: String // PENDING, SYNCED, FAILED
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
            syncStatus = try { SyncStatus.valueOf(syncStatus) } catch (_: Exception) { SyncStatus.SYNCED }
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
                syncStatus = habit.syncStatus.name
            )
        }
    }
}
