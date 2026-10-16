import SwiftUI

public struct StatsView: View {
    @EnvironmentObject private var viewModel: HabitsViewModel

    public var body: some View {
        NavigationView {
            ZStack {
                FormaTheme.background.ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 20) {
                        // High-Level Metric Tiles
                        HStack(spacing: 12) {
                            metricCard(
                                title: "TOTAL RITUALS",
                                value: "\(viewModel.totalCompletionsAllTime)",
                                subtitle: "All-time completions",
                                icon: "checkmark.seal.fill",
                                color: FormaTheme.accent
                            )

                            metricCard(
                                title: "BEST STREAK",
                                value: "\(viewModel.bestActiveStreak)d",
                                subtitle: "Unbroken rhythm",
                                icon: "flame.fill",
                                color: FormaTheme.ochreGold
                            )
                        }

                        // Consistency Heatmap (iOS 15 Canvas)
                        NotionHeatmapView(completions: viewModel.completions)

                        // Habit Breakdown List
                        VStack(alignment: .leading, spacing: 12) {
                            Text("RHYTHM LEADERBOARD")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(FormaTheme.accent)

                            ForEach(viewModel.habits) { habit in
                                let info = viewModel.getStreakInfo(for: habit)
                                habitMetricRow(habit: habit, info: info)
                            }
                        }
                        .padding(16)
                        .background(FormaTheme.surface)
                        .cornerRadius(16)
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(FormaTheme.border, lineWidth: 1)
                        )
                    }
                    .padding(16)
                }
            }
            .navigationBarTitle("Insights", displayMode: .inline)
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }

    private func metricCard(title: String, value: String, subtitle: String, icon: String, color: Color) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(title)
                    .font(.system(size: 10, weight: .bold, design: .monospaced))
                    .foregroundColor(FormaTheme.textMuted)
                Spacer()
                Image(systemName: icon)
                    .foregroundColor(color)
                    .font(.system(size: 14))
            }

            Text(value)
                .font(.system(size: 26, weight: .bold, design: .serif))
                .foregroundColor(FormaTheme.textPrimary)

            Text(subtitle)
                .font(.system(size: 11))
                .foregroundColor(FormaTheme.textSecondary)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(FormaTheme.surface)
        .cornerRadius(14)
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(FormaTheme.border, lineWidth: 1)
        )
    }

    private func habitMetricRow(habit: Habit, info: HabitStreakInfo) -> some View {
        HStack(spacing: 12) {
            Circle()
                .fill(Color(hex: habit.colorTag))
                .frame(width: 10, height: 10)

            Text(habit.name)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(FormaTheme.textPrimary)

            Spacer()

            Text("\(info.currentStreak)d streak")
                .font(.system(size: 13, weight: .bold, design: .rounded))
                .foregroundColor(FormaTheme.ochreGold)

            Text("(\(info.completionRatePercentage)%)")
                .font(.system(size: 12))
                .foregroundColor(FormaTheme.textMuted)
        }
        .padding(.vertical, 6)
    }
}
