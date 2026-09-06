package com.habitflow.app.core.designsystem.motion

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Emil Kowalski & Apple Design fluid motion parameters for Forma.
 *
 * Principles:
 * 1. Buttons and pressables MUST provide immediate feedback on touch-down (scale 0.94 - 0.97).
 * 2. Critically damped springs (damping 1.0) for standard transitions; subtle bounce (0.78) for physical releases.
 * 3. Never animate from scale(0) — always start from scale(0.94) + opacity(0).
 */
object FormaSprings {
    /** Snappy, tactile response for buttons, FABs, and action chips */
    val snappy = spring<Float>(
        dampingRatio = 0.78f,
        stiffness = 450f
    )

    /** Critically damped spring for dialogs, bottom sheets, and expansion cards (zero overshoot) */
    val settle = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 380f
    )

    /** Gliding spring for segmented pill controls and tab indicators */
    val slidingPill = spring<Float>(
        dampingRatio = 0.82f,
        stiffness = 500f
    )
}

/**
 * Instant touch-down press feedback modifier.
 * Applies continuous scale down on finger contact and spring release on lift.
 */
@Composable
fun Modifier.formaPressEffect(
    targetScale: Float = 0.96f,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) targetScale else 1f,
        animationSpec = spring(
            dampingRatio = 0.76f,
            stiffness = 500f
        ),
        label = "forma_press_scale"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled
                ) {
                    onClick()
                }
            } else {
                Modifier
            }
        )
}
