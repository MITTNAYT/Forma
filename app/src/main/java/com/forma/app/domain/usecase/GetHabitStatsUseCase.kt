package com.forma.app.domain.usecase

import com.forma.app.core.util.DateUtils
import com.forma.app.domain.model.DayCompletionRate
import com.forma.app.domain.model.HabitStreakInfo
import com.forma.app.domain.model.OverallHabitStats
import com.forma.app.domain.repository.HabitRepository
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
            val completionsByHabit = completions.groupBy { it.habitId }

            // Calculate per-habit stats
            val perHabitStats: List<HabitStreakInfo> = habits.map { habit ->
                val habitCompletions = completionsByHabit[habit.id] ?: emptyList()
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
                val missedOnDate = if (date.isBefore(today)) (totalScheduled - completedOnDate).coerceAtLeast(0) else 0

                val intensity = if (totalScheduled > 0) {
                    (completedOnDate.toFloat() / totalScheduled.toFloat()).coerceIn(0f, 1f)
                } else {
                    if (completedOnDate > 0) 1f else 0f
                }

                DayCompletionRate(
                    date = dateIso,
                    totalScheduled = totalScheduled,
                    completedCount = completedOnDate,
                    skippedCount = 0,
                    missedCount = missedOnDate,
                    intensity = intensity,
                    dayOfMonth = date.dayOfMonth
                )
            }

            // Calculate current month's exact days (e.g. 30 in September, 31 in August, 28/29 in Feb)
            val currentYearMonth = java.time.YearMonth.from(today)
            val daysInCurrentMonth = currentYearMonth.lengthOfMonth()
            val monthName = currentYearMonth.month.name
            val firstDayOfMonth = currentYearMonth.atDay(1)
            // Monday is 1, Sunday is 7 -> offset is 0 for Mon, 6 for Sun
            val firstDayOfWeekOffset = firstDayOfMonth.dayOfWeek.value - 1

            val monthHeatmapDays: List<DayCompletionRate> = (1..daysInCurrentMonth).map { dayNum ->
                val date = currentYearMonth.atDay(dayNum)
                val dateIso = DateUtils.formatDateIso(date)
                val dayOfWeek = DateUtils.getDayOfWeekInt(date)

                val scheduledForDate = habits.filter {
                    it.repeatDays.isEmpty() || it.repeatDays.contains(dayOfWeek)
                }

                val completedOnDate = completionsByDate[dateIso]?.size ?: 0
                val totalScheduled = scheduledForDate.size
                val missedOnDate = if (date.isBefore(today)) (totalScheduled - completedOnDate).coerceAtLeast(0) else 0

                val intensity = if (totalScheduled > 0) {
                    (completedOnDate.toFloat() / totalScheduled.toFloat()).coerceIn(0f, 1f)
                } else {
                    if (completedOnDate > 0) 1f else 0f
                }

                DayCompletionRate(
                    date = dateIso,
                    totalScheduled = totalScheduled,
                    completedCount = completedOnDate,
                    skippedCount = 0,
                    missedCount = missedOnDate,
                    intensity = intensity,
                    dayOfMonth = dayNum
                )
            }

            val bestCurrentStreak = perHabitStats.maxOfOrNull { it.currentStreak } ?: 0
            val bestAllTimeStreak = perHabitStats.maxOfOrNull { it.longestStreak } ?: 0

            val totalCompletionsAllTime = perHabitStats.sumOf { it.totalCompletions }
            val totalMissedAllTime = perHabitStats.sumOf { it.missedCount }
            val totalSkippedAllTime = perHabitStats.sumOf { it.skippedCount }

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
                totalCompletedCount = totalCompletionsAllTime,
                totalSkippedCount = totalSkippedAllTime,
                totalMissedCount = totalMissedAllTime,
                heatmapDays = heatmapDays,
                perHabitStats = perHabitStats,
                monthName = monthName,
                daysInMonth = daysInCurrentMonth,
                monthHeatmapDays = monthHeatmapDays,
                firstDayOfWeekOffset = firstDayOfWeekOffset
            )
        }
    }
}
