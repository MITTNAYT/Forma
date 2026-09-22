package com.forma.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.forma.app.core.util.SyncStatus
import com.forma.app.domain.model.HabitCompletion

@Entity(
    tableName = "habit_completions",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId", "date"], unique = true),
        Index(value = ["date"])
    ]
)
data class HabitCompletionEntity(
    @PrimaryKey
    val id: String,
    val habitId: String,
    val date: String, // YYYY-MM-DD
    val completedAt: Long,
    val updatedAt: Long,
    val syncStatus: String
) {
    fun toDomain(): HabitCompletion {
        return HabitCompletion(
            id = id,
            habitId = habitId,
            date = date,
            completedAt = completedAt,
            updatedAt = updatedAt,
            syncStatus = try { SyncStatus.valueOf(syncStatus) } catch (_: Exception) { SyncStatus.SYNCED }
        )
    }

    companion object {
        fun fromDomain(completion: HabitCompletion): HabitCompletionEntity {
            return HabitCompletionEntity(
                id = completion.id,
                habitId = completion.habitId,
                date = completion.date,
                completedAt = completion.completedAt,
                updatedAt = completion.updatedAt,
                syncStatus = completion.syncStatus.name
            )
        }
    }
}
