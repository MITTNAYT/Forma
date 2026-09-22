package com.habitflow.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.habitflow.app.ui.focus.FocusTimerScreen
import com.habitflow.app.ui.habits.HabitsScreen
import com.habitflow.app.ui.pomodoro.PomodoroScreen
import com.habitflow.app.ui.settings.SettingsScreen
import com.habitflow.app.ui.stats.StatsScreen
import com.habitflow.app.ui.timeline.AddEditTimelineItemScreen
import com.habitflow.app.ui.today.TodayScreen

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FormaNavGraph(
    navController: NavHostController,
    onOpenAddSheet: ((selectedDate: String) -> Unit)? = null,
    onOpenEditSheet: ((itemId: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Tab order for directional slide calculation
    val tabRoutes = listOf(
        Screen.Home.route,
        Screen.Pomodoro.route,
        Screen.AddItem.route,
        Screen.Analysis.route,
        Screen.Settings.route
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(180, easing = LinearOutSlowInEasing)) +
            scaleIn(initialScale = 0.985f, animationSpec = tween(180, easing = FastOutSlowInEasing))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(140, easing = FastOutSlowInEasing)) +
            scaleOut(targetScale = 1.01f, animationSpec = tween(140, easing = FastOutSlowInEasing))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(160, easing = LinearOutSlowInEasing)) +
            scaleIn(initialScale = 0.985f, animationSpec = tween(160, easing = FastOutSlowInEasing))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(120, easing = FastOutSlowInEasing))
        }
    ) {
        // Tab 1: Home (Forma Minimal Dashboard)
        composable(Screen.Home.route) {
            TodayScreen(
                onNavigateToAddTask = { dateStr ->
                    if (onOpenAddSheet != null) {
                        onOpenAddSheet(dateStr)
                    } else {
                        navController.navigate(Screen.AddEditTimelineItem.createRoute(selectedDate = dateStr))
                    }
                },
                onNavigateToEditTask = { itemId ->
                    if (onOpenEditSheet != null) {
                        onOpenEditSheet(itemId)
                    } else {
                        navController.navigate(Screen.AddEditTimelineItem.createRoute(itemId = itemId))
                    }
                },
                onNavigateToHabits = {
                    navController.navigate(Screen.Habits.route)
                },
                onNavigateToFocusTimer = { itemId, title, durationMinutes, isHabit ->
                    navController.navigate(Screen.FocusTimer.createRoute(itemId, title, durationMinutes, isHabit))
                }
            )
        }

        // Tab 2: Habits (Dedicated Recurring Rituals Library)
        composable(Screen.Habits.route) {
            HabitsScreen()
        }

        // Pomodoro (Dedicated Mindful Focus & Flow Clock Section)
        composable(Screen.Pomodoro.route) {
            PomodoroScreen()
        }

        // Tab 4: Analysis / Stats (Streaks & Monthly Consistency Grid)
        composable(Screen.Analysis.route) {
            StatsScreen()
        }

        // Tab 3: Add Item (Creation Modal)
        composable(
            route = Screen.AddItem.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it / 8 },
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(180, easing = LinearOutSlowInEasing))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it / 8 },
                    animationSpec = tween(180, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing))
            }
        ) {
            AddEditTimelineItemScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Tab 5: Settings (Account, Preferences & Palettes)
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToHabits = {
                    navController.navigate(Screen.Habits.route)
                },
                onNavigateToStats = {
                    navController.navigate(Screen.Analysis.route)
                }
            )
        }

        // Modal / Sub-screen: Add / Edit Task or Habit
        composable(
            route = Screen.AddEditTimelineItem.route,
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("selectedDate") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("isInbox") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            ),
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it / 8 },
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(180, easing = LinearOutSlowInEasing))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it / 8 },
                    animationSpec = tween(180, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing))
            }
        ) {
            AddEditTimelineItemScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Modal / Sub-screen: Pomodoro Focus Clock Screen for a specific task
        composable(
            route = Screen.FocusTimer.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType },
                navArgument("title") {
                    type = NavType.StringType
                    defaultValue = "Mindful Focus"
                },
                navArgument("duration") {
                    type = NavType.StringType
                    defaultValue = "25"
                },
                navArgument("isHabit") {
                    type = NavType.StringType
                    defaultValue = "false"
                }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(240, easing = LinearOutSlowInEasing)) +
                scaleIn(initialScale = 0.96f, animationSpec = tween(240, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                scaleOut(targetScale = 0.96f, animationSpec = tween(180, easing = FastOutSlowInEasing))
            }
        ) {
            FocusTimerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun HabitFlowNavGraph(
    navController: NavHostController,
    onOpenAddSheet: ((selectedDate: String) -> Unit)? = null,
    onOpenEditSheet: ((itemId: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    FormaNavGraph(
        navController = navController,
        onOpenAddSheet = onOpenAddSheet,
        onOpenEditSheet = onOpenEditSheet,
        modifier = modifier
    )
}
