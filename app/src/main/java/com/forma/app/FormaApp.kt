package com.forma.app.ui

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
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.splash.PremiumSplashScreen
import com.forma.app.ui.navigation.FormaBottomBar
import com.forma.app.ui.navigation.FormaNavGraph

import androidx.compose.runtime.collectAsState
import com.forma.app.domain.repository.UserPreferencesRepository
import com.forma.app.ui.onboarding.OnboardingScreen

@Composable
fun FormaApp(
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
            var showAddEditSheet by remember { mutableStateOf(false) }
            var activeEditItemId by remember { mutableStateOf<String?>(null) }
            var activeSelectedDate by remember { mutableStateOf<String?>(null) }

            // True Edge-to-Edge Full Screen Container
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(FormaTheme.colors.background)
            ) {
                // Navigation Screen Host (fills the full display and stays visible)
                FormaNavGraph(
                    navController = navController,
                    onOpenAddSheet = { dateStr ->
                        activeEditItemId = null
                        activeSelectedDate = dateStr
                        showAddEditSheet = true
                    },
                    onOpenEditSheet = { itemId ->
                        activeEditItemId = itemId
                        activeSelectedDate = null
                        showAddEditSheet = true
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Floating Navigation Dock (pinned gracefully at the bottom)
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    FormaBottomBar(
                        navController = navController,
                        onAddClick = {
                            activeEditItemId = null
                            activeSelectedDate = null
                            showAddEditSheet = true
                        }
                    )
                }

                // In-Place Floating Add/Edit Bottom Sheet (glides over current page without blanking/white background)
                if (showAddEditSheet) {
                    com.forma.app.ui.timeline.components.AddEditTimelineSheet(
                        itemId = activeEditItemId,
                        selectedDate = activeSelectedDate,
                        onDismiss = {
                            showAddEditSheet = false
                            activeEditItemId = null
                            activeSelectedDate = null
                        }
                    )
                }
            }
        }
    }
}
