import SwiftUI

public struct MainTabView: View {
    @State private var selectedTab: Int = 0

    public init() {
        // Customize UITabBar appearance for iOS 15 dark espresso theme
        let appearance = UITabBarAppearance()
        appearance.configureWithOpaqueBackground()
        appearance.backgroundColor = UIColor(FormaTheme.surface)
        
        // Unselected icon/text color
        appearance.stackedLayoutAppearance.normal.iconColor = UIColor(FormaTheme.textMuted)
        appearance.stackedLayoutAppearance.normal.titleTextAttributes = [
            .foregroundColor: UIColor(FormaTheme.textMuted)
        ]
        
        // Selected icon/text color
        appearance.stackedLayoutAppearance.selected.iconColor = UIColor(FormaTheme.accent)
        appearance.stackedLayoutAppearance.selected.titleTextAttributes = [
            .foregroundColor: UIColor(FormaTheme.accent)
        ]

        UITabBar.appearance().standardAppearance = appearance
        if #available(iOS 15.0, *) {
            UITabBar.appearance().scrollEdgeAppearance = appearance
        }
    }

    public var body: some View {
        TabView(selection: $selectedTab) {
            TodayView()
                .tabItem {
                    Label("Today", systemImage: "sparkles")
                }
                .tag(0)

            HabitsListView()
                .tabItem {
                    Label("Rituals", systemImage: "target")
                }
                .tag(1)

            StatsView()
                .tabItem {
                    Label("Insights", systemImage: "chart.bar.xaxis")
                }
                .tag(2)

            SettingsView()
                .tabItem {
                    Label("Sanctuary", systemImage: "gearshape.fill")
                }
                .tag(3)
        }
        .accentColor(FormaTheme.accent)
    }
}
