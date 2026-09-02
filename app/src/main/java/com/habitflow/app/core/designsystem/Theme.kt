package com.habitflow.app.core.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.habitflow.app.domain.repository.DarkModeOption
import com.habitflow.app.domain.repository.PaletteFamily
import com.habitflow.app.domain.repository.ThemeMode

val LocalNotionColors = staticCompositionLocalOf { MatchaLightColorScheme }

object NotionTheme {
    val colors: NotionColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalNotionColors.current

    val typography = HabitFlowTypography
    val shapes = HabitFlowShapes
}

@Composable
fun HabitFlowTheme(
    paletteFamily: PaletteFamily = PaletteFamily.MATCHA_OAT,
    darkModeOption: DarkModeOption = DarkModeOption.LIGHT,
    themeMode: ThemeMode = ThemeMode.MATCHA_OAT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val isDark = when (darkModeOption) {
        DarkModeOption.DARK -> true
        DarkModeOption.LIGHT -> false
        DarkModeOption.SYSTEM -> darkTheme
    }

    val activeColors = when (paletteFamily) {
        PaletteFamily.MATCHA_OAT -> if (isDark) MatchaDarkColorScheme else MatchaLightColorScheme
        PaletteFamily.COFFEE_CREAM, PaletteFamily.WALNUT_ESPRESSO -> if (isDark) CoffeeDarkColorScheme else CoffeeLightColorScheme
        PaletteFamily.MONOCHROME -> if (isDark) MonoDarkColorScheme else MonoLightColorScheme
        PaletteFamily.TERRACOTTA_SAND -> if (isDark) TerracottaDarkColorScheme else TerracottaLightColorScheme
        PaletteFamily.LAVENDER_MILK -> if (isDark) LavenderDarkColorScheme else LavenderLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = activeColors.background.toArgb()
                window.navigationBarColor = activeColors.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalNotionColors provides activeColors
    ) {
        MaterialTheme(
            typography = HabitFlowTypography,
            shapes = HabitFlowShapes,
            content = content
        )
    }
}
