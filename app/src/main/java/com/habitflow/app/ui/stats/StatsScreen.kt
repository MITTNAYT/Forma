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
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.habitflow.app.domain.repository.FocusItemSummary
import com.habitflow.app.ui.settings.components.ProPaywallBottomSheet
import com.habitflow.app.ui.stats.components.CommitmentDetailSheet
import com.habitflow.app.ui.stats.components.HabitMomentumDetailSheet
import com.habitflow.app.ui.stats.components.RhythmDayDetailSheet

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
    val weeklyFocusDays by viewModel.weeklyFocusDays.collectAsState()
    val weekOffset by viewModel.weekOffset.collectAsState()
    val selectedFocusDay by viewModel.selectedFocusDay.collectAsState()
    val selectedMatrixDay by viewModel.selectedMatrixDay.collectAsState()
    val selectedCommitment by viewModel.selectedCommitment.collectAsState()
    val selectedHabitStreak by viewModel.selectedHabitStreak.collectAsState()

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
            val completionRate = currentStats.overallCompletionRate
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

                                // Week Navigation Controls
                                val weekLabel = when {
                                    weekOffset == 0 -> "THIS WEEK"
                                    weekOffset == -1 -> "LAST WEEK"
                                    else -> "${kotlin.math.abs(weekOffset)} WEEKS AGO"
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.previousWeek() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ChevronLeft,
                                            contentDescription = "Previous Week",
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colors.accentSoft)
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = weekLabel,
                                            style = NotionTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.accent,
                                            fontSize = 11.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.nextWeek() },
                                        enabled = weekOffset < 0,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ChevronRight,
                                            contentDescription = "Next Week",
                                            tint = if (weekOffset < 0) colors.textSecondary else colors.textTertiary.copy(alpha = 0.3f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Interactive 7-Day Velocity Bar Graph
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                weeklyFocusDays.forEach { dayFocus ->
                                    val isSelected = selectedFocusDay?.dateIso == dayFocus.dateIso
                                    val barHeight by animateFloatAsState(
                                        targetValue = dayFocus.ratio,
                                        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                                        label = "bar_height"
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.selectFocusDay(dayFocus) }
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
                                                    .background(if (isSelected) colors.accentSoft else colors.surfaceVariant)
                                                    .border(
                                                        width = if (isSelected) 1.5.dp else 0.dp,
                                                        color = if (isSelected) colors.accent else Color.Transparent,
                                                        shape = RoundedCornerShape(9.dp)
                                                    )
                                            )
                                            // Fill
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .fillMaxHeight(barHeight)
                                                    .clip(RoundedCornerShape(9.dp))
                                                    .background(if (isSelected || dayFocus.isToday) colors.accent else colors.accent.copy(alpha = 0.45f))
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = dayFocus.dayLetter,
                                            style = NotionTheme.typography.labelSmall,
                                            fontWeight = if (isSelected || dayFocus.isToday) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) colors.accent else if (dayFocus.isToday) colors.textPrimary else colors.textTertiary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            // Selected Day Focus Details Banner
                            selectedFocusDay?.let { selDay ->
                                Spacer(modifier = Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(colors.accentSoft)
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "DAY FOCUS (${selDay.dayLetter} • ${selDay.dateIso})",
                                                style = NotionTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.accent,
                                                fontSize = 10.sp
                                            )
                                            val hrs = selDay.focusMinutes / 60
                                            val mins = selDay.focusMinutes % 60
                                            val timeStr = if (hrs > 0) "${hrs}h ${mins}m logged" else "${mins}m logged"
                                            Text(
                                                text = timeStr,
                                                style = NotionTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary,
                                                fontSize = 13.sp
                                            )
                                        }

                                        Text(
                                            text = if (selDay.focusMinutes > 0) "Flow Active" else "Zero Flow",
                                            style = NotionTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.accent,
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
                                    text = "${currentStats.bestCurrentStreak}d",
                                    style = NotionTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 28.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = if (currentStats.bestCurrentStreak >= 7) "Diamond Flow Tier" else "Building Rhythm",
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
                                        text = if (currentStats.bestCurrentStreak >= 7) "Consistent Flow" else "Day 1 Journey",
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

                        if (topItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(colors.surface)
                                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No focus sessions logged yet.\nComplete focus sessions in the Pomodoro clock to see your rankings.",
                                    style = NotionTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
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
                                            .clickable { viewModel.openCommitmentDetail(itemSummary) }
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
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // 6. Section: Dynamic Month-Matched Consistency Matrix Heatmap
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        val matrixTitle = if (currentStats.monthName.isNotBlank()) {
                            "${currentStats.monthName} RHYTHM MATRIX (${currentStats.daysInMonth} DAYS)"
                        } else {
                            "MONTHLY RHYTHM MATRIX"
                        }

                        Text(
                            text = matrixTitle,
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

                                val blankSlots = currentStats.firstDayOfWeekOffset
                                val monthCells = currentStats.monthHeatmapDays
                                val allGridCells: List<DayCompletionRate?> = List(blankSlots) { null } + monthCells
                                val rows = allGridCells.chunked(7)

                                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                    rows.forEach { weekRow ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            weekRow.forEach { dayRate ->
                                                if (dayRate == null) {
                                                    Box(modifier = Modifier.size(36.dp))
                                                } else {
                                                    val intensity = dayRate.intensity.coerceIn(0f, 1f)
                                                    val cellBg = if (dayRate.completedCount > 0) {
                                                        colors.accent.copy(alpha = intensity.coerceAtLeast(0.28f))
                                                    } else {
                                                        colors.surfaceVariant
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .background(cellBg)
                                                            .border(1.dp, colors.background.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                                            .clickable { viewModel.inspectMatrixDay(dayRate.date) },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "${dayRate.dayOfMonth}",
                                                            style = NotionTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (dayRate.completedCount > 0 && intensity >= 0.5f) colors.onAccent else colors.textPrimary,
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                }
                                            }
                                            // Pad row if it has fewer than 7 cells
                                            if (weekRow.size < 7) {
                                                repeat(7 - weekRow.size) {
                                                    Box(modifier = Modifier.size(36.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Matrix Legend & Tap hint
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

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Tap any day to inspect completed rituals & focus duration",
                                    style = NotionTheme.typography.labelSmall,
                                    color = colors.textTertiary,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
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

                if (currentStats.perHabitStats.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No habits created yet.\nTap + on the navigation bar to cultivate your first ritual.",
                                style = NotionTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(currentStats.perHabitStats, key = { it.habitId }) { streakInfo ->
                        val habitRate = streakInfo.completionRatePercentage
                        val habitProgress = (habitRate.toFloat() / 100f).coerceIn(0f, 1f)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 5.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                                .clickable { viewModel.openHabitStreakDetail(streakInfo) }
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
                                                text = "${streakInfo.currentStreak}d streak • Best: ${streakInfo.longestStreak}d",
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
    }

    if (showPaywall) {
        ProPaywallBottomSheet(onDismiss = { showPaywall = false })
    }

    // Modal Bottom Sheets for interactive detail inspection
    selectedMatrixDay?.let { dayInfo ->
        RhythmDayDetailSheet(
            dayInfo = dayInfo,
            onDismiss = { viewModel.closeMatrixDayDetail() }
        )
    }

    selectedCommitment?.let { commitment ->
        CommitmentDetailSheet(
            item = commitment,
            onDismiss = { viewModel.closeCommitmentDetail() },
            onStartFocusSession = { /* Timer action */ }
        )
    }

    selectedHabitStreak?.let { habitStreak ->
        HabitMomentumDetailSheet(
            streakInfo = habitStreak,
            onDismiss = { viewModel.closeHabitStreakDetail() }
        )
    }
}
