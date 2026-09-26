package com.forma.app.ui.timeline.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.ui.text.PlatformTextStyle
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.component.FormaBottomSheet
import com.forma.app.core.designsystem.component.FormaButton
import com.forma.app.core.designsystem.component.FormaButtonStyle
import com.forma.app.core.designsystem.icon.FormaIcon
import com.forma.app.core.designsystem.motion.formaPressEffect
import com.forma.app.domain.model.EnergyLevel
import com.forma.app.domain.model.TimeOfDay
import com.forma.app.ui.timeline.AddEditTimelineEvent
import com.forma.app.ui.timeline.AddEditTimelineViewModel
import com.forma.app.ui.timeline.CreationType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTimelineSheet(
    itemId: String? = null,
    selectedDate: String? = null,
    initialCreationType: CreationType? = null,
    onDismiss: () -> Unit,
    viewModel: AddEditTimelineViewModel = hiltViewModel()
) {
    val colors = FormaTheme.colors
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(itemId, selectedDate) {
        viewModel.initialize(itemId, selectedDate)
        if (initialCreationType != null && itemId == null) {
            viewModel.setCreationType(initialCreationType)
        }
    }

    var showIconPicker by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showHabitReminderTimePicker by remember { mutableStateOf(false) }
    var newSubtaskText by remember { mutableStateOf("") }
    var showAddSubtaskField by remember { mutableStateOf(false) }

    val parsedColor = if (!uiState.colorTag.isNullOrBlank()) {
        try {
            Color(android.graphics.Color.parseColor(uiState.colorTag))
        } catch (_: Exception) {
            colors.accent
        }
    } else {
        colors.accent
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is AddEditTimelineEvent.Saved, is AddEditTimelineEvent.Deleted -> onDismiss()
            }
        }
    }

    val habitSuggestions = listOf(
        Pair("Daily Hydration", "water"),
        Pair("Read 20 Pages", "book"),
        Pair("Morning Sun & Walk", "sun"),
        Pair("Meditation & Breath", "zen"),
        Pair("Deep Code Sprint", "code"),
        Pair("Evening Reflection", "journal")
    )

    val taskSuggestions = listOf(
        Pair("Project Milestone", "work"),
        Pair("Grocery & Nutrition", "food"),
        Pair("Workout Session", "gym"),
        Pair("Team Sync & Review", "computer")
    )

    FormaBottomSheet(
        onDismissRequest = onDismiss,
        title = if (uiState.isEditMode) {
            if (uiState.creationType == CreationType.HABIT) "Edit Habit" else "Edit Intention"
        } else {
            if (uiState.creationType == CreationType.HABIT) "New Habit" else "New Intention"
        },
        subtitle = if (uiState.creationType == CreationType.HABIT) "Unlimited recurring habit & streak tracking" else "Focused single-day intention",
        trailingAction = {
            if (uiState.isEditMode) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935).copy(alpha = 0.12f))
                        .formaPressEffect(targetScale = 0.90f) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.delete()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
    ) {
        // 1. Top Mode Switcher (Habit vs Task)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surfaceVariant.copy(alpha = 0.6f))
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .padding(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    Pair(CreationType.HABIT, "Daily Habit (Recurring)"),
                    Pair(CreationType.TASK, "Intention (One-Time)")
                ).forEach { (type, label) ->
                    val isSelected = uiState.creationType == type
                    val bg by animateColorAsState(
                        targetValue = if (isSelected) parsedColor else Color.Transparent,
                        animationSpec = tween(durationMillis = 200),
                        label = "type_bg"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else colors.textSecondary,
                        animationSpec = tween(durationMillis = 200),
                        label = "type_text"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(bg)
                            .formaPressEffect(targetScale = 0.95f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.setCreationType(type)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = FormaTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Name, Icon & Color Swatches Card
        var isTitleFocused by remember { mutableStateOf(false) }
        val cardBorderColor by animateColorAsState(
            targetValue = if (isTitleFocused) parsedColor.copy(alpha = 0.7f) else colors.border.copy(alpha = 0.6f),
            label = "card_border"
        )
        val cardBorderWidth by animateDpAsState(
            targetValue = if (isTitleFocused) 1.5.dp else 1.dp,
            label = "card_border_w"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surface)
                .border(cardBorderWidth, cardBorderColor, RoundedCornerShape(22.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Icon selector squircle
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(parsedColor.copy(alpha = 0.15f))
                            .border(1.5.dp, parsedColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .formaPressEffect(targetScale = 0.92f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showIconPicker = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        FormaIcon(
                            iconKey = uiState.icon,
                            contentDescription = "Select Icon",
                            tint = parsedColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title Input with perfectly aligned cursor and font metrics
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = uiState.title,
                            onValueChange = { viewModel.setTitle(it) },
                            singleLine = true,
                            cursorBrush = SolidColor(parsedColor),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                autoCorrectEnabled = true,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            textStyle = FormaTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                color = colors.textPrimary,
                                lineHeight = 20.sp
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { isTitleFocused = it.isFocused },
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (uiState.title.isEmpty()) {
                                        Text(
                                            text = if (uiState.creationType == CreationType.HABIT) "Name your daily habit..." else "What is the intention?",
                                            style = FormaTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                                lineHeight = 20.sp
                                            ),
                                            color = colors.textTertiary
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        AnimatedVisibility(
                            visible = uiState.title.isNotBlank(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setTitle("")
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear text",
                                    tint = colors.textTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Color Swatch Bar
                Text(
                    text = "COLOR ACCENT",
                    style = FormaTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent,
                    letterSpacing = 1.sp,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Theme Default Accent Bubble
                    val isThemePicked = uiState.colorTag.isNullOrBlank()
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                            .border(
                                width = if (isThemePicked) 2.5.dp else 1.dp,
                                color = if (isThemePicked) colors.textPrimary else colors.accent.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                            .formaPressEffect(targetScale = 0.88f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.setColorTag(null)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isThemePicked) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Theme Accent",
                                tint = colors.onAccent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    ZenColorPalette.forEach { swatch ->
                        ColorSwatchBubble(
                            hex = swatch.first,
                            name = swatch.second,
                            isSelected = swatch.first.equals(uiState.colorTag, ignoreCase = true),
                            onSelect = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.setColorTag(swatch.first)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Suggestions Horizontal Row
        val activeSuggestions = if (uiState.creationType == CreationType.HABIT) habitSuggestions else taskSuggestions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            activeSuggestions.forEach { suggestion ->
                SuggestionChipBubble(
                    title = suggestion.first,
                    icon = suggestion.second,
                    tintColor = parsedColor,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.setTitle(suggestion.first)
                        viewModel.setIcon(suggestion.second)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // 3. HABIT SPECIFIC CONFIGURATION
        // ==========================================
        if (uiState.creationType == CreationType.HABIT) {
            // A. Time of Day & Energy Level Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "TIME OF DAY",
                        style = FormaTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        letterSpacing = 1.2.sp,
                        fontSize = 10.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple(TimeOfDay.MORNING, "Morning", Icons.Rounded.WbSunny),
                            Triple(TimeOfDay.AFTERNOON, "Afternoon", Icons.Rounded.WbTwilight),
                            Triple(TimeOfDay.EVENING, "Evening", Icons.Rounded.NightsStay),
                            Triple(TimeOfDay.ANYTIME, "Anytime", Icons.Rounded.Spa)
                        ).forEach { (tod, label, icon) ->
                            val isSelected = uiState.timeOfDay == tod
                            val todBg by animateColorAsState(
                                targetValue = if (isSelected) parsedColor.copy(alpha = 0.15f) else colors.surfaceVariant,
                                label = "tod_bg"
                            )
                            val todBorder = if (isSelected) parsedColor else Color.Transparent

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(todBg)
                                    .border(1.dp, todBorder, RoundedCornerShape(12.dp))
                                    .formaPressEffect(targetScale = 0.93f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setTimeOfDay(tod)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) parsedColor else colors.textTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = label,
                                        style = FormaTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) parsedColor else colors.textSecondary,
                                        fontSize = 10.5.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Energy Level Selector
                    Text(
                        text = "ENERGY PROFILE",
                        style = FormaTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        letterSpacing = 1.2.sp,
                        fontSize = 10.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(EnergyLevel.HIGH, "High Focus", Icons.Rounded.Bolt),
                            Triple(EnergyLevel.MEDIUM, "Balanced", Icons.Rounded.SelfImprovement),
                            Triple(EnergyLevel.LOW, "Restorative", Icons.Rounded.Spa)
                        ).forEach { (energy, label, icon) ->
                            val isSelected = uiState.energyLevel == energy
                            val energyBg by animateColorAsState(
                                targetValue = if (isSelected) parsedColor.copy(alpha = 0.15f) else colors.surfaceVariant,
                                label = "energy_bg"
                            )
                            val energyBorder = if (isSelected) parsedColor else Color.Transparent

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(energyBg)
                                    .border(1.dp, energyBorder, RoundedCornerShape(12.dp))
                                    .formaPressEffect(targetScale = 0.93f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setEnergyLevel(energy)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) parsedColor else colors.textTertiary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = label,
                                        style = FormaTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) parsedColor else colors.textSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // B. Frequency & Repeat Days
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "REPEAT DAYS",
                            style = FormaTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            letterSpacing = 1.2.sp,
                            fontSize = 10.5.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Every Day",
                                style = FormaTheme.typography.labelSmall,
                                color = parsedColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.setRecurrenceType("DAILY")
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Weekdays",
                                style = FormaTheme.typography.labelSmall,
                                color = colors.textTertiary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.setRecurrenceType("WEEKDAYS")
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val dayLetters = listOf("M", "T", "W", "T", "F", "S", "S")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..7).forEach { dayNum ->
                            val isSelected = uiState.repeatDays.contains(dayNum)
                            val dayBg by animateColorAsState(
                                targetValue = if (isSelected) parsedColor else colors.surfaceVariant,
                                label = "day_bg"
                            )
                            val dayTextColor = if (isSelected) Color.White else colors.textTertiary

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(dayBg)
                                    .formaPressEffect(targetScale = 0.90f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.toggleRepeatDay(dayNum)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayLetters[dayNum - 1],
                                    style = FormaTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = dayTextColor,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // C. Habit Stacking Cue Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Link,
                            contentDescription = null,
                            tint = parsedColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HABIT STACKING CUE (OPTIONAL)",
                            style = FormaTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            letterSpacing = 1.2.sp,
                            fontSize = 10.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.stackedCueText,
                        onValueChange = { viewModel.setStackedCueText(it) },
                        placeholder = {
                            Text(
                                text = "e.g. After I brew morning coffee, I will...",
                                style = FormaTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                ),
                                color = colors.textTertiary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = parsedColor,
                            unfocusedBorderColor = colors.border.copy(alpha = 0.6f),
                            focusedContainerColor = colors.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = colors.surfaceVariant.copy(alpha = 0.3f),
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            cursorColor = parsedColor
                        ),
                        textStyle = FormaTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp,
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // D. Timeline Span & Ongoing Indefinite Switch
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Ongoing Indefinite",
                                style = FormaTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 14.5.sp
                            )
                            Text(
                                text = "Keep this habit active permanently",
                                style = FormaTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Switch(
                            checked = uiState.isIndefinite,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.setIsIndefinite(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = parsedColor
                            )
                        )
                    }

                    if (!uiState.isIndefinite) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Start Date
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.surfaceVariant)
                                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable { showStartDatePicker = true }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    Text("START", style = FormaTheme.typography.labelSmall, color = colors.textTertiary, fontSize = 9.sp)
                                    Text(uiState.startDate, style = FormaTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                }
                            }

                            // End Date
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.surfaceVariant)
                                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable { showEndDatePicker = true }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    Text("END", style = FormaTheme.typography.labelSmall, color = colors.textTertiary, fontSize = 9.sp)
                                    Text(uiState.endDate ?: "Pick Date", style = FormaTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (uiState.endDate != null) colors.textPrimary else parsedColor)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // E. Wintering Mode Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (uiState.isWintering) colors.accentSoft.copy(alpha = 0.5f) else colors.surface)
                    .border(
                        width = 1.dp,
                        color = if (uiState.isWintering) colors.accent.copy(alpha = 0.45f) else colors.border.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (uiState.isWintering) colors.accent else colors.accentSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AcUnit,
                                contentDescription = null,
                                tint = if (uiState.isWintering) colors.onAccent else colors.accent,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Wintering Mode",
                                    style = FormaTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isWintering) colors.accent else colors.textPrimary,
                                    fontSize = 14.5.sp
                                )
                                if (uiState.isWintering) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(colors.accent)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "FROZEN",
                                            fontWeight = FontWeight.Bold,
                                            color = colors.onAccent,
                                            fontSize = 9.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (uiState.isWintering)
                                    "Streak is mindfully frozen & protected from breaking"
                                else
                                    "Mindfully freeze streaks during recovery, travel, or illness",
                                style = FormaTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = uiState.isWintering,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.setIsWintering(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onAccent,
                            checkedTrackColor = colors.accent,
                            uncheckedThumbColor = colors.textTertiary,
                            uncheckedTrackColor = colors.surfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // F. Daily Reminder Notification
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Notifications,
                                contentDescription = null,
                                tint = parsedColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily Alert Notification",
                                style = FormaTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 14.5.sp
                            )
                        }

                        Switch(
                            checked = uiState.hasReminder,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.setHasReminder(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = parsedColor
                            )
                        )
                    }

                    if (uiState.hasReminder) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", uiState.reminderHour, uiState.reminderMinute)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceVariant)
                                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable { showHabitReminderTimePicker = true }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Alert Time", style = FormaTheme.typography.bodyMedium, color = colors.textSecondary)
                                Text(formattedTime, style = FormaTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = parsedColor)
                            }
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // 4. TASK / INTENTION SPECIFIC CONFIGURATION
            // ==========================================
            // A. Date & Schedule Block Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SCHEDULE DATE",
                            style = FormaTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            letterSpacing = 1.2.sp,
                            fontSize = 10.5.sp
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surfaceVariant)
                                .clickable { showDatePickerDialog = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    contentDescription = null,
                                    tint = parsedColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.date,
                                    style = FormaTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Time toggle & Duration presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Start Time
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceVariant)
                                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable { showStartTimePicker = true }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("START", style = FormaTheme.typography.labelSmall, color = colors.textTertiary, fontSize = 9.sp)
                                Text(uiState.startTime, style = FormaTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            }
                        }

                        // End Time
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceVariant)
                                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable { showEndTimePicker = true }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("END", style = FormaTheme.typography.labelSmall, color = colors.textTertiary, fontSize = 9.sp)
                                Text(uiState.endTime, style = FormaTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Duration Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(15, 25, 45, 60, 90).forEach { mins ->
                            val isSelected = uiState.durationMinutes == mins
                            val durBg by animateColorAsState(
                                targetValue = if (isSelected) parsedColor else colors.surfaceVariant,
                                label = "dur_bg"
                            )
                            val durText = if (isSelected) Color.White else colors.textSecondary

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(durBg)
                                    .formaPressEffect(targetScale = 0.92f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setDuration(mins)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${mins}m",
                                    style = FormaTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = durText,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4b. Shared Micro-Steps & Subtasks Checklist Builder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surface)
                .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.creationType == CreationType.HABIT) "HABIT MICRO-STEPS & CHECKLIST" else "MILESTONES & SUBTASKS",
                        style = FormaTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        letterSpacing = 1.2.sp,
                        fontSize = 10.5.sp
                    )

                    // Clear, visible, high-contrast Add Micro-Step Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(parsedColor.copy(alpha = 0.14f))
                            .border(1.dp, parsedColor.copy(alpha = 0.40f), RoundedCornerShape(10.dp))
                            .formaPressEffect(targetScale = 0.92f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showAddSubtaskField = true
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = parsedColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.creationType == CreationType.HABIT) "Add Micro-Step" else "Add Step",
                                style = FormaTheme.typography.labelSmall.copy(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                ),
                                color = parsedColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                if (uiState.subtasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    uiState.subtasks.forEach { subtask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (subtask.completed) parsedColor else colors.surfaceVariant)
                                    .border(1.dp, if (subtask.completed) parsedColor else colors.border, CircleShape)
                                    .clickable { viewModel.toggleSubtask(subtask.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (subtask.completed) {
                                    Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = subtask.title,
                                style = FormaTheme.typography.bodyMedium,
                                color = if (subtask.completed) colors.textTertiary else colors.textPrimary,
                                modifier = Modifier.weight(1f),
                                fontSize = 13.5.sp
                            )

                            IconButton(
                                onClick = { viewModel.removeSubtask(subtask.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Rounded.Close, null, tint = colors.textTertiary, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                } else if (!showAddSubtaskField) {
                    // Prominent Empty-State Callout Button
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(parsedColor.copy(alpha = 0.08f))
                            .border(1.dp, parsedColor.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
                            .formaPressEffect(targetScale = 0.96f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showAddSubtaskField = true
                            }
                            .padding(vertical = 12.dp, horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AddCircleOutline,
                                contentDescription = null,
                                tint = parsedColor,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.creationType == CreationType.HABIT) "+ Add First Micro-Step..." else "+ Add First Step...",
                                style = FormaTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                ),
                                color = parsedColor,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                if (showAddSubtaskField) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newSubtaskText,
                            onValueChange = { newSubtaskText = it },
                            placeholder = {
                                Text(
                                    if (uiState.creationType == CreationType.HABIT) "Enter micro-step (e.g. Fill water bottle)..." else "Enter milestone step...",
                                    style = FormaTheme.typography.bodyMedium.copy(
                                        fontSize = 12.sp,
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    color = colors.textTertiary
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            textStyle = FormaTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = parsedColor,
                                unfocusedBorderColor = colors.border.copy(alpha = 0.6f),
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                cursorColor = parsedColor
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FormaButton(
                            text = "Add",
                            onClick = {
                                if (newSubtaskText.isNotBlank()) {
                                    viewModel.addSubtask(newSubtaskText)
                                    newSubtaskText = ""
                                    showAddSubtaskField = false
                                }
                            },
                            style = FormaButtonStyle.SOFT_PILL
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Shared Notes & Reflections Field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surface)
                .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "NOTES & REFLECTIONS (OPTIONAL)",
                    style = FormaTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent,
                    letterSpacing = 1.2.sp,
                    fontSize = 10.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.setNotes(it) },
                    placeholder = {
                        Text(
                            text = "Add any intentions, reminders, or mindful notes...",
                            style = FormaTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            color = colors.textTertiary
                        )
                    },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = parsedColor,
                        unfocusedBorderColor = colors.border.copy(alpha = 0.6f),
                        focusedContainerColor = colors.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = colors.surfaceVariant.copy(alpha = 0.3f),
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        cursorColor = parsedColor
                    ),
                    textStyle = FormaTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6. Action Save & Cancel Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FormaButton(
                text = "Cancel",
                onClick = onDismiss,
                style = FormaButtonStyle.GHOST,
                modifier = Modifier.weight(1f)
            )

            FormaButton(
                text = if (uiState.isEditMode) "Save Changes" else if (uiState.creationType == CreationType.HABIT) "Create Habit" else "Add Intention",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.save()
                },
                enabled = uiState.title.isNotBlank(),
                style = FormaButtonStyle.PRIMARY,
                modifier = Modifier.weight(2f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Icon & Color Picker Dialog
    if (showIconPicker) {
        IconPickerDialog(
            selectedIcon = uiState.icon,
            selectedColor = uiState.colorTag,
            onIconSelected = { newIcon ->
                viewModel.setIcon(newIcon)
            },
            onColorSelected = { newColor ->
                viewModel.setColorTag(newColor)
            },
            onDismiss = { showIconPicker = false }
        )
    }

    // Date Picker Dialog (Task Schedule Date)
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                LocalDate.parse(uiState.date).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.setDate(localDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    }
                    showDatePickerDialog = false
                }) {
                    Text("Done", color = colors.accent)
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

    // Start Date Picker (Habit Span)
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                LocalDate.parse(uiState.startDate).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.setStartDate(localDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    }
                    showStartDatePicker = false
                }) {
                    Text("Done", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // End Date Picker (Habit Span)
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                uiState.endDate?.let { LocalDate.parse(it).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() } ?: System.currentTimeMillis()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.setEndDate(localDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    }
                    showEndDatePicker = false
                }) {
                    Text("Done", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Start Time Picker
    if (showStartTimePicker) {
        val parsed = try { LocalTime.parse(uiState.startTime) } catch (_: Exception) { LocalTime.of(9, 15) }
        val timePickerState = rememberTimePickerState(initialHour = parsed.hour, initialMinute = parsed.minute, is24Hour = true)
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            title = { Text("Set Start Time", style = FormaTheme.typography.headlineSmall) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimeInput(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val formatted = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                    viewModel.setStartTime(formatted)
                    showStartTimePicker = false
                }) {
                    Text("Done", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // End Time Picker
    if (showEndTimePicker) {
        val parsed = try { LocalTime.parse(uiState.endTime) } catch (_: Exception) { LocalTime.of(9, 45) }
        val timePickerState = rememberTimePickerState(initialHour = parsed.hour, initialMinute = parsed.minute, is24Hour = true)
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            title = { Text("Set End Time", style = FormaTheme.typography.headlineSmall) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimeInput(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val formatted = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                    viewModel.setEndTime(formatted)
                    showEndTimePicker = false
                }) {
                    Text("Done", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Habit Reminder Alert Time Picker
    if (showHabitReminderTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = uiState.reminderHour, initialMinute = uiState.reminderMinute, is24Hour = true)
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showHabitReminderTimePicker = false },
            title = { Text("Set Habit Reminder", style = FormaTheme.typography.headlineSmall) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimeInput(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setReminderTime(timePickerState.hour, timePickerState.minute)
                    showHabitReminderTimePicker = false
                }) {
                    Text("Done", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showHabitReminderTimePicker = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun ColorSwatchBubble(
    hex: String,
    name: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val colors = FormaTheme.colors
    val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { colors.accent }

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(c)
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) colors.textPrimary else c.copy(alpha = 0.4f),
                shape = CircleShape
            )
            .formaPressEffect(targetScale = 0.88f) {
                onSelect()
            },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = name,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun SuggestionChipBubble(
    title: String,
    icon: String,
    tintColor: Color,
    onClick: () -> Unit
) {
    val colors = FormaTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .formaPressEffect(targetScale = 0.94f) {
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FormaIcon(
                iconKey = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = title,
                style = FormaTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary,
                fontSize = 11.sp
            )
        }
    }
}
