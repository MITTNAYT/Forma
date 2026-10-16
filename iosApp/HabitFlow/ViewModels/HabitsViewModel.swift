import Foundation
import Combine

public final class HabitsViewModel: ObservableObject {
    @Published public var habits: [Habit] = []
    @Published public var completions: [HabitCompletion] = []
    @Published public var selectedDate: Date = Date()
    @Published public var selectedTimeOfDay: TimeOfDayCategory? = nil

    private let storage: HabitStorageService

    public init(storage: HabitStorageService = .shared) {
        self.storage = storage
        loadData()
    }

    public func loadData() {
        self.habits = storage.loadHabits()
        self.completions = storage.loadCompletions()
    }

    // MARK: - Filtered Lists

    public var todayHabits: [Habit] {
        habits.filter { habit in
            habit.isScheduled(on: selectedDate)
        }.filter { habit in
            guard let filter = selectedTimeOfDay else { return true }
            return habit.timeOfDay == filter || habit.timeOfDay == .anytime
        }
    }

    // MARK: - Completions & Toggling

    public func isCompleted(habitId: String, on date: Date = Date()) -> Bool {
        let dateStr = DateUtils.formatDate(date)
        return completions.contains { $0.habitId == habitId && $0.date == dateStr }
    }

    public func toggleCompletion(for habit: Habit, on date: Date = Date()) {
        let dateStr = DateUtils.formatDate(date)
        if let index = completions.firstIndex(where: { $0.habitId == habit.id && $0.date == dateStr }) {
            // Remove completion (undo)
            completions.remove(at: index)
            ZenHaptics.habitUndo()
        } else {
            // Add completion
            let newCompletion = HabitCompletion(habitId: habit.id, date: dateStr)
            completions.append(newCompletion)
            ZenHaptics.habitCompleted()
        }
        storage.saveCompletions(completions)
    }

    // MARK: - CRUD Operations

    public func addHabit(_ habit: Habit) {
        habits.append(habit)
        storage.saveHabits(habits)
        ZenHaptics.selectionChanged()
    }

    public func updateHabit(_ habit: Habit) {
        if let index = habits.firstIndex(where: { $0.id == habit.id }) {
            habits[index] = habit
            storage.saveHabits(habits)
            ZenHaptics.selectionChanged()
        }
    }

    public func deleteHabit(id: String) {
        habits.removeAll { $0.id == id }
        completions.removeAll { $0.habitId == id }
        storage.saveHabits(habits)
        storage.saveCompletions(completions)
        ZenHaptics.warningAlert()
    }

    // MARK: - Analytics & Metrics

    public func getStreakInfo(for habit: Habit) -> HabitStreakInfo {
        let habitCompletions = completions.filter { $0.habitId == habit.id }
        return StreakEngine.calculateStreakInfo(habit: habit, completions: habitCompletions, referenceDate: selectedDate)
    }

    public var scheduledCountToday: Int {
        todayHabits.count
    }

    public var completedCountToday: Int {
        let dateStr = DateUtils.formatDate(selectedDate)
        return todayHabits.filter { habit in
            completions.contains { $0.habitId == habit.id && $0.date == dateStr }
        }.count
    }

    public var todayProgressPercentage: Double {
        guard scheduledCountToday > 0 else { return 0.0 }
        return Double(completedCountToday) / Double(scheduledCountToday)
    }

    public var bestActiveStreak: Int {
        habits.map { getStreakInfo(for: $0).currentStreak }.max() ?? 0
    }

    public var totalCompletionsAllTime: Int {
        completions.count
    }
}
