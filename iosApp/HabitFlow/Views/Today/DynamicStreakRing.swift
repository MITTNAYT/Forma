import SwiftUI

public struct DynamicStreakRing: View {
    let progress: Double
    let streakCount: Int
    let completedCount: Int
    let totalCount: Int

    public var body: some View {
        HStack(spacing: 20) {
            ZStack {
                // Background Track
                Circle()
                    .stroke(FormaTheme.surfaceElevated, lineWidth: 8)
                    .frame(width: 72, height: 72)

                // Animated Progress Ring
                Circle()
                    .trim(from: 0.0, to: CGFloat(min(progress, 1.0)))
                    .stroke(
                        LinearGradient(
                            gradient: Gradient(colors: [FormaTheme.ochreGold, FormaTheme.matchaGreen]),
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        ),
                        style: StrokeStyle(lineWidth: 8, lineCap: .round)
                    )
                    .rotationEffect(Angle(degrees: -90))
                    .frame(width: 72, height: 72)
                    .animation(.spring(response: 0.6, dampingFraction: 0.7), value: progress)

                // Percentage or Check Icon
                if progress >= 1.0 && totalCount > 0 {
                    Image(systemName: "checkmark")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(FormaTheme.accent)
                } else {
                    Text("\(Int(progress * 100))%")
                        .font(.system(size: 14, weight: .semibold, design: .rounded))
                        .foregroundColor(FormaTheme.textPrimary)
                }
            }

            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 6) {
                    Image(systemName: "flame.fill")
                        .foregroundColor(FormaTheme.ochreGold)
                        .font(.system(size: 14))

                    Text("\(streakCount) DAY STREAK")
                        .font(.system(size: 11, weight: .bold, design: .monospaced))
                        .foregroundColor(FormaTheme.ochreGold)
                }

                Text(progress >= 1.0 && totalCount > 0 ? "Sanctuary in Harmony" : "\(completedCount) of \(totalCount) rituals complete")
                    .font(.system(size: 15, weight: .medium, design: .serif))
                    .foregroundColor(FormaTheme.textPrimary)

                Text(progressSubtitle)
                    .font(.system(size: 12))
                    .foregroundColor(FormaTheme.textMuted)
            }

            Spacer()
        }
        .padding(16)
        .background(FormaTheme.surface)
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(FormaTheme.border, lineWidth: 1)
        )
    }

    private var progressSubtitle: String {
        if totalCount == 0 {
            return "No rituals scheduled for today"
        } else if progress == 0 {
            return "Begin with your first mindful breath"
        } else if progress < 0.5 {
            return "Quiet momentum taking root"
        } else if progress < 1.0 {
            return "Almost complete — stay present"
        } else {
            return "All commitments honored today"
        }
    }
}
