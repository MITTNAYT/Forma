import SwiftUI

@main
struct FormaApp: App {
    @StateObject private var habitsViewModel = HabitsViewModel()

    var body: some Scene {
        WindowGroup {
            MainTabView()
                .environmentObject(habitsViewModel)
                .preferredColorScheme(.dark)
        }
    }
}
