package com.forma.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.forma.app.data.local.dao.DailyReflectionDao
import com.forma.app.data.local.dao.HabitCompletionDao
import com.forma.app.data.local.dao.HabitDao
import com.forma.app.data.local.dao.TimelineDao
import com.forma.app.data.local.entity.DailyReflectionEntity
import com.forma.app.data.local.entity.HabitCompletionEntity
import com.forma.app.data.local.entity.HabitEntity
import com.forma.app.data.local.entity.TimelineItemEntity

@Database(
    entities = [
        HabitEntity::class,
        HabitCompletionEntity::class,
        TimelineItemEntity::class,
        DailyReflectionEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FormaDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun timelineDao(): TimelineDao
    abstract fun dailyReflectionDao(): DailyReflectionDao
}
