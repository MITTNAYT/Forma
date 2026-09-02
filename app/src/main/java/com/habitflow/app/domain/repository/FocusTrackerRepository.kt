package com.habitflow.app.domain.repository

import kotlinx.coroutines.flow.Flow

data class FocusItemSummary(
    val itemId: String,
    val title: String,
    val totalMinutes: Int,
    val isHabit: Boolean
)

data class FocusTimeStats(
    val thisWeekMinutes: Int,
    val thisMonthMinutes: Int,
    val allTimeMinutes: Int,
    val topFocusedItems: List<FocusItemSummary>
)

interface FocusTrackerRepository {
    fun getFocusTimeForTask(itemId: String): Flow<Int>
    fun getTotalFocusTimeForDate(date: String): Flow<Int>
    fun getFocusTimeStats(): Flow<FocusTimeStats>
    suspend fun recordFocusSession(itemId: String, itemTitle: String, secondsSpent: Int, isHabit: Boolean, date: String)
}
