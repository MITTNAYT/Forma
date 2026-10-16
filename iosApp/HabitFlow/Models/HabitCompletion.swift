import Foundation

public struct HabitCompletion: Identifiable, Codable, Equatable {
    public let id: String
    public let habitId: String
    public let date: String // "YYYY-MM-DD"
    public let completedAt: Int64

    public init(
        id: String = UUID().uuidString,
        habitId: String,
        date: String,
        completedAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    ) {
        self.id = id
        self.habitId = habitId
        self.date = date
        self.completedAt = completedAt
    }
}
