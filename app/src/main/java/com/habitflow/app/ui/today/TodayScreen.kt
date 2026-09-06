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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.habitflow.app.core.designsystem.motion.formaPressEffect
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
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.util.DateUtils
import com.habitflow.app.domain.model.TodayScheduleItem
import com.habitflow.app.ui.settings.components.ProPaywallBottomSheet
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
    val colors = NotionTheme.colors

    val selectedDate by viewModel.selectedDate.collectAsState()
    val daySchedule by viewModel.daySchedule.collectAsState()
    val userName by viewModel.userName.collectAsState()

    var showPaywall by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showAiPresetSheet by remember { mutableStateOf(false) }
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

    Scaffold(
        containerColor = colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 12.dp)
                    ) {
                        Text(
                            text = dateFormatted.uppercase(),
                            style = NotionTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$greeting $userName",
                                style = NotionTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 28.sp,
                                letterSpacing = (-0.5).sp
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // AI Plan Pill Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.accentSoft)
                                        .border(1.dp, colors.accent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .formaPressEffect(targetScale = 0.92f) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            showAiPresetSheet = true
                                        }
                                        .padding(horizontal = 10.dp, vertical = 7.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.AutoAwesome,
                                            contentDescription = "AI Plan",
                                            tint = colors.accent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "AI Plan",
                                            style = NotionTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.accent,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Calendar Icon
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(colors.surface)
                                        .border(1.dp, colors.border.copy(alpha = 0.6f), CircleShape)
                                        .formaPressEffect(targetScale = 0.90f) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            showDatePickerDialog = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CalendarMonth,
                                        contentDescription = "Pick Date",
                                        tint = colors.textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Responsive 7-Day Week Buttons (Fits Whole Screen - Zero Scrolling)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                        style = NotionTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = subTextColor,
                                        fontSize = 10.5.sp,
                                        maxLines = 1
                                    )

                                    Text(
                                        text = dayNum,
                                        style = NotionTheme.typography.titleMedium,
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
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // 4. Section Label: TODAY'S RITUALS & HABITS
                item {
                    Text(
                        text = "TODAY'S RITUALS",
                        style = NotionTheme.typography.labelSmall,
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

                // 5. Mindful Habits & Tasks List
                if (daySchedule != null) {
                    if (scheduleItems.isEmpty()) {
                        item {
                            EmptyPeacefulState(
                                onAddTask = { onNavigateToAddTask(DateUtils.formatDateIso(selectedDate)) }
                            )
                        }
                    } else {
                    items(scheduleItems, key = { it.id }) { scheduleItem ->
                        BehanceHabitCard(
                            item = scheduleItem,
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
                            style = NotionTheme.typography.labelSmall,
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
                    style = NotionTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontSize = 16.sp
                )
                Text(
                    text = "Forma AI will generate energy-balanced time blocks based on your goal.",
                    style = NotionTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                AiPlanPreset.values().forEach { preset ->
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
                                    style = NotionTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = preset.subtitle,
                                    style = NotionTheme.typography.bodySmall,
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
}

@Composable
fun EmptyPeacefulState(
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = NotionTheme.colors

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
            text = "A Peaceful Clean Slate",
            style = NotionTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "No rituals scheduled for this day.\nTake a breath or create a mindful intention.",
            style = NotionTheme.typography.bodyMedium,
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
                style = NotionTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onAccent,
                fontSize = 13.sp
            )
        }
    }
}
