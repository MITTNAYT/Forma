import SwiftUI

public struct HabitsListView: View {
    @EnvironmentObject private var viewModel: HabitsViewModel
    @State private var habitToEdit: Habit? = nil
    @State private var isAddingHabit: Bool = false

    public var body: some View {
        NavigationView {
            ZStack {
                FormaTheme.background.ignoresSafeArea()

                ScrollView {
                    LazyVStack(spacing: 16) {
                        ForEach(viewModel.habits) { habit in
                            habitRow(habit)
                        }
                    }
                    .padding(16)
                }
            }
            .navigationBarTitle("All Rituals", displayMode: .inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { isAddingHabit = true }) {
                        Image(systemName: "plus.circle.fill")
                            .font(.system(size: 20))
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
        .navigationViewStyle(StackNavigationViewStyle())
    }

    private func habitRow(_ habit: Habit) -> some View {
        let streak = viewModel.getStreakInfo(for: habit)
        let color = Color(hex: habit.colorTag)

        return HStack(spacing: 14) {
            ZStack {
                RoundedRectangle(cornerRadius: 10)
                    .fill(color.opacity(0.15))
                    .frame(width: 44, height: 44)
                    .overlay(
                        RoundedRectangle(cornerRadius: 10)
                            .stroke(color.opacity(0.4), lineWidth: 1)
                    )

                Image(systemName: habit.icon)
                    .font(.system(size: 18))
                    .foregroundColor(color)
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(habit.name)
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(FormaTheme.textPrimary)

                HStack(spacing: 8) {
                    Text(habit.timeOfDay.title)
                        .font(.system(size: 12))
                        .foregroundColor(FormaTheme.accent)

                    Text("•")
                        .font(.system(size: 10))
                        .foregroundColor(FormaTheme.textMuted)

                    Text("\(streak.currentStreak)d streak")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(FormaTheme.ochreGold)

                    Text("•")
                        .font(.system(size: 10))
                        .foregroundColor(FormaTheme.textMuted)

                    Text("\(streak.completionRatePercentage)% consistency")
                        .font(.system(size: 12))
                        .foregroundColor(FormaTheme.textMuted)
                }
            }

            Spacer()

            Image(systemName: "chevron.right")
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(FormaTheme.textMuted)
        }
        .padding(14)
        .background(FormaTheme.surface)
        .cornerRadius(14)
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(FormaTheme.border, lineWidth: 1)
        )
        .contentShape(Rectangle())
        .onTapGesture {
            habitToEdit = habit
        }
    }
}
