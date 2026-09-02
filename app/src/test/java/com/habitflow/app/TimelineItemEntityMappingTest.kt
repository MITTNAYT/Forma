package com.habitflow.app

import com.habitflow.app.core.util.SyncStatus
import com.habitflow.app.data.local.entity.TimelineItemEntity
import com.habitflow.app.domain.model.Subtask
import com.habitflow.app.domain.model.TimelineItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimelineItemEntityMappingTest {

    @Test
    fun `domain model converts to entity and back with subtasks intact`() {
        val original = TimelineItem(
            id = "item_123",
            title = "Architecture Review",
            date = "2026-09-02",
            startTime = "10:00",
            endTime = "11:30",
            icon = "📌",
            colorTag = "#EB5757",
            notes = "Clean MVVM with Room offline first",
            subtasks = listOf(
                Subtask(id = "sub_1", title = "Setup Room entities", completed = true),
                Subtask(id = "sub_2", title = "Setup Hilt injection", completed = false)
            ),
            isRecurring = true,
            repeatDays = setOf(1, 3, 5),
            reminderMinutesBefore = 15,
            completed = false,
            syncStatus = SyncStatus.SYNCED
        )

        val entity = TimelineItemEntity.fromDomain(original)
        val restored = entity.toDomain()

        assertEquals(original.id, restored.id)
        assertEquals(original.title, restored.title)
        assertEquals(original.date, restored.date)
        assertEquals(original.startTime, restored.startTime)
        assertEquals(original.endTime, restored.endTime)
        assertEquals(original.notes, restored.notes)
        assertEquals(original.repeatDays, restored.repeatDays)
        assertEquals(2, restored.subtasks.size)
        assertEquals("sub_1", restored.subtasks[0].id)
        assertTrue(restored.subtasks[0].completed)
        assertEquals("sub_2", restored.subtasks[1].id)
        assertEquals(false, restored.subtasks[1].completed)
    }
}
