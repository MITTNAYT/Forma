import SwiftUI

public struct NotionHeatmapView: View {
    let completions: [HabitCompletion]
    let daysToDisplay: Int

    public init(completions: [HabitCompletion], daysToDisplay: Int = 70) { // 10 weeks of 7 days
        self.completions = completions
        self.daysToDisplay = daysToDisplay
    }

    private var completionDatesCount: [String: Int] {
        var counts: [String: Int] = [:]
        for c in completions {
            counts[c.date, default: 0] += 1
        }
        return counts
    }

    private var pastDays: [Date] {
        let calendar = Calendar.current
        let today = Date()
        var days: [Date] = []
        for i in (0..<daysToDisplay).reversed() {
            if let date = calendar.date(byAdding: .day, value: -i, to: today) {
                days.append(date)
            }
        }
        return days
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("CONSISTENCY HEATMAP")
                    .font(.system(size: 11, weight: .bold, design: .monospaced))
                    .foregroundColor(FormaTheme.accent)
                Spacer()
                Text("Last 10 Weeks")
                    .font(.system(size: 11))
                    .foregroundColor(FormaTheme.textMuted)
            }

            // Grid of 7 rows (days of week) x 10 columns (weeks)
            let columns = Array(repeating: GridItem(.fixed(14), spacing: 4), count: 10)
            
            LazyHGrid(rows: Array(repeating: GridItem(.fixed(14), spacing: 4), count: 7), spacing: 4) {
                ForEach(pastDays, id: \.self) { date in
                    let dateStr = DateUtils.formatDate(date)
                    let count = completionDatesCount[dateStr] ?? 0
                    heatmapCell(count: count)
                }
            }
            .frame(height: 126)

            // Legend
            HStack(spacing: 6) {
                Text("Less")
                    .font(.system(size: 10))
                    .foregroundColor(FormaTheme.textMuted)

                ForEach(0..<4) { level in
                    cellColor(for: level)
                        .frame(width: 10, height: 10)
                        .cornerRadius(2)
                }

                Text("More")
                    .font(.system(size: 10))
                    .foregroundColor(FormaTheme.textMuted)
                
                Spacer()
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

    private func heatmapCell(count: Int) -> some View {
        let level: Int
        if count == 0 {
            level = 0
        } else if count == 1 {
            level = 1
        } else if count <= 3 {
            level = 2
        } else {
            level = 3
        }

        return RoundedRectangle(cornerRadius: 3)
            .fill(cellColor(for: level))
            .frame(width: 14, height: 14)
    }

    private func cellColor(for level: Int) -> Color {
        switch level {
        case 0: return FormaTheme.surfaceElevated
        case 1: return FormaTheme.matchaGreen.opacity(0.4)
        case 2: return FormaTheme.matchaGreen.opacity(0.7)
        default: return FormaTheme.matchaGreen
        }
    }
}
