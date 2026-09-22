package com.forma.app.domain.usecase

import com.forma.app.core.util.DateUtils
import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.HabitCompletion
import com.forma.app.domain.model.HabitStreakInfo
import java.time.LocalDate
import javax.inject.Inject

class CalculateStreakUseCase @Inject constructor() {

    /**
     * Ultra-fast calculation of current streak without scanning entire history.
     */
    fun calculateCurrentStreak(
        habit: Habit,
        completedDatesSet: Set<String>,
        referenceDate: LocalDate = DateUtils.today()
    ): Int {
        val scheduledRepeatDays = if (habit.repeatDays.isEmpty()) setOf(1, 2, 3, 4, 5, 6, 7) else habit.repeatDays

        var currentStreak = 0
        var checkDate = referenceDate

        val todayStr = DateUtils.formatDateIso(referenceDate)
        val isTodayScheduled = scheduledRepeatDays.contains(DateUtils.getDayOfWeekInt(referenceDate))
        val isTodayCompleted = completedDatesSet.contains(todayStr)

        if (isTodayCompleted) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        } else if (!isTodayScheduled) {
            checkDate = checkDate.minusDays(1)
        } else {
            checkDate = checkDate.minusDays(1)
        }

        val createdEpochDay = habit.createdAt / (1000L * 60 * 60 * 24)
        val createdDate = LocalDate.ofEpochDay(createdEpochDay.coerceAtLeast(0L))
        var daysBack = 0
        val maxLookback = 365

        while (daysBack < maxLookback && !checkDate.isBefore(createdDate)) {
            val dayOfWeek = DateUtils.getDayOfWeekInt(checkDate)
            val isScheduled = scheduledRepeatDays.contains(dayOfWeek)

            if (isScheduled) {
                val dateStr = DateUtils.formatDateIso(checkDate)
                if (completedDatesSet.contains(dateStr)) {
                    currentStreak++
                } else {
                    break
                }
            }
            checkDate = checkDate.minusDays(1)
            daysBack++
        }

        return currentStreak
    }

    /**
     * Calculates the current streak, longest streak, and completion statistics for a habit.
     * Takes into account the habit's scheduled repeat days (e.g. if a habit repeats only Mon-Fri,
     * weekends do not break the streak).
     */
    operator fun invoke(
        habit: Habit,
        completions: List<HabitCompletion>,
        referenceDate: LocalDate = DateUtils.today()
    ): HabitStreakInfo {
        val completedDatesSet = completions.map { it.date }.toSet()
        val scheduledRepeatDays = if (habit.repeatDays.isEmpty()) setOf(1, 2, 3, 4, 5, 6, 7) else habit.repeatDays
        val currentStreak = calculateCurrentStreak(habit, completedDatesSet, referenceDate)

        val createdEpochDay = habit.createdAt / (1000L * 60 * 60 * 24)
        val createdDate = LocalDate.ofEpochDay(createdEpochDay.coerceAtLeast(0L))

        // Calculate Longest Streak & Total completions
        var longestStreak = currentStreak
        var runningStreak = 0

        // Iterate through all days from createdAt up to referenceDate
        var scanDate = createdDate
        var totalScheduledDays = 0

        while (!scanDate.isAfter(referenceDate)) {
            val dayOfWeek = DateUtils.getDayOfWeekInt(scanDate)
            val isScheduled = scheduledRepeatDays.contains(dayOfWeek)

            if (isScheduled) {
                totalScheduledDays++
                val dateStr = DateUtils.formatDateIso(scanDate)
                if (completedDatesSet.contains(dateStr)) {
                    runningStreak++
                    if (runningStreak > longestStreak) {
                        longestStreak = runningStreak
                    }
                } else {
                    // Only break running streak if scanDate is before today
                    if (scanDate.isBefore(referenceDate)) {
                        runningStreak = 0
                    }
                }
            }
            scanDate = scanDate.plusDays(1)
        }

        val totalCompletions = completedDatesSet.size
        val missedCount = (totalScheduledDays - totalCompletions).coerceAtLeast(0)
        val completionRate = if (totalScheduledDays > 0) {
            ((totalCompletions.toFloat() / totalScheduledDays.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else {
            if (totalCompletions > 0) 100 else 0
        }

        return HabitStreakInfo(
            habitId = habit.id,
            habitName = habit.name,
            habitIcon = habit.icon,
            habitColor = habit.colorTag,
            currentStreak = currentStreak,
            longestStreak = longestStreak.coerceAtLeast(currentStreak),
            totalCompletions = totalCompletions,
            completionRatePercentage = completionRate,
            skippedCount = 0,
            missedCount = missedCount
        )
    }
}
