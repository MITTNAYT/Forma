import Foundation

public struct HabitStreakInfo: Identifiable, Codable, Equatable {
    public var id: String { habitId }
    public let habitId: String
    public let habitName: String
    public let habitIcon: String
    public let habitColor: String
    public let currentStreak: Int
    public let longestStreak: Int
    public let totalCompletions: Int
    public let completionRatePercentage: Int
    public let skippedCount: Int
    public let missedCount: Int

    public init(
        habitId: String,
        habitName: String,
        habitIcon: String,
        habitColor: String,
        currentStreak: Int,
        longestStreak: Int,
        totalCompletions: Int,
        completionRatePercentage: Int,
        skippedCount: Int = 0,
        missedCount: Int = 0
    ) {
        self.habitId = habitId
        self.habitName = habitName
        self.habitIcon = habitIcon
        self.habitColor = habitColor
        self.currentStreak = currentStreak
        self.longestStreak = longestStreak
        self.totalCompletions = totalCompletions
        self.completionRatePercentage = completionRatePercentage
        self.skippedCount = skippedCount
        self.missedCount = missedCount
    }
}
