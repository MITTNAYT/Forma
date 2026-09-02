package com.habitflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habitflow.app.data.local.entity.TimelineItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelineDao {

    @Query("SELECT * FROM timeline_items WHERE date = :date ORDER BY startTime ASC")
    fun getTimelineItemsForDate(date: String): Flow<List<TimelineItemEntity>>

    @Query("SELECT * FROM timeline_items WHERE id = :id")
    fun getTimelineItemById(id: String): Flow<TimelineItemEntity?>

    @Query("SELECT * FROM timeline_items WHERE id = :id")
    suspend fun getTimelineItemByIdSync(id: String): TimelineItemEntity?

    @Query("SELECT * FROM timeline_items ORDER BY date ASC, startTime ASC")
    fun getAllTimelineItems(): Flow<List<TimelineItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineItem(item: TimelineItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineItems(items: List<TimelineItemEntity>)

    @Update
    suspend fun updateTimelineItem(item: TimelineItemEntity)

    @Delete
    suspend fun deleteTimelineItem(item: TimelineItemEntity)

    @Query("UPDATE timeline_items SET completed = :completed, updatedAt = :updatedAt, syncStatus = 'PENDING' WHERE id = :id")
    suspend fun setCompleted(id: String, completed: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM timeline_items")
    suspend fun getTimelineItemsCount(): Int
}
