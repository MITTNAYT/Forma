package com.forma.app.ui.chronotype.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.domain.model.Chronotype
import com.forma.app.domain.model.EnergyZone
import com.forma.app.domain.model.HourlyEnergyPoint
import java.time.LocalTime

@Composable
fun CircadianEnergyWaveCard(
    chronotype: Chronotype,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit = {}
) {
    val engine = androidx.compose.runtime.remember { com.forma.app.core.chronotype.CircadianEnergyEngine() }
    val hourlyPoints = androidx.compose.runtime.remember(chronotype) { engine.calculateDailyEnergyCurve(chronotype) }
    val (activeZone, currentEnergyScore) = androidx.compose.runtime.remember(chronotype) { engine.getCurrentEnergyState(chronotype) }

    CircadianEnergyWaveCard(
        chronotype = chronotype,
        hourlyPoints = hourlyPoints,
        activeZone = activeZone,
        currentEnergyScore = currentEnergyScore,
        onCardClick = onCardClick,
        modifier = modifier
    )
}

@Composable
fun CircadianEnergyWaveCard(
    chronotype: Chronotype,
    hourlyPoints: List<HourlyEnergyPoint>,
    activeZone: EnergyZone,
    currentEnergyScore: Float,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FormaTheme.colors
    val currentHour = LocalTime.now().hour
    val currentMinute = LocalTime.now().minute
    val timeFloat = currentHour + currentMinute / 60f

    val zoneIcon = when (activeZone) {
        EnergyZone.PEAK_FOCUS -> Icons.Rounded.LocalFireDepartment
        EnergyZone.CREATIVE_FLOW -> Icons.Rounded.Brush
        EnergyZone.RECHARGE_REST -> Icons.Rounded.Spa
        EnergyZone.WIND_DOWN -> Icons.Rounded.Bedtime
    }

    val energyAnim by animateFloatAsState(
        targetValue = currentEnergyScore,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "energy_score"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .clickable(onClick = onCardClick)
            .padding(18.dp)
    ) {
        Column {
            // Header Row: Chronotype Animal + Energy Zone Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = chronotype.animalSymbol,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = "Circadian Rhythm",
                            style = FormaTheme.typography.labelSmall,
                            color = colors.textTertiary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${chronotype.name.lowercase().replaceFirstChar { it.uppercase() }} Rhythm • ${String.format("%.0f", energyAnim * 100)}% Energy",
                            style = FormaTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 14.sp
                        )
                    }
                }

                // Zone Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accentSoft)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = zoneIcon,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = activeZone.displayName,
                        style = FormaTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Wave Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    if (hourlyPoints.isEmpty()) return@Canvas

                    val w = size.width
                    val h = size.height
                    val stepX = w / 23f

                    val wavePath = Path()
                    val fillPath = Path()

                    hourlyPoints.forEachIndexed { index, point ->
                        val x = index * stepX
                        // Higher energy score = higher up on canvas (smaller y)
                        val y = h - (point.energyScore * (h - 16f)) - 8f

                        if (index == 0) {
                            wavePath.moveTo(x, y)
                            fillPath.moveTo(x, h)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = (index - 1) * stepX
                            val prevY = h - (hourlyPoints[index - 1].energyScore * (h - 16f)) - 8f
                            val controlX1 = prevX + (x - prevX) / 2f
                            val controlX2 = prevX + (x - prevX) / 2f
                            wavePath.cubicTo(controlX1, prevY, controlX2, y, x, y)
                            fillPath.cubicTo(controlX1, prevY, controlX2, y, x, y)
                        }
                    }

                    fillPath.lineTo(w, h)
                    fillPath.close()

                    // Gradient Fill under curve
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colors.accent.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )

                    // Line Stroke
                    drawPath(
                        path = wavePath,
                        color = colors.accent,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Current Time Indicator Pulse
                    val indicatorX = (timeFloat.coerceIn(0f, 23f) / 23f) * w
                    val currentPointY = h - (currentEnergyScore * (h - 16f)) - 8f

                    // Vertical dashed timeline marker
                    drawLine(
                        color = colors.accentMuted.copy(alpha = 0.5f),
                        start = Offset(indicatorX, 0f),
                        end = Offset(indicatorX, h),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Current position circle
                    drawCircle(
                        color = colors.background,
                        radius = 6.dp.toPx(),
                        center = Offset(indicatorX, currentPointY)
                    )
                    drawCircle(
                        color = colors.accent,
                        radius = 4.dp.toPx(),
                        center = Offset(indicatorX, currentPointY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Time markers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("06:00", style = FormaTheme.typography.labelSmall, color = colors.textTertiary, fontSize = 10.sp)
                Text("12:00", style = FormaTheme.typography.labelSmall, color = colors.textTertiary, fontSize = 10.sp)
                Text("18:00", style = FormaTheme.typography.labelSmall, color = colors.textTertiary, fontSize = 10.sp)
                Text("24:00", style = FormaTheme.typography.labelSmall, color = colors.textTertiary, fontSize = 10.sp)
            }
        }
    }
}
