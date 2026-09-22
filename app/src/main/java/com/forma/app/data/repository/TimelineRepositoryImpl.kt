package com.forma.app.data.repository

import com.forma.app.data.local.dao.TimelineDao
import com.forma.app.data.local.entity.TimelineItemEntity
import com.forma.app.domain.model.TimelineItem
import com.forma.app.domain.repository.TimelineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimelineRepositoryImpl @Inject constructor(
    private val timelineDao: TimelineDao
) : TimelineRepository {

    override fun getTimelineItemsForDate(date: String): Flow<List<TimelineItem>> {
        return timelineDao.getTimelineItemsForDate(date).map { list -> list.map { it.toDomain() } }
    }

    override fun getTimelineItemById(id: String): Flow<TimelineItem?> {
        return timelineDao.getTimelineItemById(id).map { it?.toDomain() }
    }

    override fun getAllTimelineItems(): Flow<List<TimelineItem>> {
        return timelineDao.getAllTimelineItems().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertTimelineItem(item: TimelineItem) {
        timelineDao.insertTimelineItem(TimelineItemEntity.fromDomain(item))
    }

    override suspend fun updateTimelineItem(item: TimelineItem) {
        timelineDao.updateTimelineItem(TimelineItemEntity.fromDomain(item.copy(updatedAt = System.currentTimeMillis())))
    }

    override suspend fun deleteTimelineItem(item: TimelineItem) {
        timelineDao.deleteTimelineItem(TimelineItemEntity.fromDomain(item))
    }

    override suspend fun toggleTimelineItemCompletion(id: String, completed: Boolean) {
        timelineDao.setCompleted(id, completed)
    }
}
