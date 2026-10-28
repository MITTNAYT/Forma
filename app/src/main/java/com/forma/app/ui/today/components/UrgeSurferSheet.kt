package com.forma.app.ui.today.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.motion.formaPressEffect
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrgeSurferSheet(
    habitName: String,
    onDismiss: () -> Unit,
    onDeflect: () -> Unit
) {
    val colors = FormaTheme.colors
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var secondsLeft by remember { mutableIntStateOf(90) }
    var isCompleted by remember { mutableStateOf(false) }

    // 4-phase box breath cycle (16 seconds total: 4s inhale, 4s hold, 4s exhale, 4s rest)
    val cycleSecond = (90 - secondsLeft) % 16
    val breathPhase = when {
        cycleSecond < 4 -> "Inhale"
        cycleSecond < 8 -> "Hold"
        cycleSecond < 12 -> "Exhale"
        else -> "Rest"
    }

    val breathTargetScale = when {
        cycleSecond < 4 -> 1.25f
        cycleSecond < 8 -> 1.25f
        cycleSecond < 12 -> 0.90f
        else -> 0.90f
    }

    val animatedScale by animateFloatAsState(
        targetValue = breathTargetScale,
        animationSpec = tween(durationMillis = 3800, easing = FastOutSlowInEasing),
        label = "breath_scale"
    )

    // Scientific guidance based on urge surfing timeline
    val guidancePrompt = when {
        secondsLeft > 70 -> "Acknowledge the impulse without judging it. It is just a sensation."
        secondsLeft > 45 -> "Notice where the urge lives in your body. Breathe directly into it."
        secondsLeft > 20 -> "The craving wave has crested and is beginning to dissolve."
        secondsLeft > 0 -> "You are riding it out. Freedom is found in this stillness."
        else -> "Urge deflected. You observed the wave and stood firm."
    }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
            if (secondsLeft % 16 == 0 || secondsLeft % 16 == 4 || secondsLeft % 16 == 8 || secondsLeft % 16 == 12) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
        isCompleted = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "URGE SURFER",
                            style = FormaTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            letterSpacing = 1.2.sp,
                            fontSize = 10.sp
                        )
                        Text(
                            text = habitName,
                            style = FormaTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 15.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Central Breathing Ring
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(animatedScale),
                contentAlignment = Alignment.Center
            ) {
                val progress = (90f - secondsLeft.toFloat()) / 90f
                Canvas(modifier = Modifier.size(200.dp)) {
                    val strokeWidth = 8.dp.toPx()
                    // Track
                    drawCircle(
                        color = colors.surfaceVariant,
                        style = Stroke(width = strokeWidth)
                    )
                    // Progress
                    drawArc(
                        color = colors.accent,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${secondsLeft}s",
                        style = FormaTheme.typography.headlineLarge.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 36.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = breathPhase.uppercase(),
                        style = FormaTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Scientific Anchor Quote Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = guidancePrompt,
                    style = FormaTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Deflect Urge Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.accent)
                    .formaPressEffect(targetScale = 0.97f) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDeflect()
                        onDismiss()
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Rounded.Check else Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = colors.onAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isCompleted) "Urge Deflected (+1)" else "Complete Deflection (+1)",
                        style = FormaTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onAccent,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
