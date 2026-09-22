package com.habitflow.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.FormaTheme
import com.habitflow.app.domain.model.DayCompletionRate

@Composable
fun FormaHeatmap(
    heatmapDays: List<DayCompletionRate>,
    modifier: Modifier = Modifier
) {
    val colors = FormaTheme.colors

    // 5 weeks, each week having 7 days (Mon-Sun)
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    // Group the 35 days into 5 columns of 7 days (weeks)
    val weeks = heatmapDays.chunked(7)

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "5-WEEK CONSISTENCY",
                style = FormaTheme.typography.labelSmall,
                color = colors.textSecondary,
                letterSpacing = 1.sp
            )
            Text(
                text = "Grayscale intensity",
                style = FormaTheme.typography.labelSmall,
                color = colors.textTertiary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Day label column (M, T, W, T, F, S, S)
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                dayLabels.forEach { label ->
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = FormaTheme.typography.labelSmall,
                            color = colors.textTertiary,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 5 columns (weeks)
            weeks.forEach { weekDays ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    weekDays.forEach { dayRate ->
                        val cellColor = calculateGrayscaleCellColor(
                            intensity = dayRate.intensity,
                            isDark = colors.isDark,
                            emptyColor = colors.surfaceVariant,
                            borderColor = colors.border
                        )

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(FormaTheme.shapes.extraSmall)
                                .background(cellColor)
                                .border(0.5.dp, colors.border.copy(alpha = 0.5f), FormaTheme.shapes.extraSmall)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Heatmap legend: Less -> More
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Less",
                style = FormaTheme.typography.labelSmall,
                color = colors.textTertiary,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.width(6.dp))

            val intensities = listOf(0f, 0.25f, 0.5f, 0.75f, 1f)
            intensities.forEach { intensity ->
                val sampleColor = calculateGrayscaleCellColor(
                    intensity = intensity,
                    isDark = colors.isDark,
                    emptyColor = colors.surfaceVariant,
                    borderColor = colors.border
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(FormaTheme.shapes.extraSmall)
                        .background(sampleColor)
                        .border(0.5.dp, colors.border.copy(alpha = 0.4f), FormaTheme.shapes.extraSmall)
                )
                Spacer(modifier = Modifier.width(3.dp))
            }

            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "More",
                style = FormaTheme.typography.labelSmall,
                color = colors.textTertiary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun NotionHeatmap(
    heatmapDays: List<DayCompletionRate>,
    modifier: Modifier = Modifier
) {
    FormaHeatmap(
        heatmapDays = heatmapDays,
        modifier = modifier
    )
}

private fun calculateGrayscaleCellColor(
    intensity: Float,
    isDark: Boolean,
    emptyColor: Color,
    borderColor: Color
): Color {
    if (intensity <= 0f) return emptyColor

    return if (isDark) {
        // Dark theme: 0.0 -> subtle dark, 1.0 -> crisp bright white/silver
        when {
            intensity < 0.3f -> Color(0xFF383838)
            intensity < 0.6f -> Color(0xFF6B6B6B)
            intensity < 0.9f -> Color(0xFFAAAAAA)
            else -> Color(0xFFEEEEEE)
        }
    } else {
        // Light theme: 0.0 -> subtle light, 1.0 -> deep ink black
        when {
            intensity < 0.3f -> Color(0xFFD4D4D4)
            intensity < 0.6f -> Color(0xFF888888)
            intensity < 0.9f -> Color(0xFF444444)
            else -> Color(0xFF191919)
        }
    }
}
