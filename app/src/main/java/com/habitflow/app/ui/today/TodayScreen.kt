package com.habitflow.app.ui.today

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Nature
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import com.habitflow.app.ui.today.components.AiStudioSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.habitflow.app.core.designsystem.motion.FormaMotion
import com.habitflow.app.core.designsystem.motion.formaPressEffect
import com.habitflow.app.core.designsystem.motion.formaStaggeredEntrance
import com.habitflow.app.domain.repository.AiPlanPreset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.core.designsystem.FormaTheme
import com.habitflow.app.core.util.DateUtils
import com.habitflow.app.domain.model.TodayScheduleItem
import com.habitflow.app.ui.settings.components.ProPaywallBottomSheet
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.habitflow.app.ui.today.components.InlineQuickEntryBar
import com.habitflow.app.ui.today.components.BehanceHabitCard
import com.habitflow.app.ui.today.components.BehanceHeroBanner
import com.habitflow.app.ui.today.components.DynamicStreakIsland
import com.habitflow.app.ui.today.components.HabitTaskDetailBottomSheet
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Rect
import com.habitflow.app.ui.today.components.TodayCoachMarksOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onNavigateToAddTask: (selectedDate: String) -> Unit,
    onNavigateToEditTask: (itemId: String) -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToFocusTimer: (itemId: String, title: String, durationMinutes: Int, isHabit: Boolean) -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val colors = FormaTheme.colors

    val selectedDate by viewModel.selectedDate.collectAsState()
    val daySchedule by viewModel.daySchedule.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val hasSeenTodayCoachMarks by viewModel.hasSeenTodayCoachMarks.collectAsState()

    var weekStripBounds by remember { mutableStateOf<Rect?>(null) }
    var ritualPillBounds by remember { mutableStateOf<Rect?>(null) }
    var aiPlanBounds by remember { mutableStateOf<Rect?>(null) }
    var addButtonBounds by remember { mutableStateOf<Rect?>(null) }

    var showPaywall by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showAiPresetSheet by remember { mutableStateOf(false) }
    var showMorningSheet by remember { mutableStateOf(false) }
    var showEveningSheet by remember { mutableStateOf(false) }
    var showBreathingSheet by remember { mutableStateOf(false) }
    var showGuidedRoutineSheet by remember { mutableStateOf(false) }
    var showJournalSheet by remember { mutableStateOf(false) }
    var showAiStudioSheet by remember { mutableStateOf(false) }
    var selectedDetailItem by remember { mutableStateOf<TodayScheduleItem?>(null) }
    var celebrationInfo by remember { mutableStateOf<Pair<String, Int>?>(null) }
    val haptic = LocalHapticFeedback.current

    // Silky Smooth Staggered Entrance Animation
    val contentAlpha = remember { Animatable(0f) }
    val contentOffsetY = remember { Animatable(18f) }

    LaunchedEffect(Unit) {
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
        )
    }
    LaunchedEffect(Unit) {
        contentOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is TodayUiEvent.ShowProPaywall -> showPaywall = true
                is TodayUiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> "Good morning,"
        in 12..16 -> "Good afternoon,"
        in 17..21 -> "Peaceful evening,"
        else -> "Good night,"
    }

    val dateFormatted = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()))

    // Calculate 7-day week strip (Monday to Sunday)
    val startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekDays = (0..6).map { startOfWeek.plusDays(it.toLong()) }

    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            containerColor = colors.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = contentAlpha.value
                            translationY = contentOffsetY.value.dp.toPx()
                        },
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    // 1. Personalized Header
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 4.dp)
                        ) {
                            // Row 1: Date label + primary action only (Morning/Evening ritual)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateFormatted.uppercase(),
                                    style = FormaTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    letterSpacing = 1.2.sp,
                                    fontSize = 11.sp
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Quick Flow button (Freeform Pomodoro Focus Session)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colors.surfaceVariant)
                                            .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                            .formaPressEffect(targetScale = 0.92f) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onNavigateToFocusTimer("freeform", "Mindful Focus", 25, false)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.Timer,
                                                contentDescription = "Quick Flow",
                                                tint = colors.textPrimary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = "Quick Flow",
                                                style = FormaTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    // Single primary ritual pill (Morning Clarity / Evening Rest)
                                    val currentHour = LocalTime.now().hour
                                    val isEvening = currentHour >= 18 || currentHour < 4
                                    val ritualTitle = if (isEvening) "Evening Rest" else "Morning Clarity"
                                    val ritualIcon = if (isEvening) Icons.Rounded.Spa else Icons.Rounded.AutoAwesome

                                    Box(
                                        modifier = Modifier
                                            .onGloballyPositioned { coordinates ->
                                                ritualPillBounds = coordinates.boundsInRoot()
                                            }
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colors.accentSoft)
                                            .border(1.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                            .formaPressEffect(targetScale = 0.92f) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                if (isEvening) showEveningSheet = true else showMorningSheet = true
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = ritualIcon,
                                                contentDescription = ritualTitle,
                                                tint = colors.accent,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = ritualTitle,
                                                style = FormaTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.accent,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Row 2: Greeting headline & microcopy
                            Text(
                                text = "$greeting $userName",
                                style = FormaTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                letterSpacing = (-0.6).sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Start gently. What does today need from you?",
                                style = FormaTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Row 3: Secondary action pills — compact, equal weight
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // AI Studio pill
                                Box(
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .onGloballyPositioned { coordinates ->
                                            aiPlanBounds = coordinates.boundsInRoot()
                                        }
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.surfaceVariant)
                                        .border(1.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .formaPressEffect(targetScale = 0.93f) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            showAiStudioSheet = true
                                        }
                                        .padding(vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.AutoAwesome,
                                            contentDescription = "AI Studio",
                                            tint = colors.accent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "AI Studio",
                                            style = FormaTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                // Zen Rebalance pill
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.surfaceVariant)
                                        .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                        .formaPressEffect(targetScale = 0.93f) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.rebalanceDayTimeline()
                                        }
                                        .padding(vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Spa,
                                            contentDescription = "Rebalance",
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Rebalance",
                                            style = FormaTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                // Mindful Reflect pill
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.surfaceVariant)
                                        .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                        .formaPressEffect(targetScale = 0.93f) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            showMorningSheet = true
                                        }
                                        .padding(vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.WbSunny,
                                            contentDescription = "Reflect",
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Reflect",
                                            style = FormaTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                            // Calendar icon button (stays as compact icon only)
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.surfaceVariant)
                                    .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                    .formaPressEffect(targetScale = 0.90f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        showDatePickerDialog = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    contentDescription = "Pick Date",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Responsive 7-Day Week Buttons (Fits Whole Screen - Zero Scrolling)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                weekStripBounds = coordinates.boundsInRoot()
                            }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        weekDays.forEach { date ->
                            val isSelected = date == selectedDate
                            val isToday = date == LocalDate.now()

                            val dayName = date.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault())).uppercase()
                            val dayNum = date.dayOfMonth.toString()

                            val itemBg by animateColorAsState(
                                targetValue = if (isSelected) colors.accent else colors.surface,
                                label = "day_bg"
                            )

                            val textColor by animateColorAsState(
                                targetValue = if (isSelected) colors.onAccent else colors.textPrimary,
                                label = "day_text"
                            )

                            val subTextColor by animateColorAsState(
                                targetValue = if (isSelected) colors.onAccent.copy(alpha = 0.85f) else colors.textTertiary,
                                label = "day_subtext"
                            )

                            val borderColor = if (isSelected) colors.accent else if (isToday) colors.accent.copy(alpha = 0.5f) else colors.border.copy(alpha = 0.6f)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(68.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(itemBg)
                                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                    .formaPressEffect(targetScale = 0.93f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.selectDate(date)
                                    }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = dayName,
                                        style = FormaTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = subTextColor,
                                        fontSize = 10.5.sp,
                                        maxLines = 1
                                    )

                                    Text(
                                        text = dayNum,
                                        style = FormaTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        fontSize = 16.sp,
                                        maxLines = 1
                                    )

                                    if (isToday) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) colors.onAccent else colors.accent)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(14.dp)) }

                // 3. Hero Progress Card Banner
                val scheduleItems = daySchedule?.items ?: emptyList()
                val completedCount = scheduleItems.count { it.isCompleted }

                if (daySchedule != null && scheduleItems.isNotEmpty()) {
                    item {
                        BehanceHeroBanner(
                            completedCount = completedCount,
                            totalCount = scheduleItems.size
                        )
                    }

                    // Zen Flow Quick Action Bar
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Micro-Breathing Pill
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(colors.surface)
                                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                    .formaPressEffect(targetScale = 0.95f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        showBreathingSheet = true
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Spa,
                                        contentDescription = "Breathe",
                                        tint = colors.accent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Breathe",
                                        style = FormaTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Journal Archive Pill
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(colors.surface)
                                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                    .formaPressEffect(targetScale = 0.95f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        showJournalSheet = true
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Spa,
                                        contentDescription = "Journal",
                                        tint = colors.accent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Journal",
                                        style = FormaTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Flow Sequencer Pill
                            val habitItems = scheduleItems.filterIsInstance<TodayScheduleItem.HabitItem>()
                            if (habitItems.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(colors.accentSoft)
                                        .border(1.dp, colors.accent.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                        .formaPressEffect(targetScale = 0.95f) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            showGuidedRoutineSheet = true
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.AutoAwesome,
                                            contentDescription = "Flow Mode",
                                            tint = colors.accent,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Flow",
                                            style = FormaTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.accent,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }

                // 4. Section Label: TODAY'S RITUALS & HABITS
                item {
                    Text(
                        text = "TODAY'S RITUALS",
                        style = FormaTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textTertiary,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

                // 4b. Inline Quick Entry Bar (Instant In-Place Keyboard Activation)
                item {
                    InlineQuickEntryBar(
                        onQuickAdd = { title, isHabit ->
                            viewModel.quickAddInlineItem(title, isHabit)
                        },
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                addButtonBounds = coordinates.boundsInRoot()
                            }
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }


                item { Spacer(modifier = Modifier.height(8.dp)) }

                // 5. Mindful Habits & Tasks List
                if (daySchedule != null) {
                    if (scheduleItems.isEmpty()) {
                        item {
                            EmptyPeacefulState(
                                onAddTask = { onNavigateToAddTask(DateUtils.formatDateIso(selectedDate)) }
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = scheduleItems,
                            key = { _, item -> item.id },
                            contentType = { _, item -> item.javaClass.simpleName }
                        ) { index, scheduleItem ->
                            BehanceHabitCard(
                                item = scheduleItem,
                                modifier = Modifier.formaStaggeredEntrance(index),
                                onToggle = {
                                when (scheduleItem) {
                                    is TodayScheduleItem.HabitItem -> {
                                        if (!scheduleItem.isDoneToday) {
                                            celebrationInfo = Pair(scheduleItem.habit.name, scheduleItem.currentStreak + 1)
                                        }
                                        viewModel.toggleHabit(scheduleItem)
                                    }
                                    is TodayScheduleItem.TimelineBlock -> {
                                        if (!scheduleItem.item.completed) {
                                            celebrationInfo = Pair(scheduleItem.item.title, 1)
                                        }
                                        viewModel.toggleTask(scheduleItem)
                                    }
                                }
                            },
                            onClick = {
                                // Open detailed modal with full info, subtasks, notes, times, edit
                                selectedDetailItem = scheduleItem
                            },
                            onStartFocus = {
                                when (scheduleItem) {
                                    is TodayScheduleItem.HabitItem -> {
                                        val habit = scheduleItem.habit
                                        val duration = when (habit.timeOfDay) {
                                            com.habitflow.app.domain.model.TimeOfDay.MORNING -> 15
                                            com.habitflow.app.domain.model.TimeOfDay.AFTERNOON -> 25
                                            com.habitflow.app.domain.model.TimeOfDay.EVENING -> 20
                                            com.habitflow.app.domain.model.TimeOfDay.ANYTIME -> 20
                                        }
                                        onNavigateToFocusTimer(habit.id, habit.name, duration, true)
                                    }
                                    is TodayScheduleItem.TimelineBlock -> {
                                        val task = scheduleItem.item
                                        val startParsed = try { task.startTime?.let { LocalTime.parse(it) } } catch (_: Exception) { null }
                                        val endParsed = try { task.endTime?.let { LocalTime.parse(it) } } catch (_: Exception) { null }
                                        val duration = if (startParsed != null && endParsed != null) {
                                            ChronoUnit.MINUTES.between(startParsed, endParsed).toInt().coerceAtLeast(5)
                                        } else 25
                                        onNavigateToFocusTimer(task.id, task.title, duration, false)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

            // Top Floating Dynamic Streak Island
            DynamicStreakIsland(
                visible = celebrationInfo != null,
                habitTitle = celebrationInfo?.first ?: "",
                streakCount = celebrationInfo?.second ?: 1,
                onDismiss = { celebrationInfo = null },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    // Detail Bottom Sheet Modal
    selectedDetailItem?.let { item ->
        HabitTaskDetailBottomSheet(
            item = item,
            onDismiss = { selectedDetailItem = null },
            onToggleComplete = {
                when (item) {
                    is TodayScheduleItem.HabitItem -> {
                        if (!item.isDoneToday) {
                            celebrationInfo = Pair(item.habit.name, item.currentStreak + 1)
                        }
                        viewModel.toggleHabit(item)
                    }
                    is TodayScheduleItem.TimelineBlock -> {
                        if (!item.item.completed) {
                            celebrationInfo = Pair(item.item.title, 1)
                        }
                        viewModel.toggleTask(item)
                    }
                }
                selectedDetailItem = null
            },
            onEdit = {
                selectedDetailItem = null
                when (item) {
                    is TodayScheduleItem.HabitItem -> onNavigateToHabits()
                    is TodayScheduleItem.TimelineBlock -> onNavigateToEditTask(item.item.id)
                }
            },
            onStartFocus = {
                selectedDetailItem = null
                when (item) {
                    is TodayScheduleItem.HabitItem -> {
                        val habit = item.habit
                        val duration = when (habit.timeOfDay) {
                            com.habitflow.app.domain.model.TimeOfDay.MORNING -> 15
                            com.habitflow.app.domain.model.TimeOfDay.AFTERNOON -> 25
                            com.habitflow.app.domain.model.TimeOfDay.EVENING -> 20
                            com.habitflow.app.domain.model.TimeOfDay.ANYTIME -> 20
                        }
                        onNavigateToFocusTimer(habit.id, habit.name, duration, true)
                    }
                    is TodayScheduleItem.TimelineBlock -> {
                        val task = item.item
                        val startParsed = try { task.startTime?.let { LocalTime.parse(it) } } catch (_: Exception) { null }
                        val endParsed = try { task.endTime?.let { LocalTime.parse(it) } } catch (_: Exception) { null }
                        val duration = if (startParsed != null && endParsed != null) {
                            ChronoUnit.MINUTES.between(startParsed, endParsed).toInt().coerceAtLeast(5)
                        } else 25
                        onNavigateToFocusTimer(task.id, task.title, duration, false)
                    }
                }
            },
            onToggleSubtask = { subtaskId ->
                if (item is TodayScheduleItem.TimelineBlock) {
                    viewModel.toggleSubtask(item.item, subtaskId)
                }
            }
        )
    }

    // Calendar Date Picker Modal
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.selectDate(picked)
                    }
                    showDatePickerDialog = false
                }) {
                    Text("OK", color = colors.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showPaywall) {
        ProPaywallBottomSheet(onDismiss = { showPaywall = false })
    }

    if (showAiPresetSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAiPresetSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface,
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PLAN DAY WITH AI",
                            style = FormaTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(
                        onClick = { showAiPresetSheet = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Select an Architectural Rhythm",
                    style = FormaTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontSize = 16.sp
                )
                Text(
                    text = "Forma AI will generate energy-balanced time blocks based on your goal.",
                    style = FormaTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                AiPlanPreset.entries.forEach { preset ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors.surfaceVariant)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.requestAiDayPlan(preset)
                                showAiPresetSheet = false
                            }
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.accentSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (preset) {
                                        AiPlanPreset.DEEP_WORK -> Icons.Rounded.Timer
                                        AiPlanPreset.HEALTH_BALANCE -> Icons.Rounded.Spa
                                        AiPlanPreset.EXAM_STUDY -> Icons.Rounded.LocalFireDepartment
                                        AiPlanPreset.PRODUCTIVITY_SPRINT -> Icons.Rounded.Bolt
                                        AiPlanPreset.CREATIVE_FLOW -> Icons.Rounded.Brush
                                        AiPlanPreset.MINDFUL_WEEKEND -> Icons.Rounded.WbSunny
                                    },
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.title,
                                    style = FormaTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = preset.subtitle,
                                    style = FormaTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }

    if (showMorningSheet) {
        val reflectionViewModel: com.habitflow.app.ui.reflection.DailyReflectionViewModel = hiltViewModel()
        com.habitflow.app.ui.reflection.MorningAlignmentSheet(
            viewModel = reflectionViewModel,
            onDismiss = { showMorningSheet = false }
        )
    }

    if (showEveningSheet) {
        val reflectionViewModel: com.habitflow.app.ui.reflection.DailyReflectionViewModel = hiltViewModel()
        com.habitflow.app.ui.reflection.EveningReflectionSheet(
            viewModel = reflectionViewModel,
            onDismiss = { showEveningSheet = false }
        )
    }

    if (showBreathingSheet) {
        com.habitflow.app.ui.mindfulness.BreathingExerciseSheet(
            onDismiss = { showBreathingSheet = false }
        )
    }

    if (showGuidedRoutineSheet) {
        val habitItems = daySchedule?.items?.filterIsInstance<TodayScheduleItem.HabitItem>() ?: emptyList()
        com.habitflow.app.ui.mindfulness.GuidedRoutineSheet(
            habits = habitItems,
            onCompleteHabit = { habitId ->
                viewModel.completeHabitById(habitId)
            },
            onDismiss = { showGuidedRoutineSheet = false }
        )
    }

    if (showJournalSheet) {
        val reflectionViewModel: com.habitflow.app.ui.reflection.DailyReflectionViewModel = hiltViewModel()
        com.habitflow.app.ui.reflection.ReflectionJournalSheet(
            viewModel = reflectionViewModel,
            onDismiss = { showJournalSheet = false }
        )
    }

    if (showAiStudioSheet) {
        com.habitflow.app.ui.today.components.AiStudioSheet(
            viewModel = viewModel,
            onDismiss = { showAiStudioSheet = false }
        )
    }

    // First-Launch Feature Spotlight / Coach Marks Tour
    if (!hasSeenTodayCoachMarks) {
        TodayCoachMarksOverlay(
            weekStripBounds = weekStripBounds,
            ritualPillBounds = ritualPillBounds,
            aiPlanBounds = aiPlanBounds,
            addButtonBounds = addButtonBounds,
            onDismiss = { viewModel.dismissCoachMarks() }
        )
    }
}
}



@Composable
fun EmptyPeacefulState(
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FormaTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(colors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "A clean slate.",
            style = FormaTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Add a quiet moment to anchor your day.\nTake a breath or create a mindful intention.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(colors.accent)
                .clickable { onAddTask() }
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "+ Add Mindful Ritual",
                style = FormaTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onAccent,
                fontSize = 13.sp
            )
        }
    }
}
