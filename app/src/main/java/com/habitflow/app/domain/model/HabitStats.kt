package com.habitflow.app.domain.model

data class HabitStreakInfo(
    val habitId: String,
    val habitName: String,
    val habitIcon: String,
    val habitColor: String,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCompletions: Int,
    val completionRatePercentage: Int
)

data class DayCompletionRate(
    val date: String, // YYYY-MM-DD
    val totalScheduled: Int,
    val completedCount: Int,
    val intensity: Float // 0.0f to 1.0f for grayscale heatmap
)

data class OverallHabitStats(
    val totalActiveHabits: Int,
    val overallCompletionRate: Int, // 0 - 100
    val bestCurrentStreak: Int,
    val bestAllTimeStreak: Int,
    val heatmapDays: List<DayCompletionRate>,
    val perHabitStats: List<HabitStreakInfo>
)
