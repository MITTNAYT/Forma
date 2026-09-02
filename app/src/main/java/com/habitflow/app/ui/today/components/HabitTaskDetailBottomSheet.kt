package com.habitflow.app.ui.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.icon.HabitFlowIcon
import com.habitflow.app.domain.model.Subtask
import com.habitflow.app.domain.model.TodayScheduleItem
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTaskDetailBottomSheet(
    item: TodayScheduleItem,
    onDismiss: () -> Unit,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onStartFocus: () -> Unit,
    onToggleSubtask: (subtaskId: String) -> Unit = {}
) {
    val colors = NotionTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val (title, iconKey, subtitle, isDone, repeatLabel, subtasks, notes, streakCount) = when (item) {
        is TodayScheduleItem.HabitItem -> {
            val habit = item.habit
            val defaultTime = when (habit.timeOfDay) {
                com.habitflow.app.domain.model.TimeOfDay.MORNING -> "Morning • 7:00 AM"
                com.habitflow.app.domain.model.TimeOfDay.AFTERNOON -> "Afternoon • 1:00 PM"
                com.habitflow.app.domain.model.TimeOfDay.EVENING -> "Evening • 6:00 PM"
                com.habitflow.app.domain.model.TimeOfDay.ANYTIME -> "Anytime today"
            }
            val timeFormatted = habit.reminderTimeMinutes?.let { minutes ->
                val h = minutes / 60
                val m = minutes % 60
                val amPm = if (h < 12) "AM" else "PM"
                val h12 = if (h % 12 == 0) 12 else h % 12
                String.format("%d:%02d %s", h12, m, amPm)
            } ?: defaultTime

            val daysMap = mapOf(1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun")
            val repeatStr = if (habit.repeatDays.size == 7) {
                "Every Day (7x / week)"
            } else {
                "${habit.repeatDays.size}x / week (${habit.repeatDays.sorted().mapNotNull { daysMap[it] }.joinToString(", ")})"
            }

            HabitTaskDetails(
                title = habit.name,
                iconKey = habit.icon,
                subtitle = timeFormatted,
                isDone = item.isDoneToday,
                repeatLabel = repeatStr,
                subtasks = emptyList(),
                notes = "",
                streakCount = item.currentStreak
            )
        }
        is TodayScheduleItem.TimelineBlock -> {
            val task = item.item
            val timing = formatDetailTiming(task.startTime, task.endTime)

            val daysMap = mapOf(1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun")
            val repeatStr = if (!task.isRecurring) {
                "Single Intention"
            } else if (task.repeatDays.size == 7) {
                "Every Day (7x / week)"
            } else {
                "${task.repeatDays.size}x / week (${task.repeatDays.sorted().mapNotNull { daysMap[it] }.joinToString(", ")})"
            }

            HabitTaskDetails(
                title = task.title,
                iconKey = task.icon,
                subtitle = timing,
                isDone = task.completed,
                repeatLabel = repeatStr,
                subtasks = task.subtasks,
                notes = task.notes,
                streakCount = 0
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(38.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(colors.border)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // 1. Header with Icon & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        HabitFlowIcon(
                            iconKey = iconKey,
                            contentDescription = title,
                            tint = colors.accent,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = if (item is TodayScheduleItem.HabitItem) "HABIT RITUAL" else "COMMITTED TASK",
                            style = NotionTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            letterSpacing = 1.2.sp,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = title,
                            style = NotionTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 20.sp
                        )
                    }
                }

                // Checkbox status toggle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDone) colors.accent else colors.surfaceVariant)
                        .clickable { onToggleComplete() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Done",
                            tint = colors.onAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Telemetry Details Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Scheduled Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Scheduled Time",
                            style = NotionTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = colors.textTertiary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = subtitle,
                            style = NotionTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 14.sp
                        )
                    }
                }

                // Frequency / Repeat
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Repeat,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Frequency",
                            style = NotionTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = colors.textTertiary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = repeatLabel,
                            style = NotionTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 14.sp
                        )
                    }
                }

                // Streak (If Habit)
                if (item is TodayScheduleItem.HabitItem) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Current Streak",
                                style = NotionTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = colors.textTertiary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "$streakCount Days in Flow",
                                style = NotionTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // 3. Subtasks (If Any)
            if (subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                val doneSubtasks = subtasks.count { it.completed }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SUBTASKS",
                        style = NotionTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textTertiary,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "$doneSubtasks of ${subtasks.size} done",
                        style = NotionTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    subtasks.forEach { subtask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceVariant.copy(alpha = 0.4f))
                                .clickable { onToggleSubtask(subtask.id) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (subtask.completed) colors.accent else Color.Transparent)
                                    .border(1.dp, if (subtask.completed) colors.accent else colors.border, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (subtask.completed) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = colors.onAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = subtask.title,
                                style = NotionTheme.typography.bodyMedium,
                                color = if (subtask.completed) colors.textTertiary else colors.textPrimary,
                                textDecoration = if (subtask.completed) TextDecoration.LineThrough else TextDecoration.None,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // 4. Notes (If Any)
            if (notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "NOTES & INTENTIONS",
                    style = NotionTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.textTertiary,
                    letterSpacing = 1.2.sp,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.35f))
                        .padding(14.dp)
                ) {
                    Text(
                        text = notes,
                        style = NotionTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // 5. Action Buttons (Pomodoro Focus & Edit)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Edit Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surfaceVariant)
                        .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .clickable {
                            onDismiss()
                            onEdit()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Edit",
                            style = NotionTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 14.sp
                        )
                    }
                }

                // Start Pomodoro Focus
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.accent)
                        .clickable {
                            onDismiss()
                            onStartFocus()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = "Start Focus",
                            tint = colors.onAccent,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Start Focus",
                            style = NotionTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onAccent,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

private data class HabitTaskDetails(
    val title: String,
    val iconKey: String,
    val subtitle: String,
    val isDone: Boolean,
    val repeatLabel: String,
    val subtasks: List<Subtask>,
    val notes: String,
    val streakCount: Int
)

private fun formatDetailTiming(startTimeStr: String?, endTimeStr: String?): String {
    if (startTimeStr == null) return "Anytime today"

    val formatter12h = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    val startParsed = try { LocalTime.parse(startTimeStr) } catch (_: Exception) { null }
    val endParsed = try { endTimeStr?.let { LocalTime.parse(it) } } catch (_: Exception) { null }

    val formattedStart = startParsed?.format(formatter12h) ?: startTimeStr

    if (endParsed != null && startParsed != null) {
        val formattedEnd = endParsed.format(formatter12h)
        val minutes = ChronoUnit.MINUTES.between(startParsed, endParsed)
        return "$formattedStart - $formattedEnd ($minutes min)"
    }

    return formattedStart
}
