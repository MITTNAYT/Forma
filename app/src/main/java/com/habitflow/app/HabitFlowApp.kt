package com.habitflow.app.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.splash.PremiumSplashScreen
import com.habitflow.app.ui.navigation.HabitFlowBottomBar
import com.habitflow.app.ui.navigation.HabitFlowNavGraph

@Composable
fun HabitFlowApp(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    var isSplashFinished by remember { mutableStateOf(false) }

    Crossfade(
        targetState = isSplashFinished,
        animationSpec = tween(400),
        label = "splash_crossfade"
    ) { finished ->
        if (!finished) {
            PremiumSplashScreen(
                onAnimationFinished = { isSplashFinished = true }
            )
        } else {
            Scaffold(
                bottomBar = {
                    HabitFlowBottomBar(navController = navController)
                },
                containerColor = NotionTheme.colors.background,
                modifier = modifier
                    .fillMaxSize()
                    .background(NotionTheme.colors.background)
            ) { innerPadding ->
                HabitFlowNavGraph(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
