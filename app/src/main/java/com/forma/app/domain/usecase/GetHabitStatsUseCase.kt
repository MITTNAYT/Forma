package com.forma.app.domain.usecase

import com.forma.app.core.util.DateUtils
import com.forma.app.domain.model.DayCompletionRate
import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.HabitCompletion
import com.forma.app.domain.model.HabitStreakInfo
import com.forma.app.domain.model.OverallHabitStats
import com.forma.app.domain.repository.HabitRepository
import com.forma.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class GetHabitStatsUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val calculateStreakUseCase: CalculateStreakUseCase
) {
    operator fun invoke(): Flow<OverallHabitStats> {
        val today = DateUtils.today()
        val todayIso = DateUtils.formatDateIso(today)
        val past35Dates = DateUtils.getFiveWeekPastDates(today)

        return combine(
            habitRepository.getAllHabits(includeArchived = false),
            habitRepository.getAllCompletions(),
            userPreferencesRepository.getAllSkippedHabits()
        ) { habits, completions, allSkippedMap ->

            // Anti-Cheat: Strictly filter completions on or before today
            val validCompletions = completions.filter { completion ->
                try {
                    !LocalDate.parse(completion.date).isAfter(today)
                } catch (_: Exception) {
                    false
                }
            }

            val completionsByDate = validCompletions.groupBy { it.date }
            val completionsByHabit = validCompletions.groupBy { it.habitId }

            // Calculate per-habit stats with accurate skips and clean streak support
            val perHabitStats: List<HabitStreakInfo> = habits.map { habit ->
                val habitCompletions = completionsByHabit[habit.id] ?: emptyList()
                val skippedDatesForHabit = allSkippedMap
                    .filter { (_, habitSet) -> habitSet.contains(habit.id) }
                    .keys
                    .toSet()

                calculateStreakUseCase(habit, habitCompletions, today, skippedDatesForHabit)
            }

            // Calculate 5-week heatmap
            val heatmapDays: List<DayCompletionRate> = past35Dates.map { date ->
                val dateIso = DateUtils.formatDateIso(date)

                val scheduledForDate = habits.filter {
                    it.isScheduledOnDate(date)
                }

                val completedOnDate = completionsByDate[dateIso]?.size ?: 0
                val skippedOnDate = (allSkippedMap[dateIso] ?: emptySet()).count { skippedId ->
                    scheduledForDate.any { it.id == skippedId }
                }
                val totalScheduled = scheduledForDate.size
                val missedOnDate = if (date.isBefore(today)) {
                    (totalScheduled - completedOnDate - skippedOnDate).coerceAtLeast(0)
                } else 0

                val effectiveScheduled = (totalScheduled - skippedOnDate).coerceAtLeast(1)
                val intensity = if (totalScheduled > 0) {
                    (completedOnDate.toFloat() / effectiveScheduled.toFloat()).coerceIn(0f, 1f)
                } else {
                    if (completedOnDate > 0) 1f else 0f
                }

                DayCompletionRate(
                    date = dateIso,
                    totalScheduled = totalScheduled,
                    completedCount = completedOnDate,
                    skippedCount = skippedOnDate,
                    missedCount = missedOnDate,
                    intensity = intensity,
                    dayOfMonth = date.dayOfMonth
                )
            }

            // Calculate current month's exact days
            val currentYearMonth = java.time.YearMonth.from(today)
            val daysInCurrentMonth = currentYearMonth.lengthOfMonth()
            val monthName = currentYearMonth.month.name
            val firstDayOfMonth = currentYearMonth.atDay(1)
            val firstDayOfWeekOffset = firstDayOfMonth.dayOfWeek.value - 1

            val monthHeatmapDays: List<DayCompletionRate> = (1..daysInCurrentMonth).map { dayNum ->
                val date = currentYearMonth.atDay(dayNum)
                val dateIso = DateUtils.formatDateIso(date)

                val scheduledForDate = habits.filter {
                    it.isScheduledOnDate(date)
                }

                val completedOnDate = completionsByDate[dateIso]?.size ?: 0
                val skippedOnDate = (allSkippedMap[dateIso] ?: emptySet()).count { skippedId ->
                    scheduledForDate.any { it.id == skippedId }
                }
                val totalScheduled = scheduledForDate.size
                val missedOnDate = if (date.isBefore(today)) {
                    (totalScheduled - completedOnDate - skippedOnDate).coerceAtLeast(0)
                } else 0

                val effectiveScheduled = (totalScheduled - skippedOnDate).coerceAtLeast(1)
                val intensity = if (totalScheduled > 0) {
                    (completedOnDate.toFloat() / effectiveScheduled.toFloat()).coerceIn(0f, 1f)
                } else {
                    if (completedOnDate > 0) 1f else 0f
                }

                DayCompletionRate(
                    date = dateIso,
                    totalScheduled = totalScheduled,
                    completedCount = completedOnDate,
                    skippedCount = skippedOnDate,
                    missedCount = missedOnDate,
                    intensity = intensity,
                    dayOfMonth = dayNum
                )
            }

            val positiveHabits = habits.filter { !it.isNegative }
            val negativeHabits = habits.filter { it.isNegative }

            val positiveStats = perHabitStats.filter { habitStat ->
                positiveHabits.any { it.id == habitStat.habitId }
            }

            val bestCurrentStreak = positiveStats.maxOfOrNull { it.currentStreak } ?: 0
            val bestAllTimeStreak = positiveStats.maxOfOrNull { it.longestStreak } ?: 0

            val bestCleanStreak = negativeHabits.maxOfOrNull { it.cleanDaysCount } ?: 0
            val totalUrgesDeflected = negativeHabits.sumOf { it.urgesDeflected }

            val totalCompletionsAllTime = perHabitStats.sumOf { it.totalCompletions }
            val totalMissedAllTime = perHabitStats.sumOf { it.missedCount }
            val totalSkippedAllTime = perHabitStats.sumOf { it.skippedCount }

            val overallCompletionRate = if (positiveStats.isNotEmpty()) {
                (positiveStats.map { it.completionRatePercentage }.average()).toInt().coerceIn(0, 100)
            } else {
                0
            }

            OverallHabitStats(
                totalActiveHabits = positiveHabits.size,
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
                firstDayOfWeekOffset = firstDayOfWeekOffset,
                totalNegativeHabits = negativeHabits.size,
                bestCleanStreak = bestCleanStreak,
                totalUrgesDeflected = totalUrgesDeflected
            )
        }
    }
}
