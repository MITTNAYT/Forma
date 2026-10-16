import Foundation

public struct StreakEngine {
    
    /// Ultra-fast calculation of current streak without scanning entire history.
    public static func calculateCurrentStreak(
        habit: Habit,
        completedDatesSet: Set<String>,
        referenceDate: Date = Date()
    ) -> Int {
        let scheduledRepeatDays = habit.repeatDays.isEmpty ? Set(1...7) : habit.repeatDays
        var currentStreak = 0
        var checkDate = referenceDate

        let todayStr = DateUtils.formatDate(referenceDate)
        let isTodayScheduled = scheduledRepeatDays.contains(DateUtils.getIsoWeekday(for: referenceDate))
        let isTodayCompleted = completedDatesSet.contains(todayStr)

        if isTodayCompleted {
            currentStreak += 1
            checkDate = DateUtils.addDays(to: checkDate, days: -1)
        } else if !isTodayScheduled {
            checkDate = DateUtils.addDays(to: checkDate, days: -1)
        } else {
            // Today is scheduled but not completed yet; don't break yesterday's streak yet
            checkDate = DateUtils.addDays(to: checkDate, days: -1)
        }

        let habitStartDate: Date
        if let startStr = habit.startDate, let parsed = DateUtils.parseDate(startStr) {
            habitStartDate = parsed
        } else {
            habitStartDate = Date(timeIntervalSince1970: Double(habit.createdAt) / 1000.0)
        }

        var daysBack = 0
        let maxLookback = 365

        while daysBack < maxLookback && checkDate >= habitStartDate {
            let dayOfWeek = DateUtils.getIsoWeekday(for: checkDate)
            let isScheduled = scheduledRepeatDays.contains(dayOfWeek)

            if isScheduled {
                let dateStr = DateUtils.formatDate(checkDate)
                if completedDatesSet.contains(dateStr) {
                    currentStreak += 1
                } else {
                    break
                }
            }
            checkDate = DateUtils.addDays(to: checkDate, days: -1)
            daysBack += 1
        }

        return currentStreak
    }

    /// Full calculation of current streak, longest streak, and completion metrics.
    public static func calculateStreakInfo(
        habit: Habit,
        completions: [HabitCompletion],
        referenceDate: Date = Date()
    ) -> HabitStreakInfo {
        let completedDatesSet = Set(completions.map { $0.date })
        let currentStreak = calculateCurrentStreak(habit: habit, completedDatesSet: completedDatesSet, referenceDate: referenceDate)

        let habitStartDate: Date
        if let startStr = habit.startDate, let parsed = DateUtils.parseDate(startStr) {
            habitStartDate = parsed
        } else {
            habitStartDate = Date(timeIntervalSince1970: Double(habit.createdAt) / 1000.0)
        }

        var longestStreak = currentStreak
        var runningStreak = 0

        var scanDate = habitStartDate
        var totalScheduledDays = 0

        let calendar = Calendar.current
        let startOfToday = calendar.startOfDay(for: referenceDate)

        while scanDate <= referenceDate {
            let isScheduled = habit.isScheduled(on: scanDate)

            if isScheduled {
                totalScheduledDays += 1
                let dateStr = DateUtils.formatDate(scanDate)
                if completedDatesSet.contains(dateStr) {
                    runningStreak += 1
                    if runningStreak > longestStreak {
                        longestStreak = runningStreak
                    }
                } else {
                    let startOfScan = calendar.startOfDay(for: scanDate)
                    if startOfScan < startOfToday {
                        runningStreak = 0
                    }
                }
            }
            scanDate = DateUtils.addDays(to: scanDate, days: 1)
        }

        let totalCompletions = completedDatesSet.count
        let missedCount = max(0, totalScheduledDays - totalCompletions)
        let completionRate: Int
        if totalScheduledDays > 0 {
            completionRate = min(100, max(0, Int((Double(totalCompletions) / Double(totalScheduledDays)) * 100.0)))
        } else {
            completionRate = totalCompletions > 0 ? 100 : 0
        }

        return HabitStreakInfo(
            habitId: habit.id,
            habitName: habit.name,
            habitIcon: habit.icon,
            habitColor: habit.colorTag,
            currentStreak: currentStreak,
            longestStreak: max(longestStreak, currentStreak),
            totalCompletions: totalCompletions,
            completionRatePercentage: completionRate,
            skippedCount: 0,
            missedCount: missedCount
        )
    }
}
