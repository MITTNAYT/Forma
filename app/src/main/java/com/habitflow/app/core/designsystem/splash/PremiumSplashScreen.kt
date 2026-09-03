package com.habitflow.app.core.designsystem.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.component.FormaEmblem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * PremiumSplashScreen: The minimalist, refined opening animation for FORMA.
 * Features an architectural geometric glyph reveal, hairline accent anchor,
 * tracked editorial typography, and seamless tap-to-skip.
 */
@Composable
fun PremiumSplashScreen(
    onAnimationFinished: () -> Unit
) {
    val colors = NotionTheme.colors

    val screenAlpha = remember { Animatable(0f) }
    val emblemScale = remember { Animatable(0.7f) }
    val dividerWidth = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textY = remember { Animatable(16f) }
    val tracking = remember { Animatable(9f) }
    val subtitleAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1. Serene Screen Fade-In
        launch {
            screenAlpha.animateTo(1f, tween(240, easing = LinearOutSlowInEasing))
        }

        // 2. Emblem Reveal
        launch {
            emblemScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }

        // 3. Hairline Divider Expansion
        launch {
            delay(420)
            dividerWidth.animateTo(
                targetValue = 38f,
                animationSpec = tween(360, easing = FastOutSlowInEasing)
            )
        }

        // 4. Luxury Editorial Typography Reveal
        launch {
            delay(460)
            launch { textAlpha.animateTo(1f, tween(300)) }
            launch { textY.animateTo(0f, spring(dampingRatio = 0.65f)) }
            launch {
                tracking.animateTo(
                    targetValue = 6f,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            }
        }

        // 5. Tagline Soft Reveal
        launch {
            delay(640)
            subtitleAlpha.animateTo(1f, tween(320))
        }

        // 6. Serene Mindful Pause & Transition
        delay(2500)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .alpha(screenAlpha.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Tap anywhere to skip straight into the app
                onAnimationFinished()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Sculptural Architectural Monogram
            Box(modifier = Modifier.scale(emblemScale.value)) {
                FormaEmblem(
                    size = 120.dp,
                    animated = true,
                    glyphColor = colors.accent,
                    auraColor = colors.accentSoft,
                    pearlColor = colors.onAccent
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Precision Hairline Accent Bar
            Box(
                modifier = Modifier
                    .width(dividerWidth.value.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(colors.accent.copy(alpha = 0.40f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bold Architectural Typography: F O R M A
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .offset(y = textY.value.dp)
            ) {
                Text(
                    text = "FORMA",
                    style = NotionTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    letterSpacing = tracking.value.sp,
                    fontSize = 25.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.alpha(subtitleAlpha.value)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GIVE FORM TO YOUR DAYS",
                        style = NotionTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        letterSpacing = 2.4.sp,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
