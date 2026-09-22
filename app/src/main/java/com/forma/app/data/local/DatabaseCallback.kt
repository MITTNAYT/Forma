package com.forma.app.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.forma.app.core.util.DateUtils
import com.forma.app.data.local.dao.HabitCompletionDao
import com.forma.app.data.local.dao.HabitDao
import com.forma.app.data.local.dao.TimelineDao
import com.forma.app.data.local.entity.HabitCompletionEntity
import com.forma.app.data.local.entity.HabitEntity
import com.forma.app.data.local.entity.TimelineItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Provider

class DatabaseCallback(
    private val habitDaoProvider: Provider<HabitDao>,
    private val habitCompletionDaoProvider: Provider<HabitCompletionDao>,
    private val timelineDaoProvider: Provider<TimelineDao>
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Clean slate: no mock habits or data seeded so user starts fresh
    }

    suspend fun seedInitialData() {
        // Clean slate: do not seed mock data so user can start fresh
    }
}
