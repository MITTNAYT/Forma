package com.forma.app.ui.today

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
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Nature
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import com.forma.app.ui.today.components.AiStudioSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.forma.app.core.designsystem.motion.FormaMotion
import com.forma.app.core.designsystem.motion.formaPressEffect
import com.forma.app.core.designsystem.motion.formaStaggeredEntrance
import com.forma.app.domain.repository.AiPlanPreset
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
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.util.DateUtils
import com.forma.app.domain.model.TodayScheduleItem
import com.forma.app.domain.model.TimeOfDay
import com.forma.app.core.designsystem.icon.FormaIcon
import com.forma.app.ui.settings.components.ProPaywallBottomSheet
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.forma.app.ui.today.components.BehanceHabitCard
import com.forma.app.ui.today.components.BehanceHeroBanner
import com.forma.app.ui.today.components.DynamicStreakIsland
import com.forma.app.ui.today.components.HabitTaskDetailBottomSheet
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
import com.forma.app.ui.today.components.TodayCoachMarksOverlay
import com.forma.app.ui.today.components.EmptyPeacefulState
import com.forma.app.ui.today.components.TodayFilterChip

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
    var selectedDetailItem by remember { mutableStateOf<TodayScheduleItem?>(null) }
    var editingHabitId by remember { mutableStateOf<String?>(null) }
    var selectedTimeFilter by remember { mutableStateOf<TimeOfDay?>(null) }
    var showTemplatesSheet by remember { mutableStateOf(false) }
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
                val scheduleItems = daySchedule?.items ?: emptyList()
                val displayedItems = remember(scheduleItems, selectedTimeFilter) {
                    if (selectedTimeFilter == null) {
                        scheduleItems
                    } else {
                        scheduleItems.filter { item ->
                            when (item) {
                                is TodayScheduleItem.HabitItem -> item.habit.timeOfDay == selectedTimeFilter
                                is TodayScheduleItem.TimelineBlock -> {
                                    val hour = try {
                                        item.item.startTime?.let { LocalTime.parse(it).hour } ?: 12
                                    } catch (_: Exception) { 12 }
                                    when (selectedTimeFilter) {
                                        TimeOfDay.MORNING -> hour in 4..11
                                        TimeOfDay.AFTERNOON -> hour in 12..16
                                        TimeOfDay.EVENING -> hour in 17..23
                                        TimeOfDay.ANYTIME -> false
                                        null -> true
                                    }
                                }
                            }
                        }
                    }
                }

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
                                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp)
                        ) {
                            // Row 1: Date & Week metadata on left + sleek AI Studio & Calendar Picker on right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val weekOfYear = try {
                                    selectedDate.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)
                                } catch (_: Exception) { 1 }
                                val dateShortFormatted = selectedDate.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())).uppercase()
                                Text(
                                    text = "$dateShortFormatted · WEEK $weekOfYear",
                                    style = FormaTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    letterSpacing = 1.4.sp,
                                    fontSize = 11.sp
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Calendar icon button
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colors.surfaceVariant)
                                            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
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
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Row 2: Swiss Editorial "TODAY" headline + Refined Progress Badge
                            val completedCount = scheduleItems.count { it.isCompleted }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TODAY",
                                    style = FormaTheme.typography.displayLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 32.sp,
                                        letterSpacing = (-1.2).sp
                                    ),
                                    color = colors.textPrimary
                                )

                                if (scheduleItems.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.accentSoft)
                                            .border(1.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 9.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "$completedCount of ${scheduleItems.size} done",
                                            style = FormaTheme.typography.labelSmall.copy(
                                                fontFeatureSettings = "tnum",
                                                platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            color = colors.accent,
                                            fontSize = 10.sp,
                                            letterSpacing = 0.3.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            // Greeting subtext
                            Text(
                                text = if (userName.isNotBlank()) "$greeting $userName" else greeting,
                                style = FormaTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                fontSize = 13.sp
                            )
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
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                                        style = FormaTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
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

                item { Spacer(modifier = Modifier.height(12.dp)) }

                // 3. Hero Progress Card Banner
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
                                    FormaIcon(
                                        iconKey = "journal",
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

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // 4. Section Header with "TODAY'S FLOW" + Progress Badge + Quick "Templates" action
                item {
                    val completedCount = scheduleItems.count { it.isCompleted }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TODAY'S FLOW",
                                style = FormaTheme.typography.labelSmall.copy(
                                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                                ),
                                fontWeight = FontWeight.Bold,
                                color = colors.accent,
                                letterSpacing = 1.3.sp,
                                fontSize = 11.sp
                            )
                            if (scheduleItems.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.accentSoft)
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$completedCount/${scheduleItems.size}",
                                        style = FormaTheme.typography.labelSmall.copy(
                                            fontFeatureSettings = "tnum",
                                            platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = colors.accent,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.3.sp
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surfaceVariant)
                                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .formaPressEffect(targetScale = 0.92f) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showTemplatesSheet = true
                                }
                                .padding(horizontal = 11.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FormaIcon(
                                    iconKey = "sparkles",
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Templates",
                                    style = FormaTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // 5. Unified Integrated Segmented Control (All, Morning, Afternoon, Evening together with zero scroll)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(3.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val filters = listOf<Pair<String, TimeOfDay?>>(
                                "All" to null,
                                "Morning" to TimeOfDay.MORNING,
                                "Afternoon" to TimeOfDay.AFTERNOON,
                                "Evening" to TimeOfDay.EVENING
                            )

                            filters.forEach { (label, tod) ->
                                val isSelected = selectedTimeFilter == tod
                                val countForTod = if (tod == null) {
                                    scheduleItems.size
                                } else {
                                    scheduleItems.count { item ->
                                        when (item) {
                                            is TodayScheduleItem.HabitItem -> item.habit.timeOfDay == tod
                                            is TodayScheduleItem.TimelineBlock -> {
                                                val hour = try {
                                                    item.item.startTime?.let { java.time.LocalTime.parse(it).hour } ?: 12
                                                } catch (_: Exception) { 12 }
                                                when (tod) {
                                                    TimeOfDay.MORNING -> hour in 4..11
                                                    TimeOfDay.AFTERNOON -> hour in 12..16
                                                    TimeOfDay.EVENING -> hour in 17..23
                                                    TimeOfDay.ANYTIME -> true
                                                }
                                            }
                                        }
                                    }
                                }

                                val segBg by animateColorAsState(
                                    targetValue = if (isSelected) colors.accent else Color.Transparent,
                                    animationSpec = tween(180),
                                    label = "seg_bg_$label"
                                )
                                val segFg by animateColorAsState(
                                    targetValue = if (isSelected) colors.onAccent else colors.textSecondary,
                                    animationSpec = tween(180),
                                    label = "seg_fg_$label"
                                )
                                val badgeBg by animateColorAsState(
                                    targetValue = if (isSelected) colors.onAccent.copy(alpha = 0.22f) else colors.accentSoft,
                                    animationSpec = tween(180),
                                    label = "seg_badge_bg_$label"
                                )
                                val badgeFg by animateColorAsState(
                                    targetValue = if (isSelected) colors.onAccent else colors.accent,
                                    animationSpec = tween(180),
                                    label = "seg_badge_fg_$label"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(segBg)
                                        .formaPressEffect(targetScale = 0.95f) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedTimeFilter = tod
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = FormaTheme.typography.labelSmall.copy(
                                                platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                                            ),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = segFg,
                                            fontSize = 11.5.sp,
                                            maxLines = 1
                                        )
                                        if (countForTod > 0) {
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(badgeBg)
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "$countForTod",
                                                    style = FormaTheme.typography.labelSmall.copy(
                                                        fontFeatureSettings = "tnum",
                                                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                                                    ),
                                                    fontWeight = FontWeight.Bold,
                                                    color = badgeFg,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(6.dp)) }

                // 6. Mindful Habits & Tasks List (Filtered)
                if (daySchedule != null) {
                    if (displayedItems.isEmpty()) {
                        item {
                            EmptyPeacefulState(
                                onAddTask = { onNavigateToAddTask(DateUtils.formatDateIso(selectedDate)) }
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = displayedItems,
                            key = { _, item -> item.id },
                            contentType = { _, item -> item.javaClass.simpleName }
                        ) { index, scheduleItem ->
                            BehanceHabitCard(
                                item = scheduleItem,
                                index = index,
                                modifier = Modifier,
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
                                    selectedDetailItem = scheduleItem
                                },
                                onStartFocus = {
                                    when (scheduleItem) {
                                        is TodayScheduleItem.HabitItem -> {
                                            val habit = scheduleItem.habit
                                            val duration = when (habit.timeOfDay) {
                                                TimeOfDay.MORNING -> 15
                                                TimeOfDay.AFTERNOON -> 25
                                                TimeOfDay.EVENING -> 20
                                                TimeOfDay.ANYTIME -> 20
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
                                },
                                onSkip = {
                                    if (scheduleItem is TodayScheduleItem.HabitItem) {
                                        viewModel.skipHabit(scheduleItem.habit.id, scheduleItem.habit.name)
                                    }
                                },
                                onUnskip = {
                                    if (scheduleItem is TodayScheduleItem.HabitItem) {
                                        viewModel.unskipHabit(scheduleItem.habit.id)
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
                val currentItem = item
                selectedDetailItem = null
                when (currentItem) {
                    is TodayScheduleItem.HabitItem -> {
                        editingHabitId = currentItem.habit.id
                    }
                    is TodayScheduleItem.TimelineBlock -> onNavigateToEditTask(currentItem.item.id)
                }
            },
            onStartFocus = {
                selectedDetailItem = null
                when (item) {
                    is TodayScheduleItem.HabitItem -> {
                        val habit = item.habit
                        val duration = when (habit.timeOfDay) {
                            com.forma.app.domain.model.TimeOfDay.MORNING -> 15
                            com.forma.app.domain.model.TimeOfDay.AFTERNOON -> 25
                            com.forma.app.domain.model.TimeOfDay.EVENING -> 20
                            com.forma.app.domain.model.TimeOfDay.ANYTIME -> 20
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
                when (item) {
                    is TodayScheduleItem.TimelineBlock -> viewModel.toggleSubtask(item.item, subtaskId)
                    is TodayScheduleItem.HabitItem -> viewModel.toggleHabitSubtask(item.habit, subtaskId)
                }
            },
            onTogglePause = {
                if (item is TodayScheduleItem.HabitItem) {
                    viewModel.toggleHabitPause(item.habit)
                }
            },
            onSkipHabit = {
                if (item is TodayScheduleItem.HabitItem) {
                    viewModel.skipHabit(item.habit.id, item.habit.name)
                }
            }
        )
    }

    // Bespoke Forma Sanctuary Calendar Modal Sheet
    if (showDatePickerDialog) {
        com.forma.app.ui.today.components.FormaCalendarSheet(
            selectedDate = selectedDate,
            onDateSelected = { pickedDate ->
                viewModel.selectDate(pickedDate)
            },
            onDismiss = { showDatePickerDialog = false }
        )
    }

    if (showPaywall) {
        ProPaywallBottomSheet(onDismiss = { showPaywall = false })
    }

    if (showAiPresetSheet) {
        com.forma.app.ui.today.components.AiPlanPresetBottomSheet(
            onSelectPreset = { preset -> viewModel.requestAiDayPlan(preset) },
            onDismiss = { showAiPresetSheet = false }
        )
    }

    if (showMorningSheet) {
        val reflectionViewModel: com.forma.app.ui.reflection.DailyReflectionViewModel = hiltViewModel()
        com.forma.app.ui.reflection.MorningAlignmentSheet(
            viewModel = reflectionViewModel,
            onDismiss = { showMorningSheet = false }
        )
    }

    if (showEveningSheet) {
        val reflectionViewModel: com.forma.app.ui.reflection.DailyReflectionViewModel = hiltViewModel()
        com.forma.app.ui.reflection.EveningReflectionSheet(
            viewModel = reflectionViewModel,
            onDismiss = { showEveningSheet = false }
        )
    }

    if (showBreathingSheet) {
        com.forma.app.ui.mindfulness.BreathingExerciseSheet(
            onDismiss = { showBreathingSheet = false }
        )
    }

    if (showGuidedRoutineSheet) {
        val habitItems = daySchedule?.items?.filterIsInstance<TodayScheduleItem.HabitItem>() ?: emptyList()
        com.forma.app.ui.mindfulness.GuidedRoutineSheet(
            habits = habitItems,
            onCompleteHabit = { habitId ->
                viewModel.completeHabitById(habitId)
            },
            onDismiss = { showGuidedRoutineSheet = false }
        )
    }

    if (showJournalSheet) {
        val reflectionViewModel: com.forma.app.ui.reflection.DailyReflectionViewModel = hiltViewModel()
        com.forma.app.ui.reflection.ReflectionJournalSheet(
            viewModel = reflectionViewModel,
            onDismiss = { showJournalSheet = false }
        )
    }


    editingHabitId?.let { habitId ->
        com.forma.app.ui.timeline.components.AddEditTimelineSheet(
            itemId = habitId,
            initialCreationType = com.forma.app.ui.timeline.CreationType.HABIT,
            onDismiss = { editingHabitId = null }
        )
    }

    if (showTemplatesSheet) {
        com.forma.app.ui.habits.components.HabitTemplatesSheet(
            onAddHabit = { habit ->
                viewModel.addHabit(habit)
                showTemplatesSheet = false
            },
            onDismiss = { showTemplatesSheet = false }
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

