package com.forma.app.ui.stats.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.util.DateUtils
import java.time.LocalDate

@Composable
fun YearlyParchmentHeatmap(
    completionsByDate: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val colors = FormaTheme.colors
    val scrollState = rememberScrollState()

    var selectedDayInfo by remember { mutableStateOf<Pair<String, Int>?>(null) }

    // Generate 52 weeks (364 days) leading up to today
    val today = LocalDate.now()
    val weeks = remember(today) {
        val totalDays = 52 * 7
        val startDate = today.minusDays((totalDays - 1).toLong())
        (0 until 52).map { weekIndex ->
            (0 until 7).map { dayOfWeek ->
                val dayOffset = weekIndex * 7 + dayOfWeek
                val date = startDate.plusDays(dayOffset.toLong())
                DateUtils.formatDateIso(date)
            }
        }
    }

    // Scroll to end (latest weeks) on first load
    LaunchedEffect(Unit) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "365-DAY ZEN PARCHMENT",
                            style = FormaTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textTertiary,
                            letterSpacing = 1.2.sp,
                            fontSize = 10.5.sp
                        )
                        Text(
                            text = "Yearly Ritual Consistency",
                            style = FormaTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 15.sp
                        )
                    }
                }

                // Total Yearly Completions Pill
                val totalYearCompletions = completionsByDate.values.sum()
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.accentSoft)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$totalYearCompletions Rituals",
                        style = FormaTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable 52-Week Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(3.5.dp)
            ) {
                weeks.forEach { weekDays ->
                    Column(verticalArrangement = Arrangement.spacedBy(3.5.dp)) {
                        weekDays.forEach { dateIso ->
                            val count = completionsByDate[dateIso] ?: 0
                            val cellColor = when {
                                count == 0 -> colors.surfaceVariant.copy(alpha = 0.5f)
                                count in 1..2 -> colors.accentSoft.copy(alpha = 0.9f)
                                count in 3..4 -> colors.accentMuted
                                count in 5..6 -> colors.accent
                                else -> Color(0xFF2E3D27)
                            }

                            val isSelected = selectedDayInfo?.first == dateIso

                            Box(
                                modifier = Modifier
                                    .size(13.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(cellColor)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) colors.textPrimary else colors.border.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(3.dp)
                                    )
                                    .clickable {
                                        selectedDayInfo = dateIso to count
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Legend & Inspector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selected Day Inspector Info
                AnimatedVisibility(
                    visible = selectedDayInfo != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    selectedDayInfo?.let { (date, count) ->
                        Text(
                            text = "$date: $count rituals",
                            style = FormaTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            fontSize = 11.sp
                        )
                    }
                }
                if (selectedDayInfo == null) {
                    Text(
                        text = "Tap cell to inspect",
                        style = FormaTheme.typography.bodySmall,
                        color = colors.textTertiary,
                        fontSize = 11.sp
                    )
                }

                // Shading Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Less",
                        style = FormaTheme.typography.labelSmall,
                        color = colors.textTertiary,
                        fontSize = 9.5.sp
                    )
                    listOf(
                        colors.surfaceVariant.copy(alpha = 0.5f),
                        colors.accentSoft.copy(alpha = 0.9f),
                        colors.accentMuted,
                        colors.accent,
                        Color(0xFF2E3D27)
                    ).forEach { levelColor ->
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(levelColor)
                        )
                    }
                    Text(
                        text = "More",
                        style = FormaTheme.typography.labelSmall,
                        color = colors.textTertiary,
                        fontSize = 9.5.sp
                    )
                }
            }
        }
    }
}
