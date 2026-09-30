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
    // ── Mindful Keystone Assembly Sequence ─────────────────────────
    val discScale = remember { Animatable(if (animated) 0.82f else 1f) }
    val discAlpha = remember { Animatable(if (animated) 0f else 1f) }
    val columnProgress = remember { Animatable(if (animated) 0f else 1f) }
    val topBeamProgress = remember { Animatable(if (animated) 0f else 1f) }
    val midBeamProgress = remember { Animatable(if (animated) 0f else 1f) }
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

            // 2. Foundation Cairn Stone rises from ceramic base
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

            // 3. Middle Cantilever Stone glides into equilibrium
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

            // 4. Top Keystone settles onto the cairn stack
            delay(160)
            launch {
                topBeamProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.68f,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }

            // 5. Specular Glaze Highlight & Architectural Micro-Ripple
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

                // ── 4. Concept C: Mindful Keystone ("F" Zen Cairn Stones) ───────
                // 108-unit grid, optically balanced cairn stack
                // Stone 1 (Top Keystone): 44 x 13, R 6.5
                val s1Left = 31.5f * unit
                val s1Right = 75.5f * unit
                val s1Top = 28.0f * unit
                val s1Bottom = 41.0f * unit
                val s1Width = s1Right - s1Left
                val s1Height = s1Bottom - s1Top
                val s1Radius = 6.5f * unit

                // Stone 2 (Middle Cantilever Stone): 34 x 12, R 6.0
                val s2Left = 31.5f * unit
                val s2Right = 65.5f * unit
                val s2Top = 46.0f * unit
                val s2Bottom = 58.0f * unit
                val s2Width = s2Right - s2Left
                val s2Height = s2Bottom - s2Top
                val s2Radius = 6.0f * unit

                // Stone 3 (Foundation Cairn Stone): 13 x 15, R 6.5
                val s3Left = 32.5f * unit
                val s3Right = 45.5f * unit
                val s3Top = 63.0f * unit
                val s3Bottom = 78.0f * unit
                val s3Width = s3Right - s3Left
                val s3Height = s3Bottom - s3Top
                val s3Radius = 6.5f * unit

                // Unified Cast Shadow of all 3 Cairn Stones onto Disc
                val shadowOffset = Offset(1.5f * unit, 2.5f * unit)
                val fullCairnPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(s1Left, s1Top, s1Right, s1Bottom),
                            cornerRadius = CornerRadius(s1Radius, s1Radius)
                        )
                    )
                    addRoundRect(
                        RoundRect(
                            rect = Rect(s2Left, s2Top, s2Right, s2Bottom),
                            cornerRadius = CornerRadius(s2Radius, s2Radius)
                        )
                    )
                    addRoundRect(
                        RoundRect(
                            rect = Rect(s3Left, s3Top, s3Right, s3Bottom),
                            cornerRadius = CornerRadius(s3Radius, s3Radius)
                        )
                    )
                }

                // Draw Cairn Cast Shadow
                withTransform({
                    translate(shadowOffset.x, shadowOffset.y)
                }) {
                    drawPath(
                        path = fullCairnPath,
                        color = Color.Black.copy(alpha = 0.12f * globalAlpha * columnProgress.value)
                    )
                }

                // ── 5. Foundation Cairn Stone (Rising Habit Anchor) ──────
                val currentS3Height = s3Height * columnProgress.value
                if (currentS3Height > 0f) {
                    val clipS3 = Path().apply {
                        addRect(
                            Rect(
                                left = s3Left - 1f,
                                top = s3Bottom - currentS3Height,
                                right = s3Right + 1f,
                                bottom = s3Bottom + 1f
                            )
                        )
                    }
                    clipPath(clipS3) {
                        // Deep foundation shadow
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.18f * globalAlpha),
                            topLeft = Offset(s3Left, s3Top + 0.8f * unit),
                            size = Size(s3Width, s3Height),
                            cornerRadius = CornerRadius(s3Radius, s3Radius)
                        )
                        // Foundation Stone Body
                        drawRoundRect(
                            color = glyphColor.copy(alpha = globalAlpha),
                            topLeft = Offset(s3Left, s3Top),
                            size = Size(s3Width, s3Height),
                            cornerRadius = CornerRadius(s3Radius, s3Radius)
                        )
                        // Specular Crest Highlight
                        if (specularAlpha.value > 0f) {
                            drawLine(
                                color = pearlColor.copy(alpha = 0.45f * specularAlpha.value * globalAlpha),
                                start = Offset(36f * unit, 64f * unit),
                                end = Offset(41f * unit, 64f * unit),
                                strokeWidth = 1.0f * unit,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                // ── 6. Middle Cantilever Stone (Floating Balance) ─────────
                val currentS2Width = s2Width * midBeamProgress.value
                if (currentS2Width > 0f) {
                    val clipS2 = Path().apply {
                        addRect(
                            Rect(
                                left = s2Left - 1f,
                                top = s2Top - 1f,
                                right = s2Left + currentS2Width + 1f,
                                bottom = s2Bottom + 1f
                            )
                        )
                    }
                    clipPath(clipS2) {
                        // Shadow bevel
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.16f * globalAlpha),
                            topLeft = Offset(s2Left, s2Top + 0.8f * unit),
                            size = Size(s2Width, s2Height),
                            cornerRadius = CornerRadius(s2Radius, s2Radius)
                        )
                        // Stone Body
                        drawRoundRect(
                            color = glyphColor.copy(alpha = globalAlpha),
                            topLeft = Offset(s2Left, s2Top),
                            size = Size(s2Width, s2Height),
                            cornerRadius = CornerRadius(s2Radius, s2Radius)
                        )
                        // Specular Crest Highlight
                        if (specularAlpha.value > 0f) {
                            drawLine(
                                color = pearlColor.copy(alpha = 0.45f * specularAlpha.value * globalAlpha),
                                start = Offset(38f * unit, 47f * unit),
                                end = Offset(58f * unit, 47f * unit),
                                strokeWidth = 1.0f * unit,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                // ── 7. Top Keystone (Broad Resting Capstone) ─────────────
                val currentS1Width = s1Width * topBeamProgress.value
                if (currentS1Width > 0f) {
                    val clipS1 = Path().apply {
                        addRect(
                            Rect(
                                left = s1Left - 1f,
                                top = s1Top - 1f,
                                right = s1Left + currentS1Width + 1f,
                                bottom = s1Bottom + 1f
                            )
                        )
                    }
                    clipPath(clipS1) {
                        // Top keystone foundation shadow
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.16f * globalAlpha),
                            topLeft = Offset(s1Left, s1Top + 0.8f * unit),
                            size = Size(s1Width, s1Height),
                            cornerRadius = CornerRadius(s1Radius, s1Radius)
                        )
                        // Top keystone body
                        drawRoundRect(
                            color = glyphColor.copy(alpha = globalAlpha),
                            topLeft = Offset(s1Left, s1Top),
                            size = Size(s1Width, s1Height),
                            cornerRadius = CornerRadius(s1Radius, s1Radius)
                        )
                        // Specular crest highlight
                        if (specularAlpha.value > 0f) {
                            drawLine(
                                color = pearlColor.copy(alpha = 0.45f * specularAlpha.value * globalAlpha),
                                start = Offset(38f * unit, 29f * unit),
                                end = Offset(68f * unit, 29f * unit),
                                strokeWidth = 1.2f * unit,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}
