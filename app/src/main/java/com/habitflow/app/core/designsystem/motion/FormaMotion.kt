package com.habitflow.app.core.designsystem.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Emil Kowalski & Apple Design fluid motion system for Forma.
 *
 * Principles:
 * 1. Immediate tactile feedback on touch-down (scale 0.94 - 0.97).
 * 2. 120Hz GPU-accelerated graphicsLayer render passes (zero recomposition on transforms).
 * 3. Critically damped springs (damping 1.0) for modal expansions; subtle bounce (0.76) for physical releases.
 * 4. Staggered item entrances capped at 0.04s-0.08s for effortless, non-sluggish fluid load.
 */
object FormaMotion {
    /** Snappy, tactile spring for buttons, FABs, and action chips */
    val snappyFloat = spring<Float>(
        dampingRatio = 0.76f,
        stiffness = 550f
    )

    /** Bouncy physical response for checkmarks, badges, and toggle bursts */
    val bouncyFloat = spring<Float>(
        dampingRatio = 0.68f,
        stiffness = 600f
    )

    /** Critically damped spring for dialogs, bottom sheets, and expansion cards (zero overshoot) */
    val settleFloat = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 420f
    )

    /** Gentle spring for subtle continuous transitions */
    val gentleFloat = spring<Float>(
        dampingRatio = 0.88f,
        stiffness = 320f
    )

    /** Gliding spring for segmented pill controls and tab indicators */
    val slidingPill = spring<Float>(
        dampingRatio = 0.82f,
        stiffness = 520f
    )

    val snappyDp = spring<Dp>(
        dampingRatio = 0.78f,
        stiffness = 500f
    )

    val snappyOffset = spring<IntOffset>(
        dampingRatio = 0.78f,
        stiffness = 500f
    )

    /** Apple iOS standard fluid gesture bezier */
    val appleFluidEasing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

    /** Smooth natural deceleration curve */
    val naturalDecelerate = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
}

/**
 * Alias for backward compatibility
 */
val FormaSprings = FormaMotion

/**
 * Instant touch-down press feedback modifier.
 * Runs completely in the GPU graphicsLayer pass without triggering layout recalculations.
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
        animationSpec = FormaMotion.snappyFloat,
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

/**
 * Staggered, GPU-accelerated item entrance animation.
 * Smoothly cascades list cards into view with a gentle spring translation and opacity fade.
 */
@Composable
fun Modifier.formaStaggeredEntrance(
    index: Int,
    baseDelayMs: Int = 30,
    maxDelayMs: Int = 240
): Modifier {
    val alphaAnim = remember { Animatable(0f) }
    val translationYAnim = remember { Animatable(16f) }

    val delayTime = (index * baseDelayMs).coerceAtMost(maxDelayMs).toLong()

    LaunchedEffect(Unit) {
        if (delayTime > 0) delay(delayTime)
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 220, easing = FormaMotion.naturalDecelerate)
        )
    }

    LaunchedEffect(Unit) {
        if (delayTime > 0) delay(delayTime)
        translationYAnim.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = 0.82f,
                stiffness = 400f
            )
        )
    }

    return this.graphicsLayer {
        alpha = alphaAnim.value
        translationY = translationYAnim.value * density
    }
}
