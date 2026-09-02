package com.habitflow.app.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.habitflow.app.core.util.DateUtils
import com.habitflow.app.data.local.dao.HabitCompletionDao
import com.habitflow.app.data.local.dao.HabitDao
import com.habitflow.app.data.local.dao.TimelineDao
import com.habitflow.app.data.local.entity.HabitCompletionEntity
import com.habitflow.app.data.local.entity.HabitEntity
import com.habitflow.app.data.local.entity.TimelineItemEntity
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

        CoroutineScope(Dispatchers.IO).launch {
            seedInitialData()
        }
    }

    suspend fun seedInitialData() {
        val habitDao = habitDaoProvider.get()
        val habitCompletionDao = habitCompletionDaoProvider.get()
        val timelineDao = timelineDaoProvider.get()

        if (habitDao.getHabitsCount() > 0) return

        val today = DateUtils.today()
        val todayStr = DateUtils.formatDateIso(today)

        val habit1Id = UUID.randomUUID().toString()
        val habit2Id = UUID.randomUUID().toString()
        val habit3Id = UUID.randomUUID().toString()

        val habit1 = HabitEntity(
            id = habit1Id,
            name = "Morning Sunlight & Hydration",
            icon = "sun",
            colorTag = "#FFFFFF",
            timeOfDay = "MORNING",
            energyLevel = "HIGH",
            repeatDays = "1,2,3,4,5,6,7",
            createdAt = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000),
            archived = false,
            reminderTimeMinutes = 450, // 07:30 AM
            updatedAt = System.currentTimeMillis(),
            syncStatus = "SYNCED"
        )

        val habit2 = HabitEntity(
            id = habit2Id,
            name = "Deep Work Session",
            icon = "code",
            colorTag = "#FFFFFF",
            timeOfDay = "AFTERNOON",
            energyLevel = "HIGH",
            repeatDays = "1,2,3,4,5", // Mon-Fri
            createdAt = System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000),
            archived = false,
            reminderTimeMinutes = 840, // 02:00 PM
            updatedAt = System.currentTimeMillis(),
            syncStatus = "SYNCED"
        )

        val habit3 = HabitEntity(
            id = habit3Id,
            name = "Evening Reading & Reflection",
            icon = "book",
            colorTag = "#FFFFFF",
            timeOfDay = "EVENING",
            energyLevel = "LOW",
            repeatDays = "1,2,3,4,5,6,7",
            createdAt = System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000),
            archived = false,
            reminderTimeMinutes = 1260, // 09:00 PM
            updatedAt = System.currentTimeMillis(),
            syncStatus = "SYNCED"
        )

        habitDao.insertHabits(listOf(habit1, habit2, habit3))

        // Pre-seed sample completions for previous 3 days to establish initial streaks
        val completions = mutableListOf<HabitCompletionEntity>()
        for (dayOffset in 1..3) {
            val pastDateStr = DateUtils.formatDateIso(today.minusDays(dayOffset.toLong()))
            completions.add(
                HabitCompletionEntity(
                    id = UUID.randomUUID().toString(),
                    habitId = habit1Id,
                    date = pastDateStr,
                    completedAt = System.currentTimeMillis() - (dayOffset.toLong() * 24 * 60 * 60 * 1000),
                    updatedAt = System.currentTimeMillis(),
                    syncStatus = "SYNCED"
                )
            )
            completions.add(
                HabitCompletionEntity(
                    id = UUID.randomUUID().toString(),
                    habitId = habit3Id,
                    date = pastDateStr,
                    completedAt = System.currentTimeMillis() - (dayOffset.toLong() * 24 * 60 * 60 * 1000),
                    updatedAt = System.currentTimeMillis(),
                    syncStatus = "SYNCED"
                )
            )
        }
        habitCompletionDao.insertCompletions(completions)

        // Pre-seed 2 timeline items for today
        val subtask1 = "${UUID.randomUUID()}:::Review sync architecture schema:::true"
        val subtask2 = "${UUID.randomUUID()}:::Draft clean domain API contracts:::false"
        val timelineItem1 = TimelineItemEntity(
            id = UUID.randomUUID().toString(),
            title = "Product Architecture Review",
            date = todayStr,
            startTime = "10:00",
            endTime = "11:30",
            icon = "pin",
            colorTag = "#FFFFFF",
            notes = "Focus on Room offline-first sync guarantees and minimalist Jetpack Compose UI.",
            subtasksRaw = "$subtask1|||$subtask2",
            isRecurring = false,
            repeatDays = "1,2,3,4,5,6,7",
            reminderMinutesBefore = 15,
            completed = false,
            updatedAt = System.currentTimeMillis(),
            syncStatus = "SYNCED"
        )

        val timelineItem2 = TimelineItemEntity(
            id = UUID.randomUUID().toString(),
            title = "Team Sync & Daily Alignment",
            date = todayStr,
            startTime = "16:00",
            endTime = "16:45",
            icon = "voice",
            colorTag = "#FFFFFF",
            notes = "Discuss habit streaks, offline local caching, and timeline navigation.",
            subtasksRaw = "",
            isRecurring = true,
            repeatDays = "1,2,3,4,5",
            reminderMinutesBefore = 10,
            completed = false,
            updatedAt = System.currentTimeMillis(),
            syncStatus = "SYNCED"
        )

        timelineDao.insertTimelineItems(listOf(timelineItem1, timelineItem2))
    }
}
