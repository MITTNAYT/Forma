package com.forma.app.domain.usecase

import com.forma.app.core.util.DateUtils
import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.HabitCompletion
import com.forma.app.domain.model.HabitStreakInfo
import java.time.LocalDate
import javax.inject.Inject

class CalculateStreakUseCase @Inject constructor() {

    /**
     * Ultra-fast calculation of current streak with anti-cheat and skip preservation.
     * Skips preserve momentum (+0 count) without breaking the streak.
     * Completions in future dates are strictly rejected.
     */
    fun calculateCurrentStreak(
        habit: Habit,
        completedDatesSet: Set<String>,
        referenceDate: LocalDate = DateUtils.today(),
        skippedDatesSet: Set<String> = emptySet()
    ): Int {
        if (habit.isNegative) {
            return habit.cleanDaysCount
        }

        // Anti-Cheat: strictly ignore future date completions
        val validCompletedDates = completedDatesSet.filter { dateStr ->
            try { !LocalDate.parse(dateStr).isAfter(referenceDate) } catch (_: Exception) { false }
        }.toSet()

        val scheduledRepeatDays = if (habit.repeatDays.isEmpty()) setOf(1, 2, 3, 4, 5, 6, 7) else habit.repeatDays

        var currentStreak = 0
        var checkDate = referenceDate

        val todayStr = DateUtils.formatDateIso(referenceDate)
        val isTodayScheduled = scheduledRepeatDays.contains(DateUtils.getDayOfWeekInt(referenceDate))
        val isTodayCompleted = validCompletedDates.contains(todayStr)

        if (isTodayCompleted) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        } else {
            // If today is skipped or pending, streak from previous days is preserved
            checkDate = checkDate.minusDays(1)
        }

        val habitStartDate = habit.effectiveStartLocalDate
        var daysBack = 0
        val maxLookback = 365

        while (daysBack < maxLookback && !checkDate.isBefore(habitStartDate)) {
            val dayOfWeek = DateUtils.getDayOfWeekInt(checkDate)
            val isScheduled = scheduledRepeatDays.contains(dayOfWeek)

            if (isScheduled) {
                val dateStr = DateUtils.formatDateIso(checkDate)
                if (validCompletedDates.contains(dateStr)) {
                    currentStreak++
                } else if (skippedDatesSet.contains(dateStr)) {
                    // Skip preserves streak (+0). Does not break it!
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
     * Anti-cheat enforcement: strictly rejects future dates and calculates accurate skips.
     */
    operator fun invoke(
        habit: Habit,
        completions: List<HabitCompletion>,
        referenceDate: LocalDate = DateUtils.today(),
        skippedDatesSet: Set<String> = emptySet()
    ): HabitStreakInfo {
        if (habit.isNegative) {
            val cleanDays = habit.cleanDaysCount
            return HabitStreakInfo(
                habitId = habit.id,
                habitName = habit.name,
                habitIcon = habit.icon,
                habitColor = habit.colorTag,
                currentStreak = cleanDays,
                longestStreak = cleanDays,
                totalCompletions = cleanDays,
                completionRatePercentage = if (cleanDays > 0) 100 else 0,
                skippedCount = 0,
                missedCount = 0
            )
        }

        // Anti-Cheat: only consider completions on or before referenceDate
        val validCompletedDates = completions
            .map { it.date }
            .filter { dateStr ->
                try { !LocalDate.parse(dateStr).isAfter(referenceDate) } catch (_: Exception) { false }
            }.toSet()

        val currentStreak = calculateCurrentStreak(habit, validCompletedDates, referenceDate, skippedDatesSet)
        val habitStartDate = habit.effectiveStartLocalDate

        var longestStreak = currentStreak
        var runningStreak = 0

        var scanDate = habitStartDate
        var totalScheduledDays = 0

        while (!scanDate.isAfter(referenceDate)) {
            val isScheduled = habit.isScheduledOnDate(scanDate)

            if (isScheduled) {
                totalScheduledDays++
                val dateStr = DateUtils.formatDateIso(scanDate)
                if (validCompletedDates.contains(dateStr)) {
                    runningStreak++
                    if (runningStreak > longestStreak) {
                        longestStreak = runningStreak
                    }
                } else if (skippedDatesSet.contains(dateStr)) {
                    // Skip preserves runningStreak (+0), does not reset to 0
                } else {
                    if (scanDate.isBefore(referenceDate)) {
                        runningStreak = 0
                    }
                }
            }
            scanDate = scanDate.plusDays(1)
        }

        val totalCompletions = validCompletedDates.size
        val totalSkipped = skippedDatesSet.count { dateStr ->
            try {
                val d = LocalDate.parse(dateStr)
                !d.isBefore(habitStartDate) && !d.isAfter(referenceDate) && habit.isScheduledOnDate(d)
            } catch (_: Exception) { false }
        }
        val missedCount = (totalScheduledDays - totalCompletions - totalSkipped).coerceAtLeast(0)
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
            skippedCount = totalSkipped,
            missedCount = missedCount
        )
    }
}
