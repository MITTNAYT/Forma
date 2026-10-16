import Foundation

public enum HabitFrequency: String, Codable, CaseIterable {
    case daily = "DAILY"
    case weekdays = "WEEKDAYS"
    case weekends = "WEEKENDS"
    case custom = "CUSTOM"
}

public enum TimeOfDayCategory: String, Codable, CaseIterable, Identifiable {
    case morning = "MORNING"
    case afternoon = "AFTERNOON"
    case evening = "EVENING"
    case anytime = "ANYTIME"

    public var id: String { rawValue }

    public var title: String {
        switch self {
        case .morning: return "Morning"
        case .afternoon: return "Afternoon"
        case .evening: return "Evening"
        case .anytime: return "Anytime"
        }
    }

    public var systemIcon: String {
        switch self {
        case .morning: return "sunrise.fill"
        case .afternoon: return "sun.max.fill"
        case .evening: return "moon.stars.fill"
        case .anytime: return "sparkles"
        }
    }
}

public enum EnergyLevel: String, Codable, CaseIterable {
    case low = "LOW"
    case medium = "MEDIUM"
    case high = "HIGH"

    public var label: String {
        switch self {
        case .low: return "Gentle"
        case .medium: return "Balanced"
        case .high: return "Intense"
        }
    }
}

public struct Subtask: Identifiable, Codable, Equatable {
    public let id: String
    public var title: String
    public var isCompleted: Bool

    public init(id: String = UUID().uuidString, title: String, isCompleted: Bool = false) {
        self.id = id
        self.title = title
        self.isCompleted = isCompleted
    }
}

public struct Habit: Identifiable, Codable, Equatable {
    public let id: String
    public var name: String
    public var icon: String
    public var colorTag: String
    public var timeOfDay: TimeOfDayCategory
    public var energyLevel: EnergyLevel
    public var repeatDays: Set<Int> // 1 = Monday, 7 = Sunday
    public var createdAt: Int64
    public var archived: Bool
    public var reminderTimeMinutes: Int? // minutes from midnight (e.g. 540 = 09:00 AM)
    public var stackedAfterHabitId: String?
    public var stackedCueText: String?
    public var isWintering: Bool
    public var startDate: String? // "YYYY-MM-DD"
    public var endDate: String? // "YYYY-MM-DD"
    public var isIndefinite: Bool
    public var subtasks: [Subtask]

    public init(
        id: String = UUID().uuidString,
        name: String,
        icon: String = "target",
        colorTag: String = "#4E6542",
        timeOfDay: TimeOfDayCategory = .anytime,
        energyLevel: EnergyLevel = .medium,
        repeatDays: Set<Int> = Set(1...7),
        createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        archived: Bool = false,
        reminderTimeMinutes: Int? = nil,
        stackedAfterHabitId: String? = nil,
        stackedCueText: String? = nil,
        isWintering: Bool = false,
        startDate: String? = nil,
        endDate: String? = nil,
        isIndefinite: Bool = true,
        subtasks: [Subtask] = []
    ) {
        self.id = id
        self.name = name
        self.icon = icon
        self.colorTag = colorTag
        self.timeOfDay = timeOfDay
        self.energyLevel = energyLevel
        self.repeatDays = repeatDays
        self.createdAt = createdAt
        self.archived = archived
        self.reminderTimeMinutes = reminderTimeMinutes
        self.stackedAfterHabitId = stackedAfterHabitId
        self.stackedCueText = stackedCueText
        self.isWintering = isWintering
        self.startDate = startDate
        self.endDate = endDate
        self.isIndefinite = isIndefinite
        self.subtasks = subtasks
    }

    /// Determines if the habit is scheduled for a given date
    public func isScheduled(on date: Date, calendar: Calendar = .current) -> Bool {
        if archived || isWintering { return false }

        let dateString = DateUtils.formatDate(date)

        if let start = startDate, dateString < start {
            return false
        }
        if let end = endDate, dateString > end {
            return false
        }

        // Convert calendar weekday (1=Sun, 2=Mon...7=Sat in Gregorian) to ISO (1=Mon...7=Sun)
        let weekday = calendar.component(.weekday, from: date)
        let isoWeekday = (weekday == 1) ? 7 : (weekday - 1)

        return repeatDays.isEmpty || repeatDays.contains(isoWeekday)
    }
}
