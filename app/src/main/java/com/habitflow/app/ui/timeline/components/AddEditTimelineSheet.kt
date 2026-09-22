package com.habitflow.app.ui.timeline.components

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
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
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.component.FormaBottomSheet
import com.habitflow.app.core.designsystem.icon.FormaIcon
import com.habitflow.app.core.designsystem.motion.FormaMotion
import com.habitflow.app.core.designsystem.motion.formaPressEffect
import com.habitflow.app.domain.model.TimeOfDay
import com.habitflow.app.ui.timeline.AddEditTimelineEvent
import com.habitflow.app.ui.timeline.AddEditTimelineViewModel
import com.habitflow.app.ui.timeline.CreationType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Contextual Floating Bottom Sheet for creating and editing Rituals / Intentions.
 * Replaces disruptive full-page typing navigation with in-place spring presentation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTimelineSheet(
    onDismiss: () -> Unit,
    viewModel: AddEditTimelineViewModel = hiltViewModel()
) {
    val colors = NotionTheme.colors
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsState()

    var showIconPicker by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
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
                is AddEditTimelineEvent.Saved, is AddEditTimelineEvent.Deleted -> onDismiss()
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

    FormaBottomSheet(
        onDismissRequest = onDismiss,
        title = if (uiState.isEditMode) "Edit Commitment" else "New Ritual",
        subtitle = if (uiState.creationType == CreationType.HABIT) "Recurring daily practice" else "Single focused intention",
        trailingAction = {
            if (uiState.isEditMode) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE))
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
        // 1. Type Selector Pill Switcher (Habit vs Task)
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
                    Pair(CreationType.HABIT, "Daily Ritual (Repeats)"),
                    Pair(CreationType.TASK, "Single Intention (One-time)")
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
                            style = NotionTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Title, Icon & Color Swatches Card
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
                            .size(50.dp)
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

                    // Clean Minimal Title Input
                    TextField(
                        value = uiState.title,
                        onValueChange = { viewModel.setTitle(it) },
                        placeholder = {
                            Text(
                                text = if (uiState.creationType == CreationType.HABIT) "Name your daily ritual..." else "What is the intention?",
                                style = NotionTheme.typography.titleMedium,
                                color = colors.textTertiary,
                                fontSize = 15.sp
                            )
                        },
                        trailingIcon = {
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
                            fontSize = 16.sp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { isTitleFocused = it.isFocused }
                    )
                }

                // Quick Character Guide
                if (uiState.title.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, start = 62.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.title.length < 30) "Mindful & concise" else "Focused intention",
                            style = NotionTheme.typography.labelSmall,
                            color = parsedColor.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${uiState.title.length} chars",
                            style = NotionTheme.typography.labelSmall,
                            color = colors.textTertiary,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Color Swatch Bar
                Text(
                    text = "COLOR ACCENT",
                    style = NotionTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.textTertiary,
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

        // Quick Suggestions Horizontal Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickSuggestions.forEach { (name, icon) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .formaPressEffect(targetScale = 0.94f) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.setTitle(name)
                            viewModel.setIcon(icon)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FormaIcon(
                            iconKey = icon,
                            contentDescription = null,
                            tint = parsedColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
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

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Cadence & Rhythm Section
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
                    text = "TIME OF DAY & RHYTHM",
                    style = NotionTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.textTertiary,
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
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) parsedColor else colors.textTertiary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = label,
                                    style = NotionTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) parsedColor else colors.textPrimary,
                                    fontSize = 9.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Notes & Intention Area
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
                    text = "NOTES & INTENTION",
                    style = NotionTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.textTertiary,
                    letterSpacing = 1.2.sp,
                    fontSize = 10.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.setNotes(it) },
                    placeholder = {
                        Text(
                            text = "Add reflections, cue triggers, or details...",
                            color = colors.textTertiary,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = parsedColor,
                        unfocusedBorderColor = colors.border.copy(alpha = 0.6f),
                        focusedContainerColor = colors.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = colors.surfaceVariant.copy(alpha = 0.25f),
                        cursorColor = parsedColor
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Submit Action Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(20.dp))
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
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showIconPicker) {
        IconPickerDialog(
            selectedIcon = uiState.icon,
            selectedColor = uiState.colorTag,
            onIconSelected = { selected ->
                viewModel.setIcon(selected)
                showIconPicker = false
            },
            onColorSelected = { selectedColor ->
                viewModel.setColorTag(selectedColor)
            },
            onDismiss = { showIconPicker = false }
        )
    }
}
