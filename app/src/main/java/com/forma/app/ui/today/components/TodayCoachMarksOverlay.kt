package com.forma.app.ui.today.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.component.FormaButton
import com.forma.app.core.designsystem.component.FormaButtonStyle
import com.forma.app.core.designsystem.motion.FormaMotion
import kotlin.math.roundToInt

data class CoachMarkStep(
    val title: String,
    val description: String,
    val targetBounds: Rect?,
    val isBottomAligned: Boolean = false
)

@Composable
fun TodayCoachMarksOverlay(
    weekStripBounds: Rect?,
    ritualPillBounds: Rect?,
    aiPlanBounds: Rect?,
    addButtonBounds: Rect?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var currentStepIndex by remember { mutableIntStateOf(0) }

    val steps = remember(weekStripBounds, ritualPillBounds, aiPlanBounds, addButtonBounds) {
        listOf(
            CoachMarkStep(
                title = "Week Schedule Strip",
                description = "Tap any day to see your schedule",
                targetBounds = weekStripBounds,
                isBottomAligned = false
            ),
            CoachMarkStep(
                title = "Morning Clarity Ritual",
                description = "Start your day with a guided ritual",
                targetBounds = ritualPillBounds,
                isBottomAligned = false
            ),
            CoachMarkStep(
                title = "AI Day Planner",
                description = "Let AI build your perfect day",
                targetBounds = aiPlanBounds,
                isBottomAligned = false
            ),
            CoachMarkStep(
                title = "Create New Intention",
                description = "Add your first habit or task to get started",
                targetBounds = addButtonBounds,
                isBottomAligned = true
            )
        )
    }

    val currentStep = steps.getOrNull(currentStepIndex) ?: steps.first()
    val totalSteps = steps.size
    val colors = FormaTheme.colors
    val density = LocalDensity.current

    // Pulsing highlight ring animation
    val pulseAnim = remember { Animatable(0f) }
    LaunchedEffect(currentStepIndex) {
        pulseAnim.snapTo(0f)
        pulseAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    ) {
        // Dark 60% Alpha Backdrop with Cutout Spotlight
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // Tap outside advances step
                    if (currentStepIndex < totalSteps - 1) {
                        currentStepIndex++
                    } else {
                        onDismiss()
                    }
                }
        ) {
            // Draw dark 60% overlay
            drawRect(color = Color.Black.copy(alpha = 0.60f))

            // Cutout target highlight
            currentStep.targetBounds?.let { rawBounds ->
                val paddingPx = 8.dp.toPx()
                val left = (rawBounds.left - paddingPx).coerceAtLeast(8f)
                val top = (rawBounds.top - paddingPx).coerceAtLeast(8f)
                val right = (rawBounds.right + paddingPx).coerceAtMost(size.width - 8f)
                val bottom = (rawBounds.bottom + paddingPx).coerceAtMost(size.height - 8f)
                val cornerRadiusPx = 18.dp.toPx()

                // BlendMode.Clear cuts out the spotlight area
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    blendMode = BlendMode.Clear
                )

                // Outer accent glow ring
                val expandPx = pulseAnim.value * 4.dp.toPx()
                drawRoundRect(
                    color = Color(0xFF6B8A5E).copy(alpha = 0.65f * (1f - pulseAnim.value * 0.4f)),
                    topLeft = Offset(left - expandPx, top - expandPx),
                    size = Size(right - left + expandPx * 2, bottom - top + expandPx * 2),
                    cornerRadius = CornerRadius(cornerRadiusPx + expandPx, cornerRadiusPx + expandPx),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Top Bar Action: "Skip tour" ghost button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        onDismiss()
                    }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "Skip tour",
                    style = FormaTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        // Animated Spotlight Tooltip Card (250ms fade + 12dp vertical slide with spring)
        AnimatedContent(
            targetState = currentStepIndex,
            transitionSpec = {
                (fadeIn(animationSpec = tween(250)) + slideInVertically(
                    animationSpec = FormaMotion.snappyOffset,
                    initialOffsetY = { 12.dp.value.toInt() }
                )).togetherWith(
                    fadeOut(animationSpec = tween(150)) + slideOutVertically(
                        animationSpec = FormaMotion.snappyOffset,
                        targetOffsetY = { -8.dp.value.toInt() }
                    )
                )
            },
            modifier = Modifier
                .align(
                    if (currentStep.isBottomAligned) Alignment.BottomCenter
                    else Alignment.TopCenter
                )
                .then(
                    if (currentStep.isBottomAligned) {
                        Modifier.padding(start = 24.dp, end = 24.dp, bottom = 100.dp)
                    } else {
                        // Position card directly below target bounds if present
                        val topOffsetDp = currentStep.targetBounds?.let { bounds ->
                            with(density) { (bounds.bottom + 16.dp.toPx()).toDp() }
                        } ?: 180.dp
                        Modifier.padding(top = topOffsetDp, start = 24.dp, end = 24.dp)
                    }
                ),
            label = "coach_mark_step_anim"
        ) { stepIndex ->
            val step = steps[stepIndex]

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 380.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color.Black.copy(alpha = 0.25f),
                        spotColor = Color.Black.copy(alpha = 0.35f)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Step Counter & Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = step.title,
                            style = FormaTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E231B),
                            fontSize = 17.sp
                        )

                        // Step Indicator Pill (e.g. 1/4)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF6B8A5E).copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${stepIndex + 1} of $totalSteps",
                                style = FormaTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4A683E),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = step.description,
                        style = FormaTheme.typography.bodySmall,
                        color = Color(0xFF5A6255),
                        lineHeight = 19.sp,
                        fontSize = 13.5.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Bottom Row: Step Dots + "Got it →" Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dot indicators
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            repeat(totalSteps) { dotIdx ->
                                val isCurrent = dotIdx == stepIndex
                                Box(
                                    modifier = Modifier
                                        .size(if (isCurrent) 16.dp else 6.dp, 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isCurrent) Color(0xFF4A683E)
                                            else Color(0xFFD6DEC9)
                                        )
                                )
                            }
                        }

                        // Soft pill action button
                        FormaButton(
                            text = if (stepIndex == totalSteps - 1) "Get Started →" else "Got it →",
                            style = FormaButtonStyle.SOFT_PILL,
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                if (stepIndex < totalSteps - 1) {
                                    currentStepIndex++
                                } else {
                                    onDismiss()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
