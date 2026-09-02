package com.habitflow.app.domain.usecase

import com.habitflow.app.core.util.DateUtils
import com.habitflow.app.domain.model.DayCompletionRate
import com.habitflow.app.domain.model.HabitStreakInfo
import com.habitflow.app.domain.model.OverallHabitStats
import com.habitflow.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class GetHabitStatsUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val calculateStreakUseCase: CalculateStreakUseCase
) {
    operator fun invoke(): Flow<OverallHabitStats> {
        val today = DateUtils.today()
        val past35Dates = DateUtils.getFiveWeekPastDates(today)

        return combine(
            habitRepository.getAllHabits(includeArchived = false),
            habitRepository.getAllCompletions()
        ) { habits, completions ->

            val completionsByDate = completions.groupBy { it.date }

            // Calculate per-habit stats
            val perHabitStats: List<HabitStreakInfo> = habits.map { habit ->
                val habitCompletions = completions.filter { it.habitId == habit.id }
                calculateStreakUseCase(habit, habitCompletions, today)
            }

            // Calculate 5-week heatmap
            val heatmapDays: List<DayCompletionRate> = past35Dates.map { date ->
                val dateIso = DateUtils.formatDateIso(date)
                val dayOfWeek = DateUtils.getDayOfWeekInt(date)

                // How many habits were scheduled on this date?
                val scheduledForDate = habits.filter {
                    it.repeatDays.isEmpty() || it.repeatDays.contains(dayOfWeek)
                }

                val completedOnDate = completionsByDate[dateIso]?.size ?: 0
                val totalScheduled = scheduledForDate.size

                val intensity = if (totalScheduled > 0) {
                    (completedOnDate.toFloat() / totalScheduled.toFloat()).coerceIn(0f, 1f)
                } else {
                    if (completedOnDate > 0) 1f else 0f
                }

                DayCompletionRate(
                    date = dateIso,
                    totalScheduled = totalScheduled,
                    completedCount = completedOnDate,
                    intensity = intensity
                )
            }

            val bestCurrentStreak = perHabitStats.maxOfOrNull { it.currentStreak } ?: 0
            val bestAllTimeStreak = perHabitStats.maxOfOrNull { it.longestStreak } ?: 0

            val totalScheduledAllTime = perHabitStats.sumOf { (it.totalCompletions * 100) / (if (it.completionRatePercentage > 0) it.completionRatePercentage else 100) }
            val totalCompletionsAllTime = perHabitStats.sumOf { it.totalCompletions }

            val overallCompletionRate = if (perHabitStats.isNotEmpty()) {
                (perHabitStats.map { it.completionRatePercentage }.average()).toInt().coerceIn(0, 100)
            } else {
                0
            }

            OverallHabitStats(
                totalActiveHabits = habits.size,
                overallCompletionRate = overallCompletionRate,
                bestCurrentStreak = bestCurrentStreak,
                bestAllTimeStreak = bestAllTimeStreak,
                heatmapDays = heatmapDays,
                perHabitStats = perHabitStats
            )
        }
    }
}
