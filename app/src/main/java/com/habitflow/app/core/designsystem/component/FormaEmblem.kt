package com.habitflow.app.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
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
 * FormaEmblem – The Physical Tactile Concentric Zen Logo of Forma.
 *
 * Modeled after the sculptural layered concentric relief emblem:
 * 1. Elevated Base Disc (Warm Off-White with soft cast shadow)
 * 2. Outer Sculptural Olive Arc (~280° sweep with rounded caps)
 * 3. Elevated Middle Disc (Clean White raised plateau with cast shadow)
 * 4. Concentric Middle Olive Ring
 * 5. Center Solid Olive Core Dot
 */
@Composable
fun FormaEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 108.dp,
    animated: Boolean = false,
    glyphColor: Color = FormaTheme.colors.accent,
    auraColor: Color = FormaTheme.colors.accentSoft,
    discBaseColor: Color = FormaTheme.colors.surface,
    pearlColor: Color = FormaTheme.colors.onAccent
) {
    // ── Continuous Zen Breathing Aura ──────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "zen_breathe")
    val auraBreath by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraBreath"
    )
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraAlpha"
    )
    val centerPebblePulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pebblePulse"
    )

    // ── Opening Animation Sequence ─────────────────────────────────
    val discScale = remember { Animatable(if (animated) 0.82f else 1f) }
    val discAlpha = remember { Animatable(if (animated) 0f else 1f) }
    val centerDotScale = remember { Animatable(if (animated) 0f else 1f) }
    val middleRingScale = remember { Animatable(if (animated) 0f else 1f) }
    val arcDrawProgress = remember { Animatable(if (animated) 0f else 1f) }
    val arcTipAlpha = remember { Animatable(if (animated) 0f else 0f) }
    val rippleRadius = remember { Animatable(0f) }
    val rippleAlpha = remember { Animatable(0f) }
    val settleScale = remember { Animatable(1f) }

    if (animated) {
        LaunchedEffect(Unit) {
            // 1. Base discs rise with soft spring
            launch {
                discAlpha.animateTo(1f, tween(350))
            }
            launch {
                discScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }

            // 2. Center dot blossoms
            delay(150)
            launch {
                centerDotScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }

            // 3. Middle ring blossoms
            delay(120)
            launch {
                middleRingScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }

            // 4. Outer sculptural arc sweeps around circumference
            delay(180)
            launch {
                arcTipAlpha.animateTo(1f, tween(180))
                arcDrawProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 1150,
                        easing = CubicBezierEasing(0.22f, 0.0f, 0.18f, 1f)
                    )
                )
                arcTipAlpha.animateTo(0f, tween(200))
            }

            // 5. Zen water ripple radiates from center when arc concludes
            delay(1150)
            launch {
                rippleAlpha.animateTo(0.45f, tween(100))
                launch {
                    rippleRadius.animateTo(1f, tween(750, easing = CubicBezierEasing(0.15f, 0f, 0.25f, 1f)))
                }
                delay(220)
                rippleAlpha.animateTo(0f, tween(480))
            }

            // 6. Gentle settling breath
            launch {
                settleScale.animateTo(
                    targetValue = 1.025f,
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

            val overallScale = settleScale.value * discScale.value
            val globalAlpha = discAlpha.value

            withTransform({
                scale(overallScale, overallScale, Offset(cx, cy))
            }) {
                // ── 1. Serene Ambient Aura ─────────────────────────────
                val auraR = w * 0.48f * auraBreath
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            auraColor.copy(alpha = auraAlpha * globalAlpha),
                            auraColor.copy(alpha = 0f)
                        ),
                        center = Offset(cx, cy),
                        radius = auraR
                    ),
                    radius = auraR,
                    center = Offset(cx, cy)
                )

                // ── 2. Expanding Zen Ripple ────────────────────────────
                if (rippleAlpha.value > 0f) {
                    val rr = w * 0.46f * rippleRadius.value
                    drawCircle(
                        color = glyphColor.copy(alpha = rippleAlpha.value * 0.5f),
                        radius = rr.coerceAtLeast(1f),
                        center = Offset(cx, cy),
                        style = Stroke(width = w * 0.015f)
                    )
                }

                // ── 3. Base Elevated White Disc ────────────────────────
                val baseDiscRadius = w * 0.40f
                // Soft directional shadow (downward-right)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.11f * globalAlpha),
                            Color.Black.copy(alpha = 0.03f * globalAlpha),
                            Color.Transparent
                        ),
                        center = Offset(cx + w * 0.02f, cy + h * 0.035f),
                        radius = baseDiscRadius * 1.12f
                    ),
                    radius = baseDiscRadius * 1.08f,
                    center = Offset(cx + w * 0.02f, cy + h * 0.035f)
                )
                // Base Disc Body
                drawCircle(
                    color = discBaseColor.copy(alpha = globalAlpha),
                    radius = baseDiscRadius,
                    center = Offset(cx, cy)
                )
                // Subtle tactile rim
                drawCircle(
                    color = Color.Black.copy(alpha = 0.05f * globalAlpha),
                    radius = baseDiscRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1f)
                )

                // ── 4. Outer Sculptural Olive Arc ──────────────────────
                // Arc covers ~280° from ~-75° (top right) sweeping counter-clockwise to ~+25° (bottom right)
                val arcRadius = w * 0.325f
                val arcStrokeWidth = w * 0.092f

                val startAngle = -75f
                val sweepAngle = -280f

                val arcPath = Path().apply {
                    arcTo(
                        rect = Rect(
                            left = cx - arcRadius,
                            top = cy - arcRadius,
                            right = cx + arcRadius,
                            bottom = cy + arcRadius
                        ),
                        startAngleDegrees = startAngle,
                        sweepAngleDegrees = sweepAngle,
                        forceMoveTo = true
                    )
                }

                val pathMeasure = PathMeasure()
                pathMeasure.setPath(arcPath, false)
                val totalLength = pathMeasure.length
                val drawLength = totalLength * arcDrawProgress.value

                if (drawLength > 0f) {
                    val currentSegment = Path()
                    pathMeasure.getSegment(0f, drawLength, currentSegment, true)

                    // Arc subtle drop shadow onto base disc
                    drawPath(
                        path = currentSegment,
                        color = Color.Black.copy(alpha = 0.12f * globalAlpha),
                        style = Stroke(
                            width = arcStrokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Main Olive Arc Body
                    drawPath(
                        path = currentSegment,
                        color = glyphColor.copy(alpha = globalAlpha),
                        style = Stroke(
                            width = arcStrokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Luminous leading tip during draw
                    if (arcTipAlpha.value > 0f && drawLength < totalLength) {
                        val pos = pathMeasure.getPosition(drawLength)
                        drawArcTip(
                            center = pos,
                            strokeWidth = arcStrokeWidth,
                            glyphColor = glyphColor,
                            pearlColor = pearlColor,
                            alpha = arcTipAlpha.value
                        )
                    }
                }

                // ── 5. Elevated Middle White Disc ──────────────────────
                val midDiscRadius = w * 0.232f
                // Middle Disc Drop Shadow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.13f * globalAlpha),
                            Color.Black.copy(alpha = 0.04f * globalAlpha),
                            Color.Transparent
                        ),
                        center = Offset(cx + w * 0.015f, cy + h * 0.025f),
                        radius = midDiscRadius * 1.15f
                    ),
                    radius = midDiscRadius * 1.12f,
                    center = Offset(cx + w * 0.015f, cy + h * 0.025f)
                )
                // Middle Disc Body
                drawCircle(
                    color = discBaseColor.copy(alpha = globalAlpha),
                    radius = midDiscRadius,
                    center = Offset(cx, cy)
                )
                // Middle Disc Rim
                drawCircle(
                    color = Color.Black.copy(alpha = 0.05f * globalAlpha),
                    radius = midDiscRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1f)
                )

                // ── 6. Middle Concentric Olive Ring ────────────────────
                val ringRadius = (w * 0.142f) * middleRingScale.value
                val ringStrokeWidth = w * 0.054f
                if (ringRadius > 0f) {
                    drawCircle(
                        color = glyphColor.copy(alpha = globalAlpha * middleRingScale.value.coerceIn(0f, 1f)),
                        radius = ringRadius,
                        center = Offset(cx, cy),
                        style = Stroke(width = ringStrokeWidth)
                    )
                }

                // ── 7. Center Solid Olive Core Dot ─────────────────────
                val centerDotRadius = (w * 0.056f) * centerDotScale.value * centerPebblePulse
                if (centerDotRadius > 0f) {
                    drawCircle(
                        color = glyphColor.copy(alpha = globalAlpha * centerDotScale.value.coerceIn(0f, 1f)),
                        radius = centerDotRadius,
                        center = Offset(cx, cy)
                    )
                }
            }
        }
    }
}

/** Draws the mindful luminous tip leading the arc stroke. */
private fun DrawScope.drawArcTip(
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
            radius = r * 2.6f
        ),
        radius = r * 2.6f,
        center = center
    )
    // Inner core
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
