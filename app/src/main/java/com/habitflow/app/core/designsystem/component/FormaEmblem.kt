package com.habitflow.app.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.habitflow.app.core.designsystem.NotionTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * FormaEmblem: The iconic Architectural Glyphic "F" mark of Forma.
 * A serene, minimalist sculptural monogram consisting of an architectural vertical pillar,
 * two floating precision horizontal horizon beams at golden ratio proportions,
 * an illuminated focal pearl, and an ambient aura.
 */
@Composable
fun FormaEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 108.dp,
    animated: Boolean = false,
    glyphColor: Color = NotionTheme.colors.accent,
    auraColor: Color = NotionTheme.colors.accentSoft,
    pearlColor: Color = NotionTheme.colors.onAccent
) {
    val auraScale = remember { Animatable(if (animated) 0.5f else 1.25f) }
    val auraAlpha = remember { Animatable(if (animated) 0f else 0.45f) }
    val pillarProgress = remember { Animatable(if (animated) 0f else 1f) }
    val topBeamProgress = remember { Animatable(if (animated) 0f else 1f) }
    val midBeamProgress = remember { Animatable(if (animated) 0f else 1f) }
    val pearlScale = remember { Animatable(if (animated) 0f else 1f) }
    val gleamProgress = remember { Animatable(if (animated) 0f else 1f) }

    if (animated) {
        LaunchedEffect(Unit) {
            // 1. Soft Breathing Aura
            launch {
                auraAlpha.animateTo(0.65f, tween(300))
                auraScale.animateTo(
                    targetValue = 1.30f,
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow)
                )
                auraAlpha.animateTo(0.40f, tween(400))
            }

            // 2. Vertical Pillar Foundation (Grows from top to bottom)
            launch {
                pillarProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.62f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }

            // 3. Top Horizon Beam Extends
            launch {
                delay(180)
                topBeamProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(340, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
                )
            }

            // 4. Middle Crossbar Extends (Golden Ratio Stagger)
            launch {
                delay(260)
                midBeamProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(320, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
                )
            }

            // 5. Focal Accent Pearl Pop
            launch {
                delay(380)
                pearlScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.45f,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }

            // 6. Diagonal Micro-Gleam Sweep
            launch {
                delay(460)
                gleamProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(480, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Ambient Soft Aura Glow
        Box(
            modifier = Modifier
                .size(size * 0.90f)
                .scale(auraScale.value)
                .clip(CircleShape)
                .background(auraColor.copy(alpha = auraAlpha.value))
        )

        // Architectural Glyph Canvas
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Layout Metrics
            val beamThickness = w * 0.17f
            val beamCornerRadius = beamThickness / 2f
            val leftMargin = w * 0.22f
            val topMargin = h * 0.18f

            // 1. Vertical Spine Pillar: from leftMargin, spans topMargin to bottom
            val totalPillarHeight = h * 0.64f
            val currentPillarHeight = totalPillarHeight * pillarProgress.value
            if (pillarProgress.value > 0f) {
                drawRoundRect(
                    color = glyphColor,
                    topLeft = Offset(leftMargin, topMargin),
                    size = Size(beamThickness, currentPillarHeight),
                    cornerRadius = CornerRadius(beamCornerRadius, beamCornerRadius)
                )
            }

            // 2. Top Horizon Beam: spans rightwards from leftMargin
            val totalTopBeamWidth = w * 0.58f
            val currentTopBeamWidth = totalTopBeamWidth * topBeamProgress.value
            if (topBeamProgress.value > 0f) {
                drawRoundRect(
                    color = glyphColor,
                    topLeft = Offset(leftMargin, topMargin),
                    size = Size(currentTopBeamWidth, beamThickness),
                    cornerRadius = CornerRadius(beamCornerRadius, beamCornerRadius)
                )
            }

            // 3. Middle Crossbar Beam: spans rightwards at golden ratio (~68% of top beam)
            val midSlotY = topMargin + (h * 0.25f)
            val totalMidBeamWidth = w * 0.40f
            val currentMidBeamWidth = totalMidBeamWidth * midBeamProgress.value
            if (midBeamProgress.value > 0f) {
                drawRoundRect(
                    color = glyphColor,
                    topLeft = Offset(leftMargin, midSlotY),
                    size = Size(currentMidBeamWidth, beamThickness * 0.88f),
                    cornerRadius = CornerRadius(beamCornerRadius, beamCornerRadius)
                )
            }

            // 4. Diagonal Micro-Gleam Sweep across the glyph
            if (animated && gleamProgress.value > 0f && gleamProgress.value < 1f) {
                clipRect(0f, 0f, w, h) {
                    val sweepOffset = (w + h) * gleamProgress.value - (h * 0.4f)
                    val gleamWidth = w * 0.30f
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            start = Offset(sweepOffset - gleamWidth, 0f),
                            end = Offset(sweepOffset + gleamWidth, h)
                        ),
                        start = Offset(sweepOffset - gleamWidth, 0f),
                        end = Offset(sweepOffset + gleamWidth, h),
                        strokeWidth = gleamWidth
                    )
                }
            }

            // 5. Focal Accent Pearl at Top-Right Tip
            if (pearlScale.value > 0f) {
                val pearlCenter = Offset(leftMargin + totalTopBeamWidth - (beamThickness * 0.55f), topMargin + (beamThickness * 0.50f))
                val baseRadius = beamThickness * 0.32f
                val currentRadius = baseRadius * pearlScale.value

                // Outer Halo
                drawCircle(
                    color = pearlColor.copy(alpha = 0.35f),
                    radius = currentRadius * 1.6f,
                    center = pearlCenter
                )

                // Solid Pearl Core
                drawCircle(
                    color = pearlColor,
                    radius = currentRadius,
                    center = pearlCenter
                )
            }
        }
    }
}
