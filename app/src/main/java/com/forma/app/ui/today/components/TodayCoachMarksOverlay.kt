package com.forma.app.ui.today.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.component.FormaButton
import com.forma.app.core.designsystem.component.FormaButtonStyle
import com.forma.app.core.designsystem.motion.FormaMotion
import com.forma.app.core.designsystem.motion.formaPressEffect

data class CoachMarkStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
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
    val colors = FormaTheme.colors
    val density = LocalDensity.current

    val steps = remember(weekStripBounds, ritualPillBounds, aiPlanBounds, addButtonBounds) {
        listOf(
            CoachMarkStep(
                title = "Week Schedule Strip",
                description = "Glide horizontally across days. Tap any date to inspect past consistency or plan your upcoming rhythm without friction.",
                icon = Icons.Rounded.CalendarToday,
                targetBounds = weekStripBounds,
                isBottomAligned = false
            ),
            CoachMarkStep(
                title = "Daily Mindful Rituals",
                description = "Tap the checkmark to complete with tactile spring feedback. Tap the card body to inspect checklist micro-steps.",
                icon = Icons.Rounded.Spa,
                targetBounds = ritualPillBounds,
                isBottomAligned = false
            ),
            CoachMarkStep(
                title = "AI Day Synthesizer",
                description = "Build a calm, balanced day with AI that respects your energy curve, chronotype, and personal focus blocks.",
                icon = Icons.Rounded.AutoAwesome,
                targetBounds = aiPlanBounds,
                isBottomAligned = false
            ),
            CoachMarkStep(
                title = "Intentional Time-Blocker",
                description = "Create custom habits, schedule tasks, or draft actionable micro-steps anytime with a single tap.",
                icon = Icons.Rounded.Add,
                targetBounds = addButtonBounds,
                isBottomAligned = true
            )
        )
    }

    val currentStep = steps.getOrNull(currentStepIndex) ?: steps.first()
    val totalSteps = steps.size

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
        // Dark 65% Alpha Backdrop with Cutout Spotlight
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (currentStepIndex < totalSteps - 1) {
                        currentStepIndex++
                    } else {
                        onDismiss()
                    }
                }
        ) {
            // Draw dark 65% overlay
            drawRect(color = Color.Black.copy(alpha = 0.65f))

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
                    color = colors.accent.copy(alpha = 0.75f * (1f - pulseAnim.value * 0.35f)),
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
                    .background(Color.Black.copy(alpha = 0.35f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .formaPressEffect(targetScale = 0.94f) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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

        // Animated Spotlight Tooltip Card (Theme-Adaptive & Spring Easing)
        AnimatedContent(
            targetState = currentStepIndex,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220)) + slideInVertically(
                    animationSpec = FormaMotion.snappyOffset,
                    initialOffsetY = { 16.dp.value.toInt() }
                )).togetherWith(
                    fadeOut(animationSpec = tween(150)) + slideOutVertically(
                        animationSpec = FormaMotion.snappyOffset,
                        targetOffsetY = { -10.dp.value.toInt() }
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
                        Modifier.padding(start = 20.dp, end = 20.dp, bottom = 100.dp)
                    } else {
                        // Position card directly below target bounds if present
                        val topOffsetDp = currentStep.targetBounds?.let { bounds ->
                            with(density) { (bounds.bottom + 16.dp.toPx()).toDp() }
                        } ?: 170.dp
                        Modifier.padding(top = topOffsetDp, start = 20.dp, end = 20.dp)
                    }
                ),
            label = "coach_mark_step_anim"
        ) { stepIndex ->
            val step = steps[stepIndex]

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = Color.Black.copy(alpha = 0.35f),
                        spotColor = Color.Black.copy(alpha = 0.45f)
                    )
                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                color = colors.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Step Counter & Header with Pro Icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.accentSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = step.icon,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = step.title,
                                style = FormaTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 16.sp
                            )
                        }

                        // Step Indicator Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.accentSoft)
                                .padding(horizontal = 9.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${stepIndex + 1} of $totalSteps",
                                style = FormaTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = step.description,
                        style = FormaTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        lineHeight = 18.sp,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Bottom Row: Back + Step Dots + "Next" Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous button if step > 0
                        if (stepIndex > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.surfaceVariant)
                                    .formaPressEffect(targetScale = 0.94f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        currentStepIndex--
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = "Back",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Back",
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textSecondary,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        } else {
                            // Dot indicators
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                repeat(totalSteps) { dotIdx ->
                                    val isCurrent = dotIdx == stepIndex
                                    Box(
                                        modifier = Modifier
                                            .size(if (isCurrent) 16.dp else 6.dp, 6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isCurrent) colors.accent
                                                else colors.border
                                            )
                                    )
                                }
                            }
                        }

                        // Next / Start action button
                        FormaButton(
                            text = if (stepIndex == totalSteps - 1) "Enter Sanctuary" else "Got it →",
                            style = FormaButtonStyle.PRIMARY,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
