package com.habitflow.app.domain.usecase

import com.habitflow.app.core.util.DateUtils
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.HabitCompletion
import com.habitflow.app.domain.model.HabitStreakInfo
import java.time.LocalDate
import javax.inject.Inject

class CalculateStreakUseCase @Inject constructor() {

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

        // Calculate Current Streak
        var currentStreak = 0
        var checkDate = referenceDate

        // If today is scheduled and completed, count today and step backwards.
        // If today is scheduled and NOT completed, we still don't break the streak if yesterday was completed (since today is still in progress).
        val todayStr = DateUtils.formatDateIso(referenceDate)
        val isTodayScheduled = scheduledRepeatDays.contains(DateUtils.getDayOfWeekInt(referenceDate))
        val isTodayCompleted = completedDatesSet.contains(todayStr)

        if (isTodayCompleted) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        } else if (!isTodayScheduled) {
            // Today is not a scheduled day, start checking from yesterday backwards
            checkDate = checkDate.minusDays(1)
        } else {
            // Today is scheduled but not completed yet; start checking from yesterday backwards
            checkDate = checkDate.minusDays(1)
        }

        // Iterate backwards through past days
        val createdDate = LocalDate.ofEpochDay(habit.createdAt / (1000 * 60 * 60 * 24))
        var daysBack = 0
        val maxLookback = 365 * 2

        while (daysBack < maxLookback && !checkDate.isBefore(createdDate)) {
            val dayOfWeek = DateUtils.getDayOfWeekInt(checkDate)
            val isScheduled = scheduledRepeatDays.contains(dayOfWeek)

            if (isScheduled) {
                val dateStr = DateUtils.formatDateIso(checkDate)
                if (completedDatesSet.contains(dateStr)) {
                    currentStreak++
                } else {
                    // Missed scheduled day, streak breaks
                    break
                }
            }
            checkDate = checkDate.minusDays(1)
            daysBack++
        }

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
            completionRatePercentage = completionRate
        )
    }
}
