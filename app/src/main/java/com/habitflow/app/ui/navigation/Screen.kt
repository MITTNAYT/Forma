package com.habitflow.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import java.net.URLEncoder

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    object Home : Screen("home", "Home", Icons.Rounded.Home)
    object Habits : Screen("habits", "Habits", Icons.Rounded.TaskAlt)
    object Pomodoro : Screen("pomodoro", "Pomodoro", Icons.Rounded.Timer)
    object AddItem : Screen("add_item", "Add", Icons.Rounded.Add)
    object Analysis : Screen("analysis", "Analysis", Icons.Rounded.BarChart)
    object Settings : Screen("settings", "Settings", Icons.Rounded.Settings)

    // Sub-screens & Modals
    object AddEditTimelineItem : Screen("add_edit_timeline?itemId={itemId}&selectedDate={selectedDate}&isInbox={isInbox}", "Commitment") {
        fun createRoute(itemId: String? = null, selectedDate: String? = null, isInbox: Boolean = false): String {
            val params = mutableListOf<String>()
            itemId?.let { params.add("itemId=$it") }
            selectedDate?.let { params.add("selectedDate=$it") }
            if (isInbox) params.add("isInbox=true")
            return if (params.isNotEmpty()) "add_edit_timeline?${params.joinToString("&")}" else "add_edit_timeline"
        }
    }

    object FocusTimer : Screen("focus_timer/{itemId}?title={title}&duration={duration}&isHabit={isHabit}", "Focus Flow") {
        fun createRoute(itemId: String, title: String, durationMinutes: Int = 25, isHabit: Boolean = false): String {
            val encodedTitle = try { URLEncoder.encode(title, "UTF-8") } catch (_: Exception) { title }
            return "focus_timer/$itemId?title=$encodedTitle&duration=$durationMinutes&isHabit=$isHabit"
        }
    }

    companion object {
        // Strict 5 slots: Home -> Habits -> AddItem (+) -> Analysis -> Settings
        val bottomNavItems: List<Screen>
            get() = listOf(Home, Habits, AddItem, Analysis, Settings)
    }
}
