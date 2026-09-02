package com.habitflow.app.domain.repository

import com.habitflow.app.domain.model.TimelineItem
import kotlinx.coroutines.flow.Flow

interface TimelineRepository {
    fun getTimelineItemsForDate(date: String): Flow<List<TimelineItem>>
    fun getTimelineItemById(id: String): Flow<TimelineItem?>
    fun getAllTimelineItems(): Flow<List<TimelineItem>>
    suspend fun insertTimelineItem(item: TimelineItem)
    suspend fun updateTimelineItem(item: TimelineItem)
    suspend fun deleteTimelineItem(item: TimelineItem)
    suspend fun toggleTimelineItemCompletion(id: String, completed: Boolean)
}
