package com.forma.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.forma.app.core.util.SyncStatus
import com.forma.app.domain.model.Subtask
import com.forma.app.domain.model.TimelineItem

@Entity(
    tableName = "timeline_items",
    indices = [
        Index(value = ["date"]),
        Index(value = ["isRecurring"])
    ]
)
data class TimelineItemEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val date: String, // YYYY-MM-DD
    val startTime: String?, // HH:mm
    val endTime: String?, // HH:mm
    val icon: String,
    val colorTag: String,
    val notes: String,
    val subtasksRaw: String, // Encoded subtasks "id::title::isDone|id::title::isDone"
    val isRecurring: Boolean,
    val repeatDays: String, // "1,2,3,4,5,6,7"
    val reminderMinutesBefore: Int?,
    val completed: Boolean,
    val updatedAt: Long,
    val syncStatus: String
) {
    fun toDomain(): TimelineItem {
        val days = if (repeatDays.isBlank()) emptySet() else repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()

        val subtasksList = if (subtasksRaw.isBlank()) {
            emptyList()
        } else {
            subtasksRaw.split("|||").mapNotNull { subtaskStr ->
                val parts = subtaskStr.split(":::")
                if (parts.size >= 3) {
                    Subtask(
                        id = parts[0],
                        title = parts[1],
                        completed = parts[2].toBoolean()
                    )
                } else null
            }
        }

        return TimelineItem(
            id = id,
            title = title,
            date = date,
            startTime = startTime,
            endTime = endTime,
            icon = icon,
            colorTag = colorTag,
            notes = notes,
            subtasks = subtasksList,
            isRecurring = isRecurring,
            repeatDays = days,
            reminderMinutesBefore = reminderMinutesBefore,
            completed = completed,
            updatedAt = updatedAt,
            syncStatus = try { SyncStatus.valueOf(syncStatus) } catch (_: Exception) { SyncStatus.SYNCED }
        )
    }

    companion object {
        fun fromDomain(item: TimelineItem): TimelineItemEntity {
            val encodedSubtasks = item.subtasks.joinToString("|||") {
                "${it.id}:::${it.title}:::${it.completed}"
            }
            return TimelineItemEntity(
                id = item.id,
                title = item.title,
                date = item.date,
                startTime = item.startTime,
                endTime = item.endTime,
                icon = item.icon,
                colorTag = item.colorTag,
                notes = item.notes,
                subtasksRaw = encodedSubtasks,
                isRecurring = item.isRecurring,
                repeatDays = item.repeatDays.joinToString(","),
                reminderMinutesBefore = item.reminderMinutesBefore,
                completed = item.completed,
                updatedAt = item.updatedAt,
                syncStatus = item.syncStatus.name
            )
        }
    }
}
