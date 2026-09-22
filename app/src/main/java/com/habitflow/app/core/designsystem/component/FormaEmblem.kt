package com.habitflow.app.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.habitflow.app.core.designsystem.FormaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * FormaEmblem – The Zen Ensō of Forma.
 *
 * Inspired by the Japanese Ensō (円相) — an organic, mindful circular stroke
 * representing presence, tranquility, and the beauty of continuous daily ritual.
 * Its open aperture symbolizes room for growth, flow, and the infinite horizon.
 */
@Composable
fun FormaEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 108.dp,
    animated: Boolean = false,
    glyphColor: Color = FormaTheme.colors.accent,
    auraColor: Color = FormaTheme.colors.accentSoft,
    pearlColor: Color = FormaTheme.colors.onAccent
) {
    // ── Continuous Zen Breathing Aura ──────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "zen_breathe")
    val auraBreath by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraBreath"
    )
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraAlpha"
    )
    val centerPebblePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pebblePulse"
    )

    // ── Intro Animation Sequence ───────────────────────────────────
    val drawProgress = remember { Animatable(if (animated) 0f else 1f) }
    val brushTipAlpha = remember { Animatable(if (animated) 0f else 0f) }
    val rippleRadius = remember { Animatable(0f) }
    val rippleAlpha = remember { Animatable(0f) }
    val settleScale = remember { Animatable(1f) }
    val glyphAlpha = remember { Animatable(if (animated) 0f else 1f) }

    if (animated) {
        LaunchedEffect(Unit) {
            launch { glyphAlpha.animateTo(1f, tween(180)) }

            // 1. Mindful Ensō brush sweep
            launch {
                brushTipAlpha.animateTo(1f, tween(200))
                drawProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(1350, easing = CubicBezierEasing(0.25f, 0.1f, 0.15f, 1f))
                )
                brushTipAlpha.animateTo(0f, tween(250))
            }

            // 2. Zen water ripple radiates from center when stroke finishes
            delay(1300)
            launch {
                rippleAlpha.animateTo(0.55f, tween(120))
                launch {
                    rippleRadius.animateTo(1f, tween(750, easing = CubicBezierEasing(0.15f, 0f, 0.25f, 1f)))
                }
                delay(250)
                rippleAlpha.animateTo(0f, tween(500))
            }

            // 3. Gentle settling inertia
            launch {
                settleScale.animateTo(
                    targetValue = 1.035f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                )
                settleScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                )
            }
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f

            val strokeWidth = w * 0.098f
            val ss = settleScale.value
            val ga = glyphAlpha.value

            // ── 1. Serene Breathing Ambient Aura ───────────────────────
            val auraR = w * 0.46f * auraBreath
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        auraColor.copy(alpha = auraAlpha * ga),
                        auraColor.copy(alpha = 0f)
                    ),
                    center = Offset(cx, cy),
                    radius = auraR
                ),
                radius = auraR,
                center = Offset(cx, cy)
            )

            // ── 2. Expanding Zen Water Ripple ──────────────────────────
            if (rippleAlpha.value > 0f) {
                val rr = w * 0.50f * rippleRadius.value
                drawCircle(
                    color = glyphColor.copy(alpha = rippleAlpha.value * 0.5f),
                    radius = rr.coerceAtLeast(1f),
                    center = Offset(cx, cy),
                    style = Stroke(width = strokeWidth * 0.16f)
                )
            }

            withTransform({
                scale(ss, ss, Offset(cx, cy))
            }) {
                // ── 3. Inner Pale Jade Disc (Plateau) ──────────────────
                val innerRadius = w * 0.218f
                drawCircle(
                    color = Color(0xFFEDF3EB).copy(alpha = ga),
                    radius = innerRadius,
                    center = Offset(cx, cy)
                )
                // Delicate Contour Ring
                drawCircle(
                    color = Color(0xFFA0B39A).copy(alpha = 0.85f * ga),
                    radius = innerRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = strokeWidth * 0.12f)
                )

                // ── 4. Zen Ensō Arc Path (Open Circular Brushstroke) ────
                val rx = w * 0.273f
                val ry = h * 0.273f

                val ensoPath = Path().apply {
                    arcTo(
                        rect = Rect(
                            left = cx - rx,
                            top = cy - ry,
                            right = cx + rx,
                            bottom = cy + ry
                        ),
                        startAngleDegrees = 42f,
                        sweepAngleDegrees = 320f,
                        forceMoveTo = true
                    )
                }

                val pathMeasure = PathMeasure()
                pathMeasure.setPath(ensoPath, false)
                val totalLength = pathMeasure.length
                val drawLength = totalLength * drawProgress.value

                if (drawLength > 0f) {
                    val currentSegment = Path()
                    pathMeasure.getSegment(0f, drawLength, currentSegment, true)

                    // Draw the Ensō stroke
                    drawPath(
                        path = currentSegment,
                        color = glyphColor.copy(alpha = ga),
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Luminous leading pen/brush tip
                    if (brushTipAlpha.value > 0f && drawLength < totalLength) {
                        val pos = pathMeasure.getPosition(drawLength)
                        drawZenTip(
                            center = pos,
                            strokeWidth = strokeWidth,
                            glyphColor = glyphColor,
                            pearlColor = pearlColor,
                            alpha = brushTipAlpha.value
                        )
                    }
                }

                // ── 5. Balanced Center Target Ring ─────────────────────
                val targetRadius = (w * 0.039f) * centerPebblePulse
                // Fill center with oat background tone
                drawCircle(
                    color = Color(0xFFF7F8F4).copy(alpha = ga),
                    radius = targetRadius,
                    center = Offset(cx, cy)
                )
                // Matcha Outer Ring
                drawCircle(
                    color = glyphColor.copy(alpha = ga),
                    radius = targetRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = strokeWidth * 0.15f)
                )
            }
        }
    }
}

/** Draws the mindful luminous tip leading the Ensō stroke. */
private fun DrawScope.drawZenTip(
    center: Offset,
    strokeWidth: Float,
    glyphColor: Color,
    pearlColor: Color,
    alpha: Float
) {
    val r = strokeWidth * 0.55f
    // Outer halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                glyphColor.copy(alpha = alpha * 0.5f),
                Color.Transparent
            ),
            center = center,
            radius = r * 2.8f
        ),
        radius = r * 2.8f,
        center = center
    )
    // Inner droplet core
    drawCircle(
        color = pearlColor.copy(alpha = alpha),
        radius = r * 0.75f,
        center = center
    )
    // Ring definition
    drawCircle(
        color = glyphColor.copy(alpha = alpha),
        radius = r * 0.75f,
        center = center,
        style = Stroke(width = strokeWidth * 0.12f)
    )
}
