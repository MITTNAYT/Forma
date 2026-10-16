import SwiftUI

public struct HabitCardView: View {
    let habit: Habit
    let isCompleted: Bool
    let streakCount: Int
    let onToggle: () -> Void
    let onEdit: () -> Void

    private var habitColor: Color {
        Color(hex: habit.colorTag)
    }

    public var body: some View {
        HStack(spacing: 14) {
            // Checkmark Toggle Button (44x44pt touch target)
            Button(action: onToggle) {
                ZStack {
                    RoundedRectangle(cornerRadius: 12)
                        .fill(isCompleted ? habitColor : FormaTheme.surfaceElevated)
                        .frame(width: 44, height: 44)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(isCompleted ? habitColor : FormaTheme.border, lineWidth: 1.5)
                        )

                    if isCompleted {
                        Image(systemName: "checkmark")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.white)
                            .transition(.scale.combined(with: .opacity))
                    } else {
                        Image(systemName: habit.icon)
                            .font(.system(size: 18))
                            .foregroundColor(FormaTheme.textSecondary)
                    }
                }
            }
            .buttonStyle(PlainButtonStyle())

            // Habit Details (Tap to Edit)
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 8) {
                    Text(habit.name)
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(isCompleted ? FormaTheme.textMuted : FormaTheme.textPrimary)
                        .strikethrough(isCompleted, color: FormaTheme.textMuted)

                    Spacer()

                    // Streak Badge
                    if streakCount > 0 {
                        HStack(spacing: 3) {
                            Image(systemName: "flame.fill")
                                .font(.system(size: 11))
                                .foregroundColor(FormaTheme.ochreGold)
                            Text("\(streakCount)")
                                .font(.system(size: 12, weight: .bold, design: .rounded))
                                .foregroundColor(FormaTheme.ochreGold)
                        }
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(FormaTheme.ochreGold.opacity(0.12))
                        .cornerRadius(6)
                    }
                }

                // Stacked Cue or Frequency
                if let cue = habit.stackedCueText, !cue.isEmpty {
                    Text(cue)
                        .font(.system(size: 12))
                        .foregroundColor(FormaTheme.textMuted)
                        .lineLimit(1)
                } else {
                    HStack(spacing: 6) {
                        Text(habit.timeOfDay.title)
                            .font(.system(size: 11, weight: .medium))
                            .foregroundColor(FormaTheme.accent)

                        Text("•")
                            .font(.system(size: 10))
                            .foregroundColor(FormaTheme.textMuted)

                        Text(habit.energyLevel.label)
                            .font(.system(size: 11))
                            .foregroundColor(FormaTheme.textMuted)
                    }
                }
            }
            .contentShape(Rectangle())
            .onTapGesture {
                onEdit()
            }
        }
        .padding(14)
        .background(isCompleted ? FormaTheme.surface.opacity(0.7) : FormaTheme.surface)
        .cornerRadius(14)
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(isCompleted ? habitColor.opacity(0.3) : FormaTheme.border, lineWidth: 1)
        )
        .animation(.spring(response: 0.35, dampingFraction: 0.7), value: isCompleted)
    }
}
