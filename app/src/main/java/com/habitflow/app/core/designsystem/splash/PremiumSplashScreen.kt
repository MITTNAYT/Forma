package com.habitflow.app.core.designsystem.splash

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.component.FormaEmblem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * PremiumSplashScreen – Forma's living opening sequence.
 *
 * Rendered in the serene Matcha & Oat theme (or user's active theme).
 * The FormaEmblem plays its full luminous-draw + heartbeat + breathing-aura sequence,
 * then the FORMA wordmark slides up with staggered letter-tracking, and finally
 * the tagline fades in. Tap anywhere to skip.
 */
@Composable
fun PremiumSplashScreen(
    onAnimationFinished: () -> Unit
) {
    val colors = NotionTheme.colors

    // Wordmark + tagline animation values
    val wordmarkAlpha   = remember { Animatable(0f) }
    val wordmarkY       = remember { Animatable(14f) }
    val tracking        = remember { Animatable(12f) }
    val taglineAlpha    = remember { Animatable(0f) }
    val dividerAlpha    = remember { Animatable(0f) }
    val dividerWidth    = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Emblem animation runs for ~1700 ms (draw 1200 + bloom & heartbeat)
        // Wordmark appears in sync with the bloom
        delay(1300)

        // Divider hairline sweeps in
        launch { dividerAlpha.animateTo(1f, tween(300)) }
        launch {
            dividerWidth.animateTo(
                targetValue = 48f,
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            )
        }
        delay(150)

        // HABITFLOW wordmark rises
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
        delay(400)
        taglineAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))

        // Linger, then dismiss
        delay(2200)
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

            // Animated Forma Emblem (owns its own bloom → draw → heartbeat → ring sequence)
            FormaEmblem(
                size = 128.dp,
                animated = true,
                glyphColor = colors.accent,
                auraColor = colors.accentSoft,
                pearlColor = colors.surface
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Precision hairline divider
            Box(
                modifier = Modifier
                    .width(dividerWidth.value.dp)
                    .height(1.dp)
                    .clip(RoundedCornerShape(0.5.dp))
                    .background(colors.accent.copy(alpha = 0.45f * dividerAlpha.value))
            )

            Spacer(modifier = Modifier.height(18.dp))

            // FORMA Zen Wordmark
            Box(
                modifier = Modifier
                    .alpha(wordmarkAlpha.value)
                    .offset(y = wordmarkY.value.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FORMA",
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        letterSpacing = tracking.value.sp,
                        fontSize = 26.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tagline row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.alpha(taglineAlpha.value)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(0.8.dp)
                                .clip(RoundedCornerShape(0.4.dp))
                                .background(colors.accent.copy(alpha = 0.65f))
                        )
                        Spacer(modifier = Modifier.width(9.dp))
                        Text(
                            text = "GIVE FORM TO YOUR DAYS",
                            fontWeight = FontWeight.Medium,
                            color = colors.accent,
                            letterSpacing = 2.4.sp,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.width(9.dp))
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(0.8.dp)
                                .clip(RoundedCornerShape(0.4.dp))
                                .background(colors.accent.copy(alpha = 0.65f))
                        )
                    }
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
