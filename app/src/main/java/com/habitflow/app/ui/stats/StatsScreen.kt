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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.CheckCircle
import com.habitflow.app.ui.analytics.components.MindfulInsightsSheet
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Star
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
import com.habitflow.app.core.designsystem.FormaTheme
import com.habitflow.app.core.designsystem.icon.HabitFlowIcon
import com.habitflow.app.core.designsystem.motion.formaPressEffect
import com.habitflow.app.domain.model.DayCompletionRate
import com.habitflow.app.domain.model.HabitStreakInfo
import com.habitflow.app.domain.model.OverallHabitStats
import com.habitflow.app.domain.repository.FocusItemSummary
import com.habitflow.app.ui.analytics.components.MindfulInsightsSheet
import com.habitflow.app.ui.mindfulness.BinauralBreathworkSheet
import com.habitflow.app.ui.settings.components.ProPaywallBottomSheet
import com.habitflow.app.ui.stats.components.CommitmentDetailSheet
import com.habitflow.app.ui.stats.components.HabitMomentumDetailSheet
import com.habitflow.app.ui.stats.components.RhythmDayDetailSheet
import com.habitflow.app.ui.stats.components.WeeklyZenRetroSheet
import com.habitflow.app.ui.stats.components.YearlyParchmentHeatmap

enum class FocusTimeTab(val label: String) {
    WEEK("This Week"),
    MONTH("This Month"),
    ALL_TIME("All Time")
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val colors = FormaTheme.colors
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
    val mindfulInsights by viewModel.mindfulInsights.collectAsState()
    val yearlyCompletions by viewModel.yearlyCompletions.collectAsState()
    val correlations by viewModel.correlations.collectAsState()
    val weeklyRetro by viewModel.weeklyRetro.collectAsState()

    var selectedTimeTab by remember { mutableStateOf(FocusTimeTab.WEEK) }
    var showPaywall by remember { mutableStateOf(false) }
    var showInsightsSheet by remember { mutableStateOf(false) }
    var showWeeklyRetroSheet by remember { mutableStateOf(false) }
    var showBreathworkSheet by remember { mutableStateOf(false) }

    val userInitial = userName.trim().take(1).uppercase().ifBlank { "A" }

    val currentStats = stats ?: OverallHabitStats(
        totalActiveHabits = 0,
        overallCompletionRate = 0,
        bestCurrentStreak = 0,
        bestAllTimeStreak = 0,
        heatmapDays = emptyList<DayCompletionRate>(),
        perHabitStats = emptyList<HabitStreakInfo>(),
        monthName = java.time.LocalDate.now().month.name.lowercase().replaceFirstChar { it.uppercase() },
        daysInMonth = java.time.LocalDate.now().lengthOfMonth(),
        monthHeatmapDays = emptyList<DayCompletionRate>(),
        firstDayOfWeekOffset = 0
    )
    val completionRate = currentStats.overallCompletionRate
    val flowProgress = (completionRate.toFloat() / 100f).coerceIn(0f, 1f)

