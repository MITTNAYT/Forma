import Foundation

public struct DateUtils {
    private static let isoFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = TimeZone.current
        return formatter
    }()

    private static let displayFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .none
        return formatter
    }()

    public static func todayString() -> String {
        return formatDate(Date())
    }

    public static func formatDate(_ date: Date) -> String {
        return isoFormatter.string(from: date)
    }

    public static func parseDate(_ dateString: String) -> Date? {
        return isoFormatter.date(from: dateString)
    }

    public static func formatDisplayDate(_ date: Date) -> String {
        return displayFormatter.string(from: date)
    }

    public static func daysBetween(start: Date, end: Date) -> Int {
        let calendar = Calendar.current
        let startDay = calendar.startOfDay(for: start)
        let endDay = calendar.startOfDay(for: end)
        let components = calendar.dateComponents([.day], from: startDay, to: endDay)
        return components.day ?? 0
    }

    public static func addDays(to date: Date, days: Int) -> Date {
        return Calendar.current.date(byAdding: .day, value: days, to: date) ?? date
    }

    public static func getIsoWeekday(for date: Date) -> Int {
        let weekday = Calendar.current.component(.weekday, from: date)
        // 1=Sun -> 7, 2=Mon -> 1, ... 7=Sat -> 6
        return (weekday == 1) ? 7 : (weekday - 1)
    }
}
