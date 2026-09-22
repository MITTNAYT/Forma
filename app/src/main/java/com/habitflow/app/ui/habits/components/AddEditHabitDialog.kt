package com.habitflow.app.ui.habits.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.habitflow.app.core.designsystem.FormaTheme
import com.habitflow.app.core.designsystem.component.FormaButton
import com.habitflow.app.core.designsystem.component.FormaButtonStyle
import com.habitflow.app.core.designsystem.component.FormaDivider
import com.habitflow.app.core.util.Constants
import com.habitflow.app.domain.model.EnergyLevel
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.TimeOfDay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditHabitDialog(
    initialHabit: Habit?,
    onDismiss: () -> Unit,
    onSave: (Habit) -> Unit,
    onDelete: ((Habit) -> Unit)? = null
) {
    val colors = FormaTheme.colors
    val isEdit = initialHabit != null

    var name by remember { mutableStateOf(initialHabit?.name ?: "") }
    var icon by remember { mutableStateOf(initialHabit?.icon ?: "target") }
    var colorTag by remember { mutableStateOf(initialHabit?.colorTag ?: "#4E6542") }
    var startDate by remember { mutableStateOf(initialHabit?.startDate ?: com.habitflow.app.core.util.DateUtils.formatDateIso(com.habitflow.app.core.util.DateUtils.today())) }
    var endDate by remember { mutableStateOf(initialHabit?.endDate) }
    var isIndefinite by remember { mutableStateOf(initialHabit?.isIndefinite ?: (initialHabit?.endDate == null)) }
    var timeOfDay by remember { mutableStateOf(initialHabit?.timeOfDay ?: TimeOfDay.MORNING) }
    var energyLevel by remember { mutableStateOf(initialHabit?.energyLevel ?: EnergyLevel.HIGH) }
    var repeatDays by remember { mutableStateOf(initialHabit?.repeatDays ?: setOf(1, 2, 3, 4, 5, 6, 7)) }
    var hasReminder by remember { mutableStateOf(initialHabit?.reminderTimeMinutes != null) }
    var reminderHour by remember { mutableIntStateOf((initialHabit?.reminderTimeMinutes ?: 480) / 60) }
    var reminderMinute by remember { mutableIntStateOf((initialHabit?.reminderTimeMinutes ?: 480) % 60) }
    var stackedCueText by remember { mutableStateOf(initialHabit?.stackedCueText ?: "") }
    var isWintering by remember { mutableStateOf(initialHabit?.isWintering ?: false) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEdit) "Edit Habit" else "New Habit",
                    style = FormaTheme.typography.headlineMedium,
                    color = colors.textPrimary
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary
                    )
                }
            }

            val parsedColor = try {
                Color(android.graphics.Color.parseColor(colorTag))
            } catch (_: Exception) {
                colors.accent
            }

            // Habit Name Input with Trailing Clear Action
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Habit Name", style = FormaTheme.typography.bodySmall) },
                placeholder = { Text("e.g. Read 20 pages, Morning Run", color = colors.textTertiary) },
                singleLine = true,
                shape = FormaTheme.shapes.small,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
                    autoCorrectEnabled = true,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                ),
                trailingIcon = {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = name.isNotBlank(),
                        enter = androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.fadeOut()
                    ) {
                        IconButton(onClick = { name = "" }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear",
                                tint = colors.textTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = parsedColor,
                    unfocusedBorderColor = colors.border,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    cursorColor = parsedColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (name.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (name.length < 30) "Mindful & concise" else "Focused ritual",
                        style = FormaTheme.typography.labelSmall,
                        color = parsedColor.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${name.length} chars",
                        style = FormaTheme.typography.labelSmall,
                        color = colors.textTertiary,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Icon Picker
            Text(
                text = "ICON",
                style = FormaTheme.typography.labelSmall,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Constants.DEFAULT_HABIT_ICONS.forEach { iconKey ->
                    val isSelected = icon == iconKey
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(FormaTheme.shapes.extraSmall)
                            .background(if (isSelected) colors.textPrimary else colors.surfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) colors.textPrimary else Color.Transparent,
                                FormaTheme.shapes.extraSmall
                            )
                            .clickable { icon = iconKey },
                        contentAlignment = Alignment.Center
                    ) {
                        com.habitflow.app.core.designsystem.icon.HabitFlowIcon(
                            iconKey = iconKey,
                            contentDescription = iconKey,
                            tint = if (isSelected) com.habitflow.app.core.designsystem.ObsidianBlackBg else colors.textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color Accent Selection
            Text(
                text = "COLOR ACCENT",
                style = FormaTheme.typography.labelSmall,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.habitflow.app.ui.timeline.components.ZenColorPalette.forEach { (hex, colorName) ->
                    val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { colors.accent }
                    val isSelected = hex.equals(colorTag, ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) colors.textPrimary else c.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                            .clickable { colorTag = hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = colorName,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lifecycle Horizon (Start Date & Target End Date / Infinity)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FormaTheme.shapes.small)
                    .background(colors.surfaceVariant.copy(alpha = 0.45f))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RITUAL DURATION & HORIZON",
                        style = FormaTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textTertiary,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isIndefinite) colors.accent.copy(alpha = 0.15f) else colors.surfaceVariant)
                            .clickable { isIndefinite = !isIndefinite }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "∞",
                            fontWeight = FontWeight.Bold,
                            color = if (isIndefinite) colors.accent else colors.textTertiary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isIndefinite) "Ongoing / Forever" else "Set Target End",
                            style = FormaTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isIndefinite) colors.accent else colors.textTertiary,
                            fontSize = 10.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Start Date Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .clickable { showStartDatePicker = true }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Text(
                                text = "START DATE",
                                style = FormaTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textTertiary,
                                fontSize = 9.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = startDate,
                                style = FormaTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 12.5.sp
                            )
                        }
                    }

                    // Target End Date Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isIndefinite) colors.surface.copy(alpha = 0.4f) else colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .clickable(enabled = !isIndefinite) { showEndDatePicker = true }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Text(
                                text = "TARGET FINAL DATE",
                                style = FormaTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isIndefinite) colors.textTertiary.copy(alpha = 0.5f) else colors.textTertiary,
                                fontSize = 9.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isIndefinite) "No End (Forever ∞)" else (endDate ?: "Tap to set"),
                                style = FormaTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isIndefinite) colors.textTertiary else colors.textPrimary,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time of Day
            Text(
                text = "TIME OF DAY",
                style = FormaTheme.typography.labelSmall,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TimeOfDay.entries.forEach { tod ->
                    val isSelected = timeOfDay == tod
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(FormaTheme.shapes.extraSmall)
                            .background(if (isSelected) colors.textPrimary else colors.surfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent else colors.border,
                                FormaTheme.shapes.extraSmall
                            )
                            .clickable { timeOfDay = tod }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tod.displayName,
                            style = FormaTheme.typography.labelSmall,
                            color = if (isSelected) colors.surface else colors.textPrimary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Energy Level
            Text(
                text = "ENERGY LEVEL REQUIRED",
                style = FormaTheme.typography.labelSmall,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EnergyLevel.entries.forEach { level ->
                    val isSelected = energyLevel == level
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(FormaTheme.shapes.extraSmall)
                            .background(if (isSelected) colors.textPrimary else colors.surfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent else colors.border,
                                FormaTheme.shapes.extraSmall
                            )
                            .clickable { energyLevel = level }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = level.displayName,
                            style = FormaTheme.typography.labelSmall,
                            color = if (isSelected) colors.surface else colors.textPrimary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Repeat Days
            Text(
                text = "REPEAT DAYS",
                style = FormaTheme.typography.labelSmall,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 1..7) {
                    val isSelected = repeatDays.contains(i)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(FormaTheme.shapes.extraSmall)
                            .background(if (isSelected) colors.accent else colors.surfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) colors.accent else colors.border,
                                FormaTheme.shapes.extraSmall
                            )
                            .clickable {
                                repeatDays = if (isSelected) {
                                    if (repeatDays.size > 1) repeatDays - i else repeatDays
                                } else {
                                    repeatDays + i
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayNames[i - 1],
                            style = FormaTheme.typography.titleMedium,
                            color = if (isSelected) Color.White else colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notification Reminder Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Reminder",
                        style = FormaTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = if (hasReminder) String.format("%02d:%02d", reminderHour, reminderMinute) else "Off",
                        style = FormaTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }

                Switch(
                    checked = hasReminder,
                    onCheckedChange = { hasReminder = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = colors.accent,
                        uncheckedThumbColor = colors.textSecondary,
                        uncheckedTrackColor = colors.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Habit Stacking (Cue Chain)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "HABIT STACKING (CUE CHAIN)",
                    style = FormaTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = stackedCueText,
                    onValueChange = { stackedCueText = it },
                    label = { Text("Precursor Cue / Habit Trigger", style = FormaTheme.typography.bodySmall) },
                    placeholder = { Text("e.g. After I brew morning tea...", color = colors.textTertiary) },
                    singleLine = true,
                    shape = FormaTheme.shapes.small,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
                        autoCorrectEnabled = true,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.textPrimary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        cursorColor = parsedColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Anchor this ritual directly after an existing daily rhythm for effortless habit stacking.",
                    style = FormaTheme.typography.bodySmall,
                    color = colors.textTertiary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wintering / Seasonal Rest Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FormaTheme.shapes.small)
                    .background(colors.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🍵 Seasonal Wintering Mode",
                        style = FormaTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Pause this ritual during travel, illness, or resting periods without penalty to your Consistency Index.",
                        style = FormaTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = isWintering,
                    onCheckedChange = { isWintering = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = colors.accent,
                        uncheckedThumbColor = colors.textSecondary,
                        uncheckedTrackColor = colors.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            FormaDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (initialHabit != null && onDelete != null) {
                    IconButton(
                        onClick = { onDelete(initialHabit) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(FormaTheme.shapes.small)
                            .background(colors.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete Habit",
                            tint = colors.accent
                        )
                    }
                }

                FormaButton(
                    text = if (isEdit) "Save Changes" else "Create Habit",
                    onClick = {
                        if (name.isNotBlank()) {
                            val reminderMinutes = if (hasReminder) reminderHour * 60 + reminderMinute else null
                            val habit = (initialHabit ?: Habit(name = name)).copy(
                                name = name.trim(),
                                icon = icon,
                                colorTag = colorTag,
                                startDate = startDate,
                                endDate = if (isIndefinite) null else endDate,
                                isIndefinite = isIndefinite,
                                timeOfDay = timeOfDay,
                                energyLevel = energyLevel,
                                repeatDays = repeatDays,
                                reminderTimeMinutes = reminderMinutes,
                                stackedCueText = stackedCueText.trim().takeIf { it.isNotBlank() },
                                isWintering = isWintering,
                                updatedAt = System.currentTimeMillis()
                            )
                            onSave(habit)
                        }
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showStartDatePicker) {
        val startDatePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = try {
                java.time.LocalDate.parse(startDate).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        )

        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let { millis ->
                        val picked = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        startDate = picked.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showStartDatePicker = false
                }) {
                    Text("Confirm", color = colors.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        ) {
            androidx.compose.material3.DatePicker(state = startDatePickerState)
        }
    }

    if (showEndDatePicker) {
        val currentEnd = endDate ?: startDate
        val endDatePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = try {
                java.time.LocalDate.parse(currentEnd).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        )

        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    endDatePickerState.selectedDateMillis?.let { millis ->
                        val picked = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        endDate = picked.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                        isIndefinite = false
                    }
                    showEndDatePicker = false
                }) {
                    Text("Confirm", color = colors.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        ) {
            androidx.compose.material3.DatePicker(state = endDatePickerState)
        }
    }
}

