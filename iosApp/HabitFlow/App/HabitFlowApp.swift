import SwiftUI

@main
struct HabitFlowApp: App {
    @StateObject private var habitsViewModel = HabitsViewModel()

    var body: some Scene {
        WindowGroup {
            MainTabView()
                .environmentObject(habitsViewModel)
                .preferredColorScheme(.dark)
        }
    }
}
