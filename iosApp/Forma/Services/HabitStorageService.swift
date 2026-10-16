import Foundation

public final class HabitStorageService {
    public static let shared = HabitStorageService()

    private let fileManager = FileManager.default
    private let habitsFileName = "forma_habits.json"
    private let completionsFileName = "forma_completions.json"

    private var storageDirectory: URL {
        let urls = fileManager.urls(for: .applicationSupportDirectory, in: .userDomainMask)
        let dir = urls[0].appendingPathComponent("FormaData", isDirectory: true)
        if !fileManager.fileExists(atPath: dir.path) {
            try? fileManager.createDirectory(at: dir, withIntermediateDirectories: true, attributes: nil)
        }
        return dir
    }

    private var habitsFileURL: URL {
        storageDirectory.appendingPathComponent(habitsFileName)
    }

    private var completionsFileURL: URL {
        storageDirectory.appendingPathComponent(completionsFileName)
    }

    private init() {}

    // MARK: - Habits Operations

    public func loadHabits() -> [Habit] {
        guard fileManager.fileExists(atPath: habitsFileURL.path) else {
            let defaults = createDefaultHabits()
            saveHabits(defaults)
            return defaults
        }

        do {
            let data = try Data(contentsOf: habitsFileURL)
            let habits = try JSONDecoder().decode([Habit].self, from: data)
            return habits
        } catch {
            print("Error loading habits: \(error)")
            return []
        }
    }

    public func saveHabits(_ habits: [Habit]) {
        do {
            let data = try JSONEncoder().encode(habits)
            try data.write(to: habitsFileURL, options: .atomic)
        } catch {
            print("Error saving habits: \(error)")
        }
    }

    // MARK: - Completions Operations

    public func loadCompletions() -> [HabitCompletion] {
        guard fileManager.fileExists(atPath: completionsFileURL.path) else {
            return []
        }

        do {
            let data = try Data(contentsOf: completionsFileURL)
            let completions = try JSONDecoder().decode([HabitCompletion].self, from: data)
            return completions
        } catch {
            print("Error loading completions: \(error)")
            return []
        }
    }

    public func saveCompletions(_ completions: [HabitCompletion]) {
        do {
            let data = try JSONEncoder().encode(completions)
            try data.write(to: completionsFileURL, options: .atomic)
        } catch {
            print("Error saving completions: \(error)")
        }
    }

    // MARK: - Default Seed Habits

    private func createDefaultHabits() -> [Habit] {
        let today = DateUtils.todayString()
        return [
            Habit(
                name: "Morning Sunlight & Breath",
                icon: "sun.max.fill",
                colorTag: "#D99B26",
                timeOfDay: .morning,
                energyLevel: .medium,
                repeatDays: Set(1...7),
                stackedCueText: "After I wake up and open the blinds",
                startDate: today
            ),
            Habit(
                name: "Deep Focus Flow",
                icon: "brain.head.profile",
                colorTag: "#4D7799",
                timeOfDay: .morning,
                energyLevel: .high,
                repeatDays: Set(1...5),
                stackedCueText: "At 09:30 AM before reading email",
                startDate: today
            ),
            Habit(
                name: "Mindful Tea / Hydration",
                icon: "cup.and.saucer.fill",
                colorTag: "#4E6542",
                timeOfDay: .afternoon,
                energyLevel: .low,
                repeatDays: Set(1...7),
                stackedCueText: "After lunch break",
                startDate: today
            ),
            Habit(
                name: "Evening Reflection & Unwind",
                icon: "moon.stars.fill",
                colorTag: "#7D6B90",
                timeOfDay: .evening,
                energyLevel: .low,
                repeatDays: Set(1...7),
                stackedCueText: "Before closing my laptop for the night",
                startDate: today
            )
        ]
    }
}
