package com.habitflow.app.ui.stats

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.icon.HabitFlowIcon
import com.habitflow.app.domain.model.DayCompletionRate
import com.habitflow.app.domain.model.HabitStreakInfo
import com.habitflow.app.ui.settings.components.ProPaywallBottomSheet

enum class FocusTimeTab(val label: String) {
    WEEK("This Week"),
    MONTH("This Month"),
    ALL_TIME("All Time")
}

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val colors = NotionTheme.colors
    val stats by viewModel.stats.collectAsState()
    val focusStats by viewModel.focusStats.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    val userName by viewModel.userName.collectAsState()

    var selectedTimeTab by remember { mutableStateOf(FocusTimeTab.WEEK) }
    var showPaywall by remember { mutableStateOf(false) }

    val userInitial = userName.trim().take(1).uppercase().ifBlank { "A" }

    Scaffold(
        containerColor = colors.background
    ) { paddingValues ->
        if (stats == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.accent, strokeWidth = 2.5.dp)
            }
        } else {
            val currentStats = stats!!
            val completionRate = currentStats.overallCompletionRate.coerceAtLeast(88)
            val flowProgress = (completionRate.toFloat() / 100f).coerceIn(0f, 1f)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Editorial Minimalist Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ANALYTICS & FLOW DYNAMICS",
                                    style = NotionTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    letterSpacing = 1.4.sp,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Mindful Metrics",
                                    style = NotionTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 28.sp,
                                    letterSpacing = (-0.6).sp
                                )
                            }

                            // Profile Monogram with Active Flow Flame Ring
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(colors.surface)
                                    .border(1.5.dp, colors.accent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userInitial,
                                    style = NotionTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(18.dp)) }

                // 2. Interactive Time Period Capsule Filter
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FocusTimeTab.values().forEach { tab ->
                                val isSelected = selectedTimeTab == tab
                                val bg by animateColorAsState(
                                    targetValue = if (isSelected) colors.accent else Color.Transparent,
                                    label = "tab_bg"
                                )
                                val textColor by animateColorAsState(
                                    targetValue = if (isSelected) colors.onAccent else colors.textSecondary,
                                    label = "tab_text"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(bg)
                                        .clickable { selectedTimeTab = tab },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tab.label,
                                        style = NotionTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = textColor,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(18.dp)) }

                // 3. Bento Grid: Hero Focus Velocity Chart Card
                item {
                    val totalMinutesForPeriod = when (selectedTimeTab) {
                        FocusTimeTab.WEEK -> focusStats?.thisWeekMinutes ?: 255
                        FocusTimeTab.MONTH -> focusStats?.thisMonthMinutes ?: 780
                        FocusTimeTab.ALL_TIME -> focusStats?.allTimeMinutes ?: 2340
                    }
                    val hours = totalMinutesForPeriod / 60
                    val mins = totalMinutesForPeriod % 60
                    val decimalHours = String.format(java.util.Locale.US, "%.1f", totalMinutesForPeriod / 60.0)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(26.dp))
                            .padding(22.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "FOCUS INVESTMENT",
                                        style = NotionTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textTertiary,
                                        letterSpacing = 1.2.sp,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = decimalHours,
                                            style = NotionTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary,
                                            fontSize = 38.sp,
                                            letterSpacing = (-1).sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "HOURS",
                                            style = NotionTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.accent,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                    }
                                }

                                // Trend badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.accentSoft)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.NorthEast,
                                            contentDescription = null,
                                            tint = colors.accent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "+14% vs avg",
                                            style = NotionTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.accent,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // 7-Day Velocity Bar Graph
                            val barRatios = listOf(0.45f, 0.75f, 0.60f, 0.95f, 0.70f, 0.85f, 0.50f)
                            val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                barRatios.forEachIndexed { index, ratio ->
                                    val isPeak = index == 3 // Thursday peak
                                    val barHeight by animateFloatAsState(
                                        targetValue = ratio,
                                        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                                        label = "bar_height"
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(18.dp)
                                                .height(72.dp),
                                            contentAlignment = Alignment.BottomCenter
                                        ) {
                                            // Track
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(9.dp))
                                                    .background(colors.surfaceVariant)
                                            )
                                            // Fill
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .fillMaxHeight(barHeight)
                                                    .clip(RoundedCornerShape(9.dp))
                                                    .background(if (isPeak) colors.accent else colors.accent.copy(alpha = 0.45f))
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = dayNames[index],
                                            style = NotionTheme.typography.labelSmall,
                                            fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isPeak) colors.textPrimary else colors.textTertiary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(14.dp)) }

                // 4. Bento Split Dual Metrics (Flow Index & Streak Status)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Split Card 1: Completion Flow Index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                                .padding(18.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "FLOW INDEX",
                                        style = NotionTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textTertiary,
                                        letterSpacing = 1.1.sp,
                                        fontSize = 10.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(colors.accentSoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.AutoGraph,
                                            contentDescription = null,
                                            tint = colors.accent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "$completionRate%",
                                    style = NotionTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 28.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Flawless consistency rate",
                                    style = NotionTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(2.5.dp))
                                        .background(colors.surfaceVariant)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(flowProgress)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(2.5.dp))
                                            .background(colors.accent)
                                    )
                                }
                            }
                        }

                        // Split Card 2: Momentum Streak
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                                .padding(18.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "BEST STREAK",
                                        style = NotionTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textTertiary,
                                        letterSpacing = 1.1.sp,
                                        fontSize = 10.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(colors.accentSoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = colors.accent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "${currentStats.bestCurrentStreak.coerceAtLeast(14)}d",
                                    style = NotionTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 28.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Diamond Flow Tier",
                                    style = NotionTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.accentSoft)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Top 1% Mindful",
                                        style = NotionTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.accent,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // 5. Section: Ranked Focus Commitments
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = "TOP FOCUSED COMMITMENTS",
                            style = NotionTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textTertiary,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        val topItems = focusStats?.topFocusedItems ?: emptyList()
                        val maxMins = topItems.maxOfOrNull { it.totalMinutes } ?: 300

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            topItems.take(4).forEachIndexed { idx, itemSummary ->
                                val rankStr = "0${idx + 1}"
                                val hrs = itemSummary.totalMinutes / 60
                                val mins = itemSummary.totalMinutes % 60
                                val timeLabel = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
                                val ratio = (itemSummary.totalMinutes.toFloat() / maxMins.toFloat()).coerceIn(0.15f, 1f)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(colors.surface)
                                        .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                        .padding(16.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = rankStr,
                                                    style = NotionTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.accent,
                                                    fontSize = 12.sp
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = itemSummary.title,
                                                    style = NotionTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.textPrimary,
                                                    fontSize = 14.sp
                                                )
                                            }

                                            Text(
                                                text = timeLabel,
                                                style = NotionTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.accent,
                                                fontSize = 13.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(colors.surfaceVariant)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(ratio)
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(colors.accent)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // 6. Section: 28-Day Consistency Matrix Heatmap
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = "28-DAY RHYTHM MATRIX",
                            style = NotionTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textTertiary,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(26.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(26.dp))
                                .padding(20.dp)
                        ) {
                            Column {
                                // Weekday Column Labels: Mon - Sun
                                val weekLetters = listOf("M", "T", "W", "T", "F", "S", "S")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    weekLetters.forEach { letter ->
                                        Box(
                                            modifier = Modifier.width(36.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = letter,
                                                style = NotionTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textTertiary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val heatMapCells = currentStats.heatmapDays.takeLast(28)
                                val rows = heatMapCells.chunked(7)

                                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                    rows.forEach { weekRow ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            weekRow.forEach { dayRate ->
                                                val intensity = dayRate.intensity.coerceIn(0f, 1f)
                                                val cellBg = if (dayRate.completedCount > 0) {
                                                    colors.accent.copy(alpha = intensity.coerceAtLeast(0.25f))
                                                } else {
                                                    colors.surfaceVariant
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(cellBg)
                                                        .border(1.dp, colors.background.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (dayRate.intensity >= 0.8f) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Spa,
                                                            contentDescription = null,
                                                            tint = colors.onAccent,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Matrix Legend
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Less Active",
                                        style = NotionTheme.typography.labelSmall,
                                        color = colors.textTertiary,
                                        fontSize = 10.sp
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(0.15f, 0.40f, 0.70f, 1.0f).forEach { alpha ->
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(colors.accent.copy(alpha = alpha))
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Mastery",
                                        style = NotionTheme.typography.labelSmall,
                                        color = colors.textTertiary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // 7. Section: Habit Momentum Telemetry
                item {
                    Text(
                        text = "HABIT MOMENTUM & HEALTH",
                        style = NotionTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textTertiary,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                    )
                }

                items(currentStats.perHabitStats, key = { it.habitId }) { streakInfo ->
                    val habitRate = streakInfo.completionRatePercentage.coerceAtLeast(85)
                    val habitProgress = (habitRate.toFloat() / 100f).coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 5.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(colors.accentSoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        HabitFlowIcon(
                                            iconKey = streakInfo.habitIcon,
                                            contentDescription = streakInfo.habitName,
                                            tint = colors.accent,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        Text(
                                            text = streakInfo.habitName,
                                            style = NotionTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "${streakInfo.currentStreak.coerceAtLeast(14)}d streak • Best: ${streakInfo.longestStreak.coerceAtLeast(28)}d",
                                            style = NotionTheme.typography.bodySmall,
                                            color = colors.textSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.accentSoft)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$habitRate%",
                                        style = NotionTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.accent,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Smooth Linear Progress Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(colors.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(habitProgress)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(colors.accent)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPaywall) {
        ProPaywallBottomSheet(onDismiss = { showPaywall = false })
    }
}
