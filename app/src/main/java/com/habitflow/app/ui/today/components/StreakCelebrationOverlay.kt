package com.habitflow.app.ui.today.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.habitflow.app.core.designsystem.FormaTheme
import kotlinx.coroutines.delay

@Composable
fun StreakCelebrationOverlay(
    streakCount: Int,
    habitName: String,
    onDismiss: () -> Unit
) {
    val colors = FormaTheme.colors

    val cardScale = remember { Animatable(0.4f) }
    val rippleScale = remember { Animatable(0.5f) }
    val rippleAlpha = remember { Animatable(0.8f) }

    val infiniteTransition = rememberInfiniteTransition(label = "celebration_orbit")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )

    LaunchedEffect(Unit) {
        cardScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
        rippleScale.animateTo(
            targetValue = 1.4f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        rippleAlpha.animateTo(
            targetValue = 0.2f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )

        // Auto dismiss after 2.8 seconds
        delay(2600)
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { onDismiss() }
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Elevated Modal Card
            Box(
                modifier = Modifier
                    .scale(cardScale.value)
                    .clip(RoundedCornerShape(32.dp))
                    .background(colors.surface)
                    .border(1.5.dp, colors.accent.copy(alpha = 0.4f), RoundedCornerShape(32.dp))
                    .clickable(enabled = false) {}
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Floating Luminous Fire & Lotus Emblem
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Expanding Ring
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .scale(rippleScale.value)
                                .clip(CircleShape)
                                .background(colors.accentSoft.copy(alpha = rippleAlpha.value))
                        )

                        // Rotating Dashed Orbit Ring
                        Canvas(
                            modifier = Modifier
                                .size(96.dp)
                                .rotate(orbitAngle)
                        ) {
                            drawCircle(
                                color = colors.accent,
                                radius = size.minDimension / 2f,
                                style = Stroke(
                                    width = 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            )
                        }

                        // Inner Solid Orb
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(colors.accent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = "Streak Fire",
                                tint = colors.onAccent,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Streak Badge Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.accentSoft)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Spa,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${streakCount.coerceAtLeast(1)} DAYS IN RHYTHM",
                                style = FormaTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent,
                                letterSpacing = 1.2.sp,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Momentum Flowing!",
                        style = FormaTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "You've honored your commitment to '$habitName'. Mindful progress compounds daily.",
                        style = FormaTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Continue Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.accent)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Keep The Loop Going",
                            style = FormaTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onAccent,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
