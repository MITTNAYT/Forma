package com.forma.app.core.designsystem.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.component.FormaEmblem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * PremiumSplashScreen – Forma's living sculptural opening sequence.
 *
 * Rendered in the serene Matcha & Oat theme (or active theme).
 * Features the physical layered concentric emblem assembly sequence,
 * followed by the FORMA wordmark and mindful tagline. Tap anywhere to skip.
 */
@Composable
fun PremiumSplashScreen(
    onAnimationFinished: () -> Unit
) {
    val colors = FormaTheme.colors
    val haptic = LocalHapticFeedback.current

    // Wordmark + tagline animation values
    val wordmarkAlpha   = remember { Animatable(0f) }
    val wordmarkY       = remember { Animatable(14f) }
    val tracking        = remember { Animatable(12f) }
    val taglineAlpha    = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Subtle haptic cue as the Bauhaus F settles and ripple radiates
        delay(980)
        try {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (_: Exception) {}

        delay(80)

        // FORMA wordmark rises
        launch { wordmarkAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing)) }
        launch {
            wordmarkY.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow)
            )
        }
        launch {
            tracking.animateTo(
                targetValue = 6f,
                animationSpec = tween(600, easing = CubicBezierEasing(0.2f, 0f, 0.2f, 1f))
            )
        }

        // Tagline soft fade
        delay(350)
        taglineAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))

        // Linger, then proceed to app
        delay(2000)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onAnimationFinished() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Sculptural Tactile Concentric Logo Assembly
            FormaEmblem(
                size = 136.dp,
                animated = true,
                glyphColor = colors.accent,
                auraColor = colors.accentSoft,
                discBaseColor = colors.surface,
                pearlColor = colors.onAccent
            )

            Spacer(modifier = Modifier.height(36.dp))

            // FORMA Wordmark & Slogan (Pure Bauhaus Typography — No Lines or Dashes)
            Box(
                modifier = Modifier
                    .alpha(wordmarkAlpha.value)
                    .offset(y = wordmarkY.value.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FORMA",
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary,
                        letterSpacing = tracking.value.sp,
                        fontSize = 28.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ARCHITECTURE OF HABIT",
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent,
                        letterSpacing = 2.4.sp,
                        fontSize = 11.sp,
                        modifier = Modifier.alpha(taglineAlpha.value)
                    )
                }
            }
        }

        // Version micro-label at bottom
        Text(
            text = "2 . 0",
            fontWeight = FontWeight.Light,
            color = colors.textPrimary.copy(alpha = 0.25f),
            letterSpacing = 4.sp,
            fontSize = 9.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(taglineAlpha.value)
        )
    }
}
