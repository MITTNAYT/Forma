package com.forma.app.ui.settings.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.motion.formaPressEffect

private data class TourStep(
    val stepNumber: String,
    val badge: String,
    val title: String,
    val headline: String,
    val description: String,
    val highlight: String,
    val icon: ImageVector,
    val iconColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanctuaryWalkthroughSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = FormaTheme.colors
    val haptic = LocalHapticFeedback.current

    var currentStepIndex by remember { mutableIntStateOf(0) }

    val steps = remember {
        listOf(
            TourStep(
                stepNumber = "01 / 05",
                badge = "DAY TIMELINE",
                title = "Chronological Clarity",
                headline = "A tranquil timeline, not an endless to-do list.",
                description = "Forma organizes your day in harmony with your natural energy rhythm. Timebox intentional focus blocks and anchor keystone rituals to morning, afternoon, or evening.",
                highlight = "Tactile spring checkmarks give instant, satisfying tactile feedback with zero clutter.",
                icon = Icons.Rounded.Schedule,
                iconColor = Color(0xFFC58A24) // Terracotta Gold
            ),
            TourStep(
                stepNumber = "02 / 05",
                badge = "MICRO-STEPS & CUES",
                title = "Atomic Habit Architecture",
                headline = "Shrink resistance down to two minutes.",
                description = "Big ambitions fail when starting friction is overwhelming. Attach micro-step checklists to any ritual and anchor new habits onto existing cues in your environment.",
                highlight = "Check off individual steps directly from the Today card without opening nested menus.",
                icon = Icons.Rounded.Layers,
                iconColor = Color(0xFF4A7C59) // Sage Matcha
            ),
            TourStep(
                stepNumber = "03 / 05",
                badge = "WINTERING SANCTUARY",
                title = "Guilt-Free Rest Days",
                headline = "Life is cyclical, not mechanical.",
                description = "When you are unwell, traveling, or taking an intentional Sabbath, toggle Wintering Mode. Your streaks freeze safely, preserving your hard-earned momentum without guilt or anxiety.",
                highlight = "Forma never shames you. Rest is an essential pillar of long-term consistency.",
                icon = Icons.Rounded.AcUnit,
                iconColor = Color(0xFF5E548E) // Soft Lavender
            ),
            TourStep(
                stepNumber = "04 / 05",
                badge = "AI STUDIO",
                title = "Gemini Routine Synthesizer",
                headline = "From vague ambition to structured blueprint.",
                description = "Open the AI Studio anytime. Describe your upcoming day or long-term goals in simple words, and Gemini constructs an optimized timeline with timeboxes, checklists, and habits.",
                highlight = "One-tap curated presets allow you to plan focus sprints or reset days in seconds.",
                icon = Icons.Rounded.AutoAwesome,
                iconColor = Color(0xFFD4AF37) // Warm Gold
            ),
            TourStep(
                stepNumber = "05 / 05",
                badge = "SOVEREIGN PRIVACY",
                title = "Local Offline Sanctuary",
                headline = "Your private life stays on your glass.",
                description = "Forma operates entirely offline-first with an encrypted Room database. There are no tracking pixels, ad networks, or data brokers. Your reflections belong to you.",
                highlight = "Protect your sanctuary with biometric fingerprint/face lock and private vault export.",
                icon = Icons.Rounded.Shield,
                iconColor = Color(0xFF2C221E) // Espresso Obsidian
            )
        )
    }

    val currentStep = steps[currentStepIndex]
    val isLastStep = currentStepIndex == steps.size - 1

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.border)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 8.dp)
        ) {
            // Header Row: Step counter & close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.accent.copy(alpha = 0.14f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = currentStep.badge,
                            style = FormaTheme.typography.labelSmall.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                letterSpacing = 1.2.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentStep.stepNumber,
                        style = FormaTheme.typography.labelSmall.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ),
                        color = colors.textTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDismiss()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step Progress Track (5 segments)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                steps.indices.forEach { index ->
                    val isActive = index <= currentStepIndex
                    val segmentColor by animateColorAsState(
                        targetValue = if (isActive) colors.accent else colors.surfaceVariant,
                        label = "progress_segment"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(segmentColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Animated Slide Content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState.stepNumber > initialState.stepNumber) {
                        (slideInHorizontally { width -> width / 3 } + fadeIn(tween(250)))
                            .togetherWith(slideOutHorizontally { width -> -width / 3 } + fadeOut(tween(200)))
                    } else {
                        (slideInHorizontally { width -> -width / 3 } + fadeIn(tween(250)))
                            .togetherWith(slideOutHorizontally { width -> width / 3 } + fadeOut(tween(200)))
                    }
                },
                label = "tour_slide"
            ) { step ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Monumental Icon Medallion
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(step.iconColor.copy(alpha = 0.12f))
                            .border(1.5.dp, step.iconColor.copy(alpha = 0.35f), RoundedCornerShape(26.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = null,
                            tint = step.iconColor,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = step.title,
                        style = FormaTheme.typography.titleMedium.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            letterSpacing = (-0.3).sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = step.headline,
                        style = FormaTheme.typography.bodyMedium.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            lineHeight = 20.sp
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent,
                        fontSize = 13.5.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = step.description,
                        style = FormaTheme.typography.bodyMedium.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            lineHeight = 21.sp
                        ),
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Highlight callout
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surfaceVariant.copy(alpha = 0.6f))
                            .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = step.highlight,
                                style = FormaTheme.typography.bodySmall.copy(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeight = 16.sp
                                ),
                                color = colors.textPrimary,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Navigation Row (Previous & Next/Finish)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStepIndex > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surfaceVariant)
                            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .formaPressEffect(targetScale = 0.94f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentStepIndex--
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Previous",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Previous",
                                style = FormaTheme.typography.labelSmall.copy(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                ),
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.accent)
                        .formaPressEffect(targetScale = 0.94f) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (isLastStep) {
                                onDismiss()
                            } else {
                                currentStepIndex++
                            }
                        }
                        .padding(horizontal = 22.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isLastStep) "Enter Sanctuary" else "Continue",
                            style = FormaTheme.typography.labelSmall.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            fontWeight = FontWeight.Bold,
                            color = colors.onAccent,
                            fontSize = 12.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (isLastStep) Icons.Rounded.Check else Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = colors.onAccent,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
