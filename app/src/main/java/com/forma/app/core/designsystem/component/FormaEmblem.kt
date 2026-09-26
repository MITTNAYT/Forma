package com.forma.app.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.forma.app.core.designsystem.FormaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * FormaEmblem – Bauhaus Architectural "F" Emblem.
 *
 * Embodying the Bauhaus philosophy ("Form follows function"):
 * - A tactile ceramic stone disc foundation
 * - A vertical structural habit spine (the consistency column)
 * - Two cantilevered horizontal beams forming the architectural "F"
 * - A golden-ratio focal balance point
 * - Tactile ambient shadows, subtle bevels, and living spring assembly
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
    val focalDotPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "focalPulse"
    )

    // ── Bauhaus Architectural Assembly Sequence ─────────────────────
    val discScale = remember { Animatable(if (animated) 0.82f else 1f) }
    val discAlpha = remember { Animatable(if (animated) 0f else 1f) }
    val columnProgress = remember { Animatable(if (animated) 0f else 1f) }
    val topBeamProgress = remember { Animatable(if (animated) 0f else 1f) }
    val midBeamProgress = remember { Animatable(if (animated) 0f else 1f) }
    val focalDotScale = remember { Animatable(if (animated) 0f else 1f) }
    val specularAlpha = remember { Animatable(if (animated) 0f else 0.8f) }
    val rippleRadius = remember { Animatable(0f) }
    val rippleAlpha = remember { Animatable(0f) }
    val settleScale = remember { Animatable(1f) }

    if (animated) {
        LaunchedEffect(Unit) {
            // 1. Ceramic Foundation Disc rises
            launch { discAlpha.animateTo(1f, tween(320)) }
            launch {
                discScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }

            // 2. Vertical Architectural Column erects from foundation
            delay(100)
            launch {
                columnProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 420,
                        easing = CubicBezierEasing(0.18f, 0.0f, 0.15f, 1.0f)
                    )
                )
            }

            // 3. Top Cantilever Beam glides in horizontally
            delay(180)
            launch {
                topBeamProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.68f,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }

            // 4. Mid Horizontal Beam slides in (staggered)
            delay(120)
            launch {
                midBeamProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.68f,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }

            // 5. Focal Balance Pebble blossoms
            delay(140)
            launch {
                focalDotScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }

            // 6. Specular Glaze Highlight & Architectural Micro-Ripple
            delay(160)
            launch {
                specularAlpha.animateTo(1f, tween(200))
                delay(120)
                specularAlpha.animateTo(0.75f, tween(300))
            }
            launch {
                rippleAlpha.animateTo(0.40f, tween(100))
                launch {
                    rippleRadius.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
                }
                delay(180)
                rippleAlpha.animateTo(0f, tween(420))
            }

            // 7. Tactile settle breath
            launch {
                settleScale.animateTo(
                    targetValue = 1.02f,
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
            val unit = w / 108f

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

                // ── 2. Expanding Architectural Ripple ──────────────────
                if (rippleAlpha.value > 0f) {
                    val rr = w * 0.46f * rippleRadius.value
                    drawCircle(
                        color = glyphColor.copy(alpha = rippleAlpha.value * 0.45f),
                        radius = rr.coerceAtLeast(1f),
                        center = Offset(cx, cy),
                        style = Stroke(width = w * 0.015f)
                    )
                }

                // ── 3. Base Elevated Ceramic Stone Disc ────────────────
                val baseDiscRadius = w * 0.40f
                // Directional Ambient Shadow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.12f * globalAlpha),
                            Color.Black.copy(alpha = 0.035f * globalAlpha),
                            Color.Transparent
                        ),
                        center = Offset(cx + w * 0.02f, cy + h * 0.035f),
                        radius = baseDiscRadius * 1.14f
                    ),
                    radius = baseDiscRadius * 1.08f,
                    center = Offset(cx + w * 0.02f, cy + h * 0.035f)
                )
                // Disc Surface
                drawCircle(
                    color = discBaseColor.copy(alpha = globalAlpha),
                    radius = baseDiscRadius,
                    center = Offset(cx, cy)
                )
                // Tactile Bevel Rim
                drawCircle(
                    color = Color.Black.copy(alpha = 0.05f * globalAlpha),
                    radius = baseDiscRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1f)
                )
                // Debossed Outer Circular Groove
                drawCircle(
                    color = Color.Black.copy(alpha = 0.035f * globalAlpha),
                    radius = baseDiscRadius * 0.88f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.2f)
                )

                // ── 4. Bauhaus Architectural "F" Coordinates ───────────
                // Scaled to 108-unit grid
                val colLeft = 38f * unit
                val colRight = 51f * unit
                val colTop = 35f * unit
                val colBottom = 73f * unit
                val colWidth = colRight - colLeft
                val colHeight = colBottom - colTop
                val cornerR = 3f * unit

                val topBeamLeft = 38f * unit
                val topBeamRight = 72f * unit
                val topBeamTop = 35f * unit
                val topBeamBottom = 45f * unit

                val midBeamLeft = 38f * unit
                val midBeamRight = 64f * unit
                val midBeamTop = 50f * unit
                val midBeamBottom = 59f * unit

                val focalCenterX = 61f * unit
                val focalCenterY = 68f * unit
                val focalRadius = 2.0f * unit

                // Cast Shadow of Bauhaus "F" onto Disc
                val shadowOffset = Offset(1.5f * unit, 2.5f * unit)
                val fullFPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(colLeft, colTop, colRight, colBottom),
                            topLeft = CornerRadius(cornerR, cornerR),
                            topRight = CornerRadius(0f, 0f),
                            bottomRight = CornerRadius(cornerR, cornerR),
                            bottomLeft = CornerRadius(cornerR, cornerR)
                        )
                    )
                    addRoundRect(
                        RoundRect(
                            rect = Rect(colLeft, topBeamTop, topBeamRight, topBeamBottom),
                            topLeft = CornerRadius(cornerR, cornerR),
                            topRight = CornerRadius(cornerR, cornerR),
                            bottomRight = CornerRadius(cornerR, cornerR),
                            bottomLeft = CornerRadius(0f, 0f)
                        )
                    )
                    addRoundRect(
                        RoundRect(
                            rect = Rect(colLeft, midBeamTop, midBeamRight, midBeamBottom),
                            topLeft = CornerRadius(0f, 0f),
                            topRight = CornerRadius(cornerR, cornerR),
                            bottomRight = CornerRadius(cornerR, cornerR),
                            bottomLeft = CornerRadius(0f, 0f)
                        )
                    )
                }

                // Draw Cast Shadow
                withTransform({
                    translate(shadowOffset.x, shadowOffset.y)
                }) {
                    drawPath(
                        path = fullFPath,
                        color = Color.Black.copy(alpha = 0.10f * globalAlpha * columnProgress.value)
                    )
                }

                // ── 5. Vertical Architectural Column (Habit Spine) ─────
                val currentColHeight = colHeight * columnProgress.value
                if (currentColHeight > 0f) {
                    val clipCol = Path().apply {
                        addRect(
                            Rect(
                                left = colLeft - 1f,
                                top = colBottom - currentColHeight,
                                right = colRight + 1f,
                                bottom = colBottom + 1f
                            )
                        )
                    }
                    clipPath(clipCol) {
                        // Deep foundation bevel layer
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.18f * globalAlpha),
                            topLeft = Offset(colLeft, colTop + 0.8f * unit),
                            size = Size(colWidth, colHeight),
                            cornerRadius = CornerRadius(cornerR, cornerR)
                        )
                        // Column body
                        drawRoundRect(
                            color = glyphColor.copy(alpha = globalAlpha),
                            topLeft = Offset(colLeft, colTop),
                            size = Size(colWidth, colHeight),
                            cornerRadius = CornerRadius(cornerR, cornerR)
                        )
                    }
                }

                // ── 6. Top Cantilever Beam ─────────────────────────────
                val currentTopBeamWidth = (topBeamRight - colLeft) * topBeamProgress.value
                if (currentTopBeamWidth > 0f) {
                    val topClip = Path().apply {
                        addRect(
                            Rect(
                                left = colLeft,
                                top = topBeamTop - 1f,
                                right = colLeft + currentTopBeamWidth + 1f,
                                bottom = topBeamBottom + 1f
                            )
                        )
                    }
                    clipPath(topClip) {
                        // Top beam foundation shadow
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.16f * globalAlpha),
                            topLeft = Offset(colLeft, topBeamTop + 0.8f * unit),
                            size = Size(topBeamRight - colLeft, topBeamBottom - topBeamTop),
                            cornerRadius = CornerRadius(cornerR, cornerR)
                        )
                        // Top beam body
                        drawRoundRect(
                            color = glyphColor.copy(alpha = globalAlpha),
                            topLeft = Offset(colLeft, topBeamTop),
                            size = Size(topBeamRight - colLeft, topBeamBottom - topBeamTop),
                            cornerRadius = CornerRadius(cornerR, cornerR)
                        )
                        // Specular glazed edge highlight
                        if (specularAlpha.value > 0f) {
                            drawLine(
                                color = pearlColor.copy(alpha = 0.40f * specularAlpha.value * globalAlpha),
                                start = Offset(colLeft + 2f * unit, topBeamTop + 1.2f * unit),
                                end = Offset(topBeamRight - 3f * unit, topBeamTop + 1.2f * unit),
                                strokeWidth = 1.0f * unit,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                // ── 7. Middle Cantilever Beam ──────────────────────────
                val currentMidBeamWidth = (midBeamRight - colLeft) * midBeamProgress.value
                if (currentMidBeamWidth > 0f) {
                    val midClip = Path().apply {
                        addRect(
                            Rect(
                                left = colLeft,
                                top = midBeamTop - 1f,
                                right = colLeft + currentMidBeamWidth + 1f,
                                bottom = midBeamBottom + 1f
                            )
                        )
                    }
                    clipPath(midClip) {
                        // Mid beam foundation shadow
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.16f * globalAlpha),
                            topLeft = Offset(colLeft, midBeamTop + 0.8f * unit),
                            size = Size(midBeamRight - colLeft, midBeamBottom - midBeamTop),
                            cornerRadius = CornerRadius(cornerR, cornerR)
                        )
                        // Mid beam body
                        drawRoundRect(
                            color = glyphColor.copy(alpha = globalAlpha),
                            topLeft = Offset(colLeft, midBeamTop),
                            size = Size(midBeamRight - colLeft, midBeamBottom - midBeamTop),
                            cornerRadius = CornerRadius(cornerR, cornerR)
                        )
                    }
                }

                // ── 8. Golden Ratio Focal Balance Pebble ───────────────
                val currentFocalR = focalRadius * focalDotScale.value * focalDotPulse
                if (currentFocalR > 0f) {
                    // Pebble shadow
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.20f * globalAlpha),
                        radius = currentFocalR * 1.15f,
                        center = Offset(focalCenterX + 0.4f * unit, focalCenterY + 0.6f * unit)
                    )
                    // Pebble body
                    drawCircle(
                        color = glyphColor.copy(alpha = globalAlpha),
                        radius = currentFocalR,
                        center = Offset(focalCenterX, focalCenterY)
                    )
                    // Pebble jewel specular highlight
                    drawCircle(
                        color = pearlColor.copy(alpha = 0.65f * globalAlpha),
                        radius = currentFocalR * 0.38f,
                        center = Offset(focalCenterX - currentFocalR * 0.28f, focalCenterY - currentFocalR * 0.28f)
                    )
                }
            }
        }
    }
}
