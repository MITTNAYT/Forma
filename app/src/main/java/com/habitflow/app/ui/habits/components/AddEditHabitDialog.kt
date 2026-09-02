package com.habitflow.app.ui.habits.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.component.NotionButton
import com.habitflow.app.core.designsystem.component.NotionButtonStyle
import com.habitflow.app.core.designsystem.component.NotionDivider
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
    val colors = NotionTheme.colors
    val isEdit = initialHabit != null

    var name by remember { mutableStateOf(initialHabit?.name ?: "") }
    var icon by remember { mutableStateOf(initialHabit?.icon ?: "⚡") }
    var timeOfDay by remember { mutableStateOf(initialHabit?.timeOfDay ?: TimeOfDay.MORNING) }
    var energyLevel by remember { mutableStateOf(initialHabit?.energyLevel ?: EnergyLevel.HIGH) }
    var repeatDays by remember { mutableStateOf(initialHabit?.repeatDays ?: setOf(1, 2, 3, 4, 5, 6, 7)) }
    var hasReminder by remember { mutableStateOf(initialHabit?.reminderTimeMinutes != null) }
    var reminderHour by remember { mutableIntStateOf((initialHabit?.reminderTimeMinutes ?: 480) / 60) }
    var reminderMinute by remember { mutableIntStateOf((initialHabit?.reminderTimeMinutes ?: 480) % 60) }

    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                    style = NotionTheme.typography.headlineMedium,
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

            Spacer(modifier = Modifier.height(16.dp))

            // Habit Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Habit Name", style = NotionTheme.typography.bodySmall) },
                placeholder = { Text("e.g. Read 20 pages, Morning Run", color = colors.textTertiary) },
                singleLine = true,
                shape = NotionTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.textPrimary,
                    unfocusedBorderColor = colors.border,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Icon Picker
            Text(
                text = "ICON",
                style = NotionTheme.typography.labelSmall,
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
                            .clip(NotionTheme.shapes.extraSmall)
                            .background(if (isSelected) colors.textPrimary else colors.surfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) colors.textPrimary else Color.Transparent,
                                NotionTheme.shapes.extraSmall
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

            // Time of Day
            Text(
                text = "TIME OF DAY",
                style = NotionTheme.typography.labelSmall,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TimeOfDay.values().forEach { tod ->
                    val isSelected = timeOfDay == tod
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(NotionTheme.shapes.extraSmall)
                            .background(if (isSelected) colors.textPrimary else colors.surfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent else colors.border,
                                NotionTheme.shapes.extraSmall
                            )
                            .clickable { timeOfDay = tod }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tod.displayName,
                            style = NotionTheme.typography.labelSmall,
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
                style = NotionTheme.typography.labelSmall,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EnergyLevel.values().forEach { level ->
                    val isSelected = energyLevel == level
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(NotionTheme.shapes.extraSmall)
                            .background(if (isSelected) colors.textPrimary else colors.surfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent else colors.border,
                                NotionTheme.shapes.extraSmall
                            )
                            .clickable { energyLevel = level }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = level.displayName,
                            style = NotionTheme.typography.labelSmall,
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
                style = NotionTheme.typography.labelSmall,
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
                            .clip(NotionTheme.shapes.extraSmall)
                            .background(if (isSelected) colors.accent else colors.surfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) colors.accent else colors.border,
                                NotionTheme.shapes.extraSmall
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
                            style = NotionTheme.typography.titleMedium,
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
                        style = NotionTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = if (hasReminder) String.format("%02d:%02d", reminderHour, reminderMinute) else "Off",
                        style = NotionTheme.typography.bodySmall,
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

            Spacer(modifier = Modifier.height(24.dp))
            NotionDivider()
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
                            .clip(NotionTheme.shapes.small)
                            .background(colors.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete Habit",
                            tint = colors.accent
                        )
                    }
                }

                NotionButton(
                    text = if (isEdit) "Save Changes" else "Create Habit",
                    onClick = {
                        if (name.isNotBlank()) {
                            val reminderMinutes = if (hasReminder) reminderHour * 60 + reminderMinute else null
                            val habit = (initialHabit ?: Habit(name = name)).copy(
                                name = name.trim(),
                                icon = icon,
                                timeOfDay = timeOfDay,
                                energyLevel = energyLevel,
                                repeatDays = repeatDays,
                                reminderTimeMinutes = reminderMinutes,
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
}
