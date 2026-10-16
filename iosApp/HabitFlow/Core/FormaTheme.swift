import SwiftUI

extension Color {
    public init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

public struct FormaTheme {
    // Backgrounds & Surfaces (Espresso Dark Palette)
    public static let background = Color(hex: "#14100D")
    public static let surface = Color(hex: "#1E1813")
    public static let surfaceElevated = Color(hex: "#29211B")
    public static let border = Color(hex: "#3D3128")
    
    // Accents & Typography
    public static let accent = Color(hex: "#D4BFA8") // Warm Creamy Alabaster
    public static let accentSoft = Color(hex: "#2E241E")
    public static let textPrimary = Color(hex: "#FAF3EB")
    public static let textSecondary = Color(hex: "#C7B5A3")
    public static let textMuted = Color(hex: "#7A685B")
    
    // Signature Habit Palette
    public static let matchaGreen = Color(hex: "#4E6542")
    public static let terracotta = Color(hex: "#C85A32")
    public static let ochreGold = Color(hex: "#D99B26")
    public static let mistBlue = Color(hex: "#4D7799")
    public static let wisteriaPurple = Color(hex: "#7D6B90")
    public static let roseQuartz = Color(hex: "#B85D75")
    
    public static let habitPalette = [
        "#4E6542", // Matcha Green
        "#C85A32", // Terracotta
        "#D99B26", // Ochre Gold
        "#4D7799", // Mist Blue
        "#7D6B90", // Wisteria Purple
        "#B85D75", // Rose Quartz
        "#8C7869", // Zen Cocoa
        "#3B7A57"  // Deep Sage
    ]
}
