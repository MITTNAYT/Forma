package com.habitflow.app.core.designsystem

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.habitflow.app.domain.repository.DarkModeOption
import com.habitflow.app.domain.repository.PaletteFamily
import com.habitflow.app.domain.repository.ThemeMode

val LocalFormaColors = staticCompositionLocalOf { MatchaLightColorScheme }
val LocalNotionColors = LocalFormaColors

object FormaTheme {
    val colors: FormaColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalFormaColors.current

    val typography = FormaTypography
    val shapes = FormaShapes
}

object NotionTheme {
    val colors: FormaColorScheme
        @Composable
        @ReadOnlyComposable
        get() = FormaTheme.colors

    val typography = FormaTheme.typography
    val shapes = FormaTheme.shapes
}

@Composable
fun animatedStructuredColorScheme(target: StructuredColorScheme): StructuredColorScheme {
    val animSpec = tween<Color>(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )

    val background by animateColorAsState(target.background, animSpec, label = "th_bg")
    val surface by animateColorAsState(target.surface, animSpec, label = "th_surf")
    val surfaceVariant by animateColorAsState(target.surfaceVariant, animSpec, label = "th_surfv")
    val border by animateColorAsState(target.border, animSpec, label = "th_bord")
    val textPrimary by animateColorAsState(target.textPrimary, animSpec, label = "th_txtp")
    val textSecondary by animateColorAsState(target.textSecondary, animSpec, label = "th_txts")
    val textTertiary by animateColorAsState(target.textTertiary, animSpec, label = "th_txtt")
    val accent by animateColorAsState(target.accent, animSpec, label = "th_acc")
    val accentMuted by animateColorAsState(target.accentMuted, animSpec, label = "th_accm")
    val accentSoft by animateColorAsState(target.accentSoft, animSpec, label = "th_accs")
    val onAccent by animateColorAsState(target.onAccent, animSpec, label = "th_onacc")
    val timelineLine by animateColorAsState(target.timelineLine, animSpec, label = "th_time")

    return StructuredColorScheme(
        background = background,
        surface = surface,
        surfaceVariant = surfaceVariant,
        border = border,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        textTertiary = textTertiary,
        accent = accent,
        accentMuted = accentMuted,
        accentSoft = accentSoft,
        onAccent = onAccent,
        timelineLine = timelineLine,
        isDark = target.isDark
    )
}

@Composable
fun FormaTheme(
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

    val targetColors = when (paletteFamily) {
        PaletteFamily.MATCHA_OAT -> if (isDark) MatchaDarkColorScheme else MatchaLightColorScheme
        PaletteFamily.COFFEE_CREAM, PaletteFamily.WALNUT_ESPRESSO -> if (isDark) CoffeeDarkColorScheme else CoffeeLightColorScheme
        PaletteFamily.MONOCHROME -> if (isDark) MonoDarkColorScheme else MonoLightColorScheme
        PaletteFamily.TERRACOTTA_SAND -> if (isDark) TerracottaDarkColorScheme else TerracottaLightColorScheme
        PaletteFamily.LAVENDER_MILK -> if (isDark) LavenderDarkColorScheme else LavenderLightColorScheme
    }

    // Coordinated 300ms color transition provider (eliminates snapping, white flashes & flicker)
    val animatedColors = animatedStructuredColorScheme(targetColors)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = animatedColors.background.toArgb()
                window.navigationBarColor = animatedColors.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalFormaColors provides animatedColors
    ) {
        MaterialTheme(
            typography = FormaTypography,
            shapes = FormaShapes,
            content = content
        )
    }
}

@Composable
fun HabitFlowTheme(
    paletteFamily: PaletteFamily = PaletteFamily.MATCHA_OAT,
    darkModeOption: DarkModeOption = DarkModeOption.LIGHT,
    themeMode: ThemeMode = ThemeMode.MATCHA_OAT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    FormaTheme(
        paletteFamily = paletteFamily,
        darkModeOption = darkModeOption,
        themeMode = themeMode,
        darkTheme = darkTheme,
        content = content
    )
}
