package com.habitflow.app.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.splash.PremiumSplashScreen
import com.habitflow.app.ui.navigation.HabitFlowBottomBar
import com.habitflow.app.ui.navigation.HabitFlowNavGraph

import androidx.compose.runtime.collectAsState
import com.habitflow.app.domain.repository.UserPreferencesRepository
import com.habitflow.app.ui.onboarding.OnboardingScreen

@Composable
fun HabitFlowApp(
    preferencesRepository: UserPreferencesRepository? = null,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    var isSplashFinished by remember { mutableStateOf(false) }
    val isOnboardingCompleted by (preferencesRepository?.isOnboardingCompleted?.collectAsState(initial = false)
        ?: remember { mutableStateOf(true) })

    Crossfade(
        targetState = isSplashFinished to isOnboardingCompleted,
        animationSpec = tween(400),
        label = "app_state_crossfade"
    ) { (finishedSplash, onboardingDone) ->
        if (!finishedSplash) {
            PremiumSplashScreen(
                onAnimationFinished = { isSplashFinished = true }
            )
        } else if (!onboardingDone) {
            OnboardingScreen(
                onOnboardingFinished = { /* StateFlow updates automatically */ }
            )
        } else {
            // True Edge-to-Edge Full Screen Container
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(NotionTheme.colors.background)
            ) {
                // Navigation Screen Host (fills the full display)
                HabitFlowNavGraph(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )

                // Floating Navigation Dock (pinned gracefully at the bottom)
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    HabitFlowBottomBar(navController = navController)
                }
            }
        }
    }
}
