package com.habitflow.app.core.designsystem.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EnergySavingsLeaf
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.NotionTheme
import kotlinx.coroutines.delay

@Composable
fun PremiumSplashScreen(
    onAnimationFinished: () -> Unit
) {
    val colors = NotionTheme.colors

    val mainAlpha = remember { Animatable(0f) }
    val coreScale = remember { Animatable(0.2f) }
    val rippleScale = remember { Animatable(0.4f) }
    val rippleAlpha = remember { Animatable(0.8f) }
    var showSubtitle by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Phase 1: Precision Fade In & Leaf Emblem Spring (0 -> 450ms)
        mainAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing)
        )
        coreScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )

        // Phase 2: Organic Blossom Ripple Expansion
        rippleScale.animateTo(
            targetValue = 1.28f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        rippleAlpha.animateTo(
            targetValue = 0.4f,
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
        )

        showSubtitle = true
        // Mindful breathing pause for elegant presence
        delay(600)

        // Seamless handoff to App Crossfade (no blank black/white flash)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .alpha(mainAlpha.value),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Refined Blossom Habit Leaf Emblem Stack
            Box(
                modifier = Modifier.size(112.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Ambient Blossom Ripple
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .scale(rippleScale.value)
                        .clip(CircleShape)
                        .background(colors.accentSoft.copy(alpha = rippleAlpha.value))
                )

                // Secondary Soft Glow Ring
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .scale(coreScale.value)
                        .clip(CircleShape)
                        .background(colors.accentSoft)
                )

                // Inner Solid Emblem Core
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .scale(coreScale.value)
                        .clip(CircleShape)
                        .background(colors.accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EnergySavingsLeaf,
                        contentDescription = "HabitFlow Leaf Logo",
                        tint = colors.onAccent,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Satellite Accent Dot
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .scale(coreScale.value)
                        .clip(CircleShape)
                        .background(if (colors.isDark) colors.surfaceVariant else colors.accent)
                        .border(2.5.dp, colors.background, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Professional Brand Typography
            Text(
                text = "HABITFLOW",
                style = NotionTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                letterSpacing = 4.5.sp,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = showSubtitle,
                enter = fadeIn(tween(450)) + slideInVertically(
                    initialOffsetY = { 16 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "KEEP THE LOOP GOING",
                        style = NotionTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        letterSpacing = 1.8.sp,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
