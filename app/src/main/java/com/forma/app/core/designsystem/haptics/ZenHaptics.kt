package com.forma.app.core.designsystem.haptics

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * 3-Tier Contextual Zen Haptics Palette:
 * - Light Whisper Tick: For time selectors, duration pills (+15m, +30m), sliders.
 * - Matcha Snap: For checking off habit rings, completing timeline tasks.
 * - Zen Gong Heavy Pulse: For finishing a Pomodoro focus cycle or hitting milestone streaks.
 */
class ZenHaptics(
    private val context: Context,
    private val composeHaptics: HapticFeedback
) {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Subtle, light tick for micro-interactions (e.g. time adjustments, chip selection).
     */
    fun lightTick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            composeHaptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    /**
     * Satisfying, crisp snap for completing a habit or confirming an action.
     */
    fun matchaSnap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            composeHaptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    /**
     * Resonant, deeper pulse for timer completions and mindful celebrations.
     */
    fun zenGongPulse() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else {
            composeHaptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}

@Composable
fun rememberZenHaptics(): ZenHaptics {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    return remember(context, haptics) {
        ZenHaptics(context.applicationContext, haptics)
    }
}
