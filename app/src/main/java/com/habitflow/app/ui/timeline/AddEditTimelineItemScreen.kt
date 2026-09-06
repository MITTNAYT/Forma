package com.habitflow.app.ui.timeline

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Schedule
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.habitflow.app.core.designsystem.motion.formaPressEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.icon.FormaIcon
import com.habitflow.app.domain.model.TimeOfDay
import com.habitflow.app.ui.timeline.components.IconPickerDialog
import com.habitflow.app.ui.timeline.components.ZenColorPalette
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTimelineItemScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditTimelineViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val colors = NotionTheme.colors
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsState()

    var showIconPicker by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var newSubtaskText by remember { mutableStateOf("") }
    var showAddSubtaskField by remember { mutableStateOf(false) }

    val parsedColor = try {
        Color(android.graphics.Color.parseColor(uiState.colorTag))
    } catch (_: Exception) {
        colors.accent
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is AddEditTimelineEvent.Saved, is AddEditTimelineEvent.Deleted -> onNavigateBack()
            }
        }
    }

    val quickSuggestions = listOf(
        Pair("Hydrate 2L", "water"),
        Pair("Deep Reading", "book"),
        Pair("Mindful Meditation", "zen"),
        Pair("Cardio Run", "run"),
        Pair("Deep Code Flow", "code"),
        Pair("Nature Walk", "walk")
    )

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (uiState.title.isNotBlank()) parsedColor else parsedColor.copy(alpha = 0.4f))
                        .formaPressEffect(
                            targetScale = 0.97f,
                            enabled = uiState.title.isNotBlank()
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.save()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.isEditMode) "Save Changes" else if (uiState.creationType == CreationType.HABIT) "Save Forma Ritual" else "Save Focused Intention",
                        style = NotionTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        },
        containerColor = colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.5f), CircleShape)
                            .formaPressEffect(targetScale = 0.90f) { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = if (uiState.isEditMode) "Edit Commitment" else "New Ritual",
                        style = NotionTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 17.sp
                    )

                    if (uiState.isEditMode) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(colors.surface)
                                .border(1.dp, colors.border.copy(alpha = 0.5f), CircleShape)
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
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(42.dp))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(18.dp)) }

            // 2. Type Selector Pill Switcher (Habit vs Task)
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
                        listOf(
                            Pair(CreationType.HABIT, "Daily Ritual (Repeats)"),
                            Pair(CreationType.TASK, "Single Intention (One-time)")
                        ).forEach { (type, label) ->
                            val isSelected = uiState.creationType == type
                            val bg by animateColorAsState(
                                targetValue = if (isSelected) parsedColor else Color.Transparent,
                                label = "type_bg"
                            )
                            val textColor by animateColorAsState(
                                targetValue = if (isSelected) Color.White else colors.textSecondary,
                                label = "type_text"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(bg)
                                    .formaPressEffect(targetScale = 0.95f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setCreationType(type)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
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

            // 3. Title, Icon & Zen Color Swatches Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Icon selector squircle with selected custom color
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(parsedColor.copy(alpha = 0.16f))
                                        .border(1.5.dp, parsedColor.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
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
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                // Clean Minimal Title Input - Direct inline typing without extract UI
                                TextField(
                                    value = uiState.title,
                                    onValueChange = { viewModel.setTitle(it) },
                                    placeholder = {
                                        Text(
                                            text = if (uiState.creationType == CreationType.HABIT) "Name your daily ritual..." else "What is the intention?",
                                            style = NotionTheme.typography.titleMedium,
                                            color = colors.textTertiary,
                                            fontSize = 16.sp
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Sentences,
                                        autoCorrectEnabled = true,
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    ),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = colors.textPrimary,
                                        unfocusedTextColor = colors.textPrimary,
                                        cursorColor = parsedColor
                                    ),
                                    textStyle = NotionTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Color Swatch Bar
                            Text(
                                text = "COLOR ACCENT",
                                style = NotionTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textTertiary,
                                letterSpacing = 1.sp,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ZenColorPalette.forEach { (hex, name) ->
                                    val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { colors.accent }
                                    val isSelected = hex.equals(uiState.colorTag, ignoreCase = true)

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
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                viewModel.setColorTag(hex)
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
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Suggestion Chips
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickSuggestions.forEach { (name, icon) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(colors.surface)
                                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                    .formaPressEffect(targetScale = 0.94f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setTitle(name)
                                        viewModel.setIcon(icon)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    FormaIcon(
                                        iconKey = icon,
                                        contentDescription = null,
                                        tint = parsedColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = name,
                                        style = NotionTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(18.dp)) }

            // 4. Schedule, Cadence & Dual Time Card (Start & End Time)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Text(
                            text = "TIME OF DAY & RHYTHM",
                            style = NotionTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textTertiary,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Time of Day Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(todBg)
                                        .border(1.dp, todBorder, RoundedCornerShape(14.dp))
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
                                            contentDescription = null,
                                            tint = if (isSelected) parsedColor else colors.textTertiary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = label,
                                            style = NotionTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) parsedColor else colors.textPrimary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Specific Scheduled Start & End Time Pickers
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "START & END TIME",
                                style = NotionTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textTertiary,
                                letterSpacing = 1.2.sp,
                                fontSize = 11.sp
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (uiState.hasTime) parsedColor.copy(alpha = 0.15f) else colors.surfaceVariant)
                                    .formaPressEffect(targetScale = 0.92f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setHasTime(!uiState.hasTime)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (uiState.hasTime) "Scheduled" else "Anytime",
                                    style = NotionTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.hasTime) parsedColor else colors.textTertiary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (uiState.hasTime) {
                            Spacer(modifier = Modifier.height(12.dp))

                            // Dual Start & End Time Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Start Time Box
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(colors.surfaceVariant.copy(alpha = 0.45f))
                                        .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                        .formaPressEffect(targetScale = 0.95f) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            showStartTimePicker = true
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "START",
                                            style = NotionTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textTertiary,
                                            letterSpacing = 1.sp,
                                            fontSize = 9.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.Schedule,
                                                contentDescription = "Start Time",
                                                tint = parsedColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = formatTime12h(uiState.startTime),
                                                style = NotionTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }

                                // Center Duration Indicator
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = "➔",
                                        color = colors.textTertiary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${uiState.durationMinutes}m",
                                        color = parsedColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }

                                // End Time Box
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(colors.surfaceVariant.copy(alpha = 0.45f))
                                        .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                        .formaPressEffect(targetScale = 0.95f) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            showEndTimePicker = true
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "END",
                                            style = NotionTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textTertiary,
                                            letterSpacing = 1.sp,
                                            fontSize = 9.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.Flag,
                                                contentDescription = "End Time",
                                                tint = parsedColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = formatTime12h(uiState.endTime),
                                                style = NotionTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Duration Shortcut Chips (+15m, +30m, +45m, +1h, +1.5h, +2h)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    15 to "+15m",
                                    30 to "+30m",
                                    45 to "+45m",
                                    60 to "+1h",
                                    90 to "+1.5h",
                                    120 to "+2h"
                                ).forEach { (dur, label) ->
                                    val isCurrent = uiState.durationMinutes == dur
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isCurrent) parsedColor else colors.surfaceVariant)
                                            .clickable { viewModel.setDuration(dur) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = NotionTheme.typography.labelSmall,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isCurrent) Color.White else colors.textSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        // For Habits: Repeat Days Selector
                        if (uiState.creationType == CreationType.HABIT) {
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "REPEAT CADENCE",
                                style = NotionTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textTertiary,
                                letterSpacing = 1.2.sp,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                daysOfWeek.forEachIndexed { index, dayLetter ->
                                    val dayInt = index + 1
                                    val isSelected = uiState.repeatDays.contains(dayInt)

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) parsedColor else colors.surfaceVariant)
                                            .clickable { viewModel.toggleRepeatDay(dayInt) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayLetter,
                                            style = NotionTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else colors.textPrimary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(18.dp)) }

            // 5. Date Selector (For Tasks)
            if (uiState.creationType == CreationType.TASK) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                            .clickable { showDatePickerDialog = true }
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TARGET DATE",
                                    style = NotionTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textTertiary,
                                    letterSpacing = 1.2.sp,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = uiState.date,
                                    style = NotionTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 16.sp
                                )
                            }
                            Text(
                                text = "Change",
                                style = NotionTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = parsedColor,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(18.dp)) }
            }

            // 6. Subtasks / Step Checklist
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
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
                                text = "STEP CHECKLIST",
                                style = NotionTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textTertiary,
                                letterSpacing = 1.2.sp,
                                fontSize = 11.sp
                            )
                            IconButton(
                                onClick = { showAddSubtaskField = !showAddSubtaskField },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (showAddSubtaskField) Icons.Rounded.Close else Icons.Rounded.Add,
                                    contentDescription = "Add Step",
                                    tint = parsedColor,
                                    modifier = Modifier.size(18.dp)
                                )
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
                                    placeholder = { Text("Add micro-step...", fontSize = 13.sp, color = colors.textTertiary) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Sentences,
                                        autoCorrectEnabled = true,
                                        imeAction = ImeAction.Done
                                    ),
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = parsedColor,
                                        unfocusedBorderColor = colors.border
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(
                                    onClick = {
                                        viewModel.addSubtask(newSubtaskText)
                                        newSubtaskText = ""
                                        showAddSubtaskField = false
                                    },
                                    enabled = newSubtaskText.isNotBlank()
                                ) {
                                    Text("Add", color = parsedColor, fontWeight = FontWeight.Bold)
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
                                            .background(if (subtask.completed) parsedColor else Color.Transparent)
                                            .border(1.5.dp, if (subtask.completed) parsedColor else colors.border, CircleShape)
                                            .clickable { viewModel.toggleSubtask(subtask.id) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (subtask.completed) {
                                            Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = subtask.title,
                                        style = NotionTheme.typography.bodyMedium,
                                        color = if (subtask.completed) colors.textTertiary else colors.textPrimary,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeSubtask(subtask.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Delete", tint = colors.textTertiary, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(18.dp)) }

            // 7. Notes & Reflection Card - Direct inline typing without extract UI
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
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
                                text = "NOTES & REFLECTION",
                                style = NotionTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textTertiary,
                                letterSpacing = 1.2.sp,
                                fontSize = 11.sp
                            )
                            if (uiState.notes.isNotBlank()) {
                                Text(
                                    text = "${uiState.notes.length} chars",
                                    style = NotionTheme.typography.labelSmall,
                                    color = colors.textTertiary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TextField(
                            value = uiState.notes,
                            onValueChange = { viewModel.setNotes(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp),
                            placeholder = {
                                Text(
                                    text = "Add context, thoughts, or reflections on why this matters...",
                                    style = NotionTheme.typography.bodyMedium,
                                    color = colors.textTertiary,
                                    fontSize = 14.sp,
                                    lineHeight = 21.sp
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                autoCorrectEnabled = true,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Default
                            ),
                            textStyle = NotionTheme.typography.bodyMedium.copy(
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                lineHeight = 21.sp
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                cursorColor = parsedColor
                            )
                        )
                    }
                }
            }
        }
    }

    if (showIconPicker) {
        IconPickerDialog(
            selectedIcon = uiState.icon,
            selectedColor = uiState.colorTag,
            onIconSelected = {
                viewModel.setIcon(it)
                showIconPicker = false
            },
            onColorSelected = {
                viewModel.setColorTag(it)
            },
            onDismiss = { showIconPicker = false }
        )
    }

    if (showStartTimePicker) {
        val (curH, curM) = parseHourMinute(uiState.startTime)
        val timePickerState = rememberTimePickerState(
            initialHour = curH,
            initialMinute = curM,
            is24Hour = false
        )

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            title = {
                Text(
                    text = "Select Start Time",
                    style = NotionTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val newTime = String.format(Locale.US, "%02d:%02d", timePickerState.hour, timePickerState.minute)
                    viewModel.setStartTime(newTime)
                    showStartTimePicker = false
                }) {
                    Text("Confirm", color = parsedColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            text = {
                TimeInput(state = timePickerState)
            },
            containerColor = colors.surface,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textPrimary
        )
    }

    if (showEndTimePicker) {
        val (curH, curM) = parseHourMinute(uiState.endTime)
        val timePickerState = rememberTimePickerState(
            initialHour = curH,
            initialMinute = curM,
            is24Hour = false
        )

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            title = {
                Text(
                    text = "Select End Time",
                    style = NotionTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val newTime = String.format(Locale.US, "%02d:%02d", timePickerState.hour, timePickerState.minute)
                    viewModel.setEndTime(newTime)
                    showEndTimePicker = false
                }) {
                    Text("Confirm", color = parsedColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            text = {
                TimeInput(state = timePickerState)
            },
            containerColor = colors.surface,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textPrimary
        )
    }

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = LocalDate.parse(uiState.date).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.setDate(picked.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    }
                    showDatePickerDialog = false
                }) {
                    Text("Confirm", color = parsedColor, fontWeight = FontWeight.Bold)
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
}

private fun formatTime12h(time24: String): String {
    return try {
        val parts = time24.split(":")
        val h = parts[0].toInt()
        val m = parts[1].toInt()
        val ampm = if (h >= 12) "PM" else "AM"
        val h12 = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        String.format(Locale.US, "%d:%02d %s", h12, m, ampm)
    } catch (_: Exception) {
        time24
    }
}

private fun parseHourMinute(time24: String): Pair<Int, Int> {
    return try {
        val parts = time24.split(":")
        Pair(parts[0].toInt(), parts[1].toInt())
    } catch (_: Exception) {
        Pair(9, 0)
    }
}