    Scaffold(
        containerColor = colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Hero Consistency Metric Block
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    // Small label stays
                    Text(
                        text = "YOUR RHYTHM",
                        style = FormaTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textTertiary,
                        letterSpacing = 1.5.sp,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Hero completion rate — biggest number on screen
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "$completionRate",
                            style = FormaTheme.typography.displayLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.accent,
                            fontSize = 56.sp,
                            letterSpacing = (-1.5).sp
                        )
                        Text(
                            text = "%",
                            style = FormaTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 8.dp, start = 3.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "overall",
                                style = FormaTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "consistency",
                                style = FormaTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Animated progress bar under the big number
                    val animatedProgress by animateFloatAsState(
                        targetValue = flowProgress,
                        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                        label = "hero_progress"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.accentMuted)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.accent)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary context line
                    Text(
                        text = "${currentStats.bestCurrentStreak} day current run  ·  ${currentStats.totalActiveHabits} active habits",
                        style = FormaTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action pills row (Breathwork + Retro + Insights)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Breathwork pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.accentSoft)
                                .border(1.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .formaPressEffect(targetScale = 0.93f) { showBreathworkSheet = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Spa,
                                    contentDescription = "Breathwork",
                                    tint = colors.accent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Breathwork",
                                    style = FormaTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Weekly Retro pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceVariant)
                                .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .formaPressEffect(targetScale = 0.93f) { showWeeklyRetroSheet = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = "Weekly Retro",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Weekly Retro",
                                    style = FormaTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Insights pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceVariant)
                                .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .formaPressEffect(targetScale = 0.93f) { showInsightsSheet = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoGraph,
                                    contentDescription = "Insights",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Insights",
                                    style = FormaTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(18.dp)) }

            // 1b. Hero Zen Consistency Ring Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(26.dp))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Circular Zen Meter
                        Box(
                            modifier = Modifier.size(96.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.size(96.dp)) {
                                val stroke = 8.dp.toPx()
                                // Background subtle track
                                drawArc(
                                    color = colors.accentSoft.copy(alpha = 0.6f),
                                    startAngle = 140f,
                                    sweepAngle = 260f,
                                    useCenter = false,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = stroke,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                )
                                // Active Zen progress arc
                                val activeSweep = 260f * flowProgress
                                if (activeSweep > 0f) {
                                    drawArc(
                                        color = colors.accent,
                                        startAngle = 140f,
                                        sweepAngle = activeSweep,
                                        useCenter = false,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = stroke,
                                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                                        )
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$completionRate%",
                                    style = FormaTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = "RHYTHM",
                                    style = FormaTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    fontSize = 9.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Stats Summary Column
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Current Streak", style = FormaTheme.typography.bodySmall, color = colors.textSecondary, fontSize = 12.sp)
                                }
                                Text(
                                    text = "${currentStats.bestCurrentStreak} days",
                                    style = FormaTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 13.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Spa, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Active Rituals", style = FormaTheme.typography.bodySmall, color = colors.textSecondary, fontSize = 12.sp)
                                }
                                Text(
                                    text = "${currentStats.totalActiveHabits}",
                                    style = FormaTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 13.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Star, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Best Record", style = FormaTheme.typography.bodySmall, color = colors.textSecondary, fontSize = 12.sp)
                                }
                                Text(
                                    text = "${currentStats.bestAllTimeStreak} days",
                                    style = FormaTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 13.sp
                                )
                            }
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
                            FocusTimeTab.entries.forEach { tab ->
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
                                        style = FormaTheme.typography.labelSmall,
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
                        FocusTimeTab.WEEK -> focusStats?.thisWeekMinutes ?: 0
                        FocusTimeTab.MONTH -> focusStats?.thisMonthMinutes ?: 0
                        FocusTimeTab.ALL_TIME -> focusStats?.allTimeMinutes ?: 0
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
                                        style = FormaTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textTertiary,
                                        letterSpacing = 1.2.sp,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = decimalHours,
                                            style = FormaTheme.typography.headlineLarge.copy(fontFeatureSettings = "tnum"),
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary,
                                            fontSize = 38.sp,
                                            letterSpacing = (-1).sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "HOURS",
                                            style = FormaTheme.typography.titleSmall,
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
                                            style = FormaTheme.typography.labelSmall,
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
                                            .formaPressEffect(targetScale = 0.90f) {
                                                viewModel.selectFocusDay(dayFocus)
                                            }
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
                                            style = FormaTheme.typography.labelSmall,
                                            fontWeight = if (isSelected || dayFocus.isToday) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) colors.accent else if (dayFocus.isToday) colors.textPrimary else colors.textTertiary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            selectedFocusDay?.let { selDay ->
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${selDay.dayLetter} • ${selDay.dateIso}",
                                        style = FormaTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textTertiary,
                                        fontSize = 12.sp
                                    )
                                    val hrs = selDay.focusMinutes / 60
                                    val mins = selDay.focusMinutes % 60
                                    val timeStr = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
                                    Text(
                                        text = timeStr,
                                        style = FormaTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                                        fontWeight = FontWeight.Bold,
                                        color = colors.accent,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }

                // 4. Section: Ranked Focus Commitments (Clean & Empty until User Starts Committing)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = "TOP FOCUSED COMMITMENTS",
                            style = FormaTheme.typography.labelSmall,
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
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(colors.surface)
                                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "You haven't committed to anything yet.",
                                        style = FormaTheme.typography.bodyMedium,
                                        color = colors.textSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                topItems.take(5).forEachIndexed { idx, itemSummary ->
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
                                                        style = FormaTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                                        fontWeight = FontWeight.Bold,
                                                        color = colors.accent,
                                                        fontSize = 12.sp
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(
                                                        text = itemSummary.title,
                                                        style = FormaTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = colors.textPrimary,
                                                        fontSize = 14.sp
                                                    )
                                                }

                                                Text(
                                                    text = timeLabel,
                                                    style = FormaTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
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

                item { Spacer(modifier = Modifier.height(20.dp)) }

                // 5. Section: Dynamic Month-Matched Consistency Matrix Heatmap
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
                            style = FormaTheme.typography.labelSmall,
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
                                                style = FormaTheme.typography.labelSmall,
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
                                                            .formaPressEffect(targetScale = 0.88f) {
                                                                viewModel.inspectMatrixDay(dayRate.date)
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "${dayRate.dayOfMonth}",
                                                            style = FormaTheme.typography.labelSmall,
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
                                        style = FormaTheme.typography.labelSmall,
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
                                        style = FormaTheme.typography.labelSmall,
                                        color = colors.textTertiary,
                                        fontSize = 10.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Tap any day to inspect completed rituals & focus duration",
                                    style = FormaTheme.typography.labelSmall,
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

            // 7b. 365-Day Parchment Heatmap Section
            item {
                YearlyParchmentHeatmap(
                    completionsByDate = yearlyCompletions,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // 7c. Habit Correlation Insights Section
            if (correlations.isNotEmpty()) {
                item { Spacer(modifier = Modifier.height(24.dp)) }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "HABIT SYNERGY & CORRELATIONS",
                                style = FormaTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textTertiary,
                                letterSpacing = 1.2.sp,
                                fontSize = 11.sp
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            correlations.take(4).forEach { correlation ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(colors.surface)
                                        .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(colors.accentSoft),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                HabitFlowIcon(
                                                    iconKey = correlation.primaryHabitIcon,
                                                    contentDescription = null,
                                                    tint = colors.accent,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${correlation.primaryHabitName} → ${correlation.correlatedFactor}",
                                                    style = FormaTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.textPrimary,
                                                    fontSize = 14.sp
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = correlation.insightSummary,
                                                    style = FormaTheme.typography.bodySmall,
                                                    color = colors.textSecondary,
                                                    fontSize = 12.sp,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (correlation.isPositive) colors.accentSoft else colors.surfaceVariant)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (correlation.impactPercentage > 0) "+${correlation.impactPercentage}%" else "${correlation.impactPercentage}%",
                                                style = FormaTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (correlation.isPositive) colors.accent else colors.textSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
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

    if (showInsightsSheet && mindfulInsights != null) {
        MindfulInsightsSheet(
            report = mindfulInsights!!,
            onDismiss = { showInsightsSheet = false }
        )
    }

    if (showWeeklyRetroSheet && weeklyRetro != null) {
        WeeklyZenRetroSheet(
            retro = weeklyRetro!!,
            onDismiss = { showWeeklyRetroSheet = false }
        )
    }

    if (showBreathworkSheet) {
        BinauralBreathworkSheet(
            onDismiss = { showBreathworkSheet = false }
        )
    }
}
