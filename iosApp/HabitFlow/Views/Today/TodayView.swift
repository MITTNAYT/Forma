import SwiftUI

public struct TodayView: View {
    @EnvironmentObject private var viewModel: HabitsViewModel
    @State private var habitToEdit: Habit? = nil
    @State private var isAddingHabit: Bool = false

    public var body: some View {
        NavigationView {
            ZStack {
                FormaTheme.background.ignoresSafeArea()

                ScrollView {
                    LazyVStack(spacing: 16) {
                        // Date Header
                        dateHeaderSection

                        // Progress & Streak Ring
                        DynamicStreakRing(
                            progress: viewModel.todayProgressPercentage,
                            streakCount: viewModel.bestActiveStreak,
                            completedCount: viewModel.completedCountToday,
                            totalCount: viewModel.scheduledCountToday
                        )

                        // Time of Day Filter Pills
                        timeOfDayFilterBar

                        // Habit Cards List
                        if viewModel.todayHabits.isEmpty {
                            emptyStateView
                        } else {
                            ForEach(viewModel.todayHabits) { habit in
                                HabitCardView(
                                    habit: habit,
                                    isCompleted: viewModel.isCompleted(habitId: habit.id, on: viewModel.selectedDate),
                                    streakCount: viewModel.getStreakInfo(for: habit).currentStreak,
                                    onToggle: {
                                        viewModel.toggleCompletion(for: habit, on: viewModel.selectedDate)
                                    },
                                    onEdit: {
                                        habitToEdit = habit
                                    }
                                )
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 12)
                    .padding(.bottom, 32)
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Text("Forma")
                        .font(.system(size: 20, weight: .bold, design: .serif))
                        .foregroundColor(FormaTheme.textPrimary)
                }

                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { isAddingHabit = true }) {
                        Image(systemName: "plus.circle.fill")
                            .font(.system(size: 22))
                            .foregroundColor(FormaTheme.accent)
                    }
                }
            }
            .sheet(isPresented: $isAddingHabit) {
                AddEditHabitSheet(habitToEdit: nil) { newHabit in
                    viewModel.addHabit(newHabit)
                }
                .environmentObject(viewModel)
            }
            .sheet(item: $habitToEdit) { habit in
                AddEditHabitSheet(habitToEdit: habit) { updatedHabit in
                    viewModel.updateHabit(updatedHabit)
                }
                .environmentObject(viewModel)
            }
        }
        .navigationViewStyle(StackNavigationViewStyle()) // Best for iPhone single-column navigation
    }

    private var dateHeaderSection: some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text(DateUtils.formatDisplayDate(viewModel.selectedDate).uppercased())
                    .font(.system(size: 11, weight: .bold, design: .monospaced))
                    .foregroundColor(FormaTheme.accent)

                Text(Calendar.current.isDateInToday(viewModel.selectedDate) ? "Today's Sanctuary" : "Daily Reflection")
                    .font(.system(size: 22, weight: .bold, design: .serif))
                    .foregroundColor(FormaTheme.textPrimary)
            }

            Spacer()

            // Quick Today Reset button if viewing other dates
            if !Calendar.current.isDateInToday(viewModel.selectedDate) {
                Button(action: {
                    viewModel.selectedDate = Date()
                    ZenHaptics.selectionChanged()
                }) {
                    Text("Back to Today")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(FormaTheme.accent)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(FormaTheme.surfaceElevated)
                        .cornerRadius(8)
                }
            }
        }
    }

    private var timeOfDayFilterBar: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                filterPill(title: "All", category: nil)
                filterPill(title: "Morning", category: .morning)
                filterPill(title: "Afternoon", category: .afternoon)
                filterPill(title: "Evening", category: .evening)
            }
            .padding(.vertical, 4)
        }
    }

    private func filterPill(title: String, category: TimeOfDayCategory?) -> some View {
        let isSelected = viewModel.selectedTimeOfDay == category
        return Button(action: {
            viewModel.selectedTimeOfDay = category
            ZenHaptics.selectionChanged()
        }) {
            Text(title)
                .font(.system(size: 13, weight: isSelected ? .semibold : .regular))
                .foregroundColor(isSelected ? FormaTheme.background : FormaTheme.textSecondary)
                .padding(.horizontal, 14)
                .padding(.vertical, 6)
                .background(isSelected ? FormaTheme.accent : FormaTheme.surfaceElevated)
                .cornerRadius(20)
                .overlay(
                    RoundedRectangle(cornerRadius: 20)
                        .stroke(isSelected ? FormaTheme.accent : FormaTheme.border, lineWidth: 1)
                )
        }
    }

    private var emptyStateView: some View {
        VStack(spacing: 12) {
            Image(systemName: "sparkles")
                .font(.system(size: 36))
                .foregroundColor(FormaTheme.accent)
                .padding(.top, 24)

            Text("No Rituals Here")
                .font(.system(size: 16, weight: .medium, design: .serif))
                .foregroundColor(FormaTheme.textPrimary)

            Text("Create a new mindful commitment or change your filter.")
                .font(.system(size: 13))
                .foregroundColor(FormaTheme.textMuted)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)

            Button(action: { isAddingHabit = true }) {
                Text("Create Habit")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(FormaTheme.background)
                    .padding(.horizontal, 18)
                    .padding(.vertical, 10)
                    .background(FormaTheme.accent)
                    .cornerRadius(10)
            }
            .padding(.top, 8)
            .padding(.bottom, 24)
        }
        .frame(maxWidth: .infinity)
        .background(FormaTheme.surface)
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(FormaTheme.border, lineWidth: 1)
        )
    }
}
