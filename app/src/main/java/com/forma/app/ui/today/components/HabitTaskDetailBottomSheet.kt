package com.forma.app.ui.today.components

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
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.icon.FormaIcon
import com.forma.app.core.designsystem.motion.formaPressEffect
import com.forma.app.core.share.MilestoneCardExporter
import com.forma.app.domain.model.Subtask
import com.forma.app.domain.model.TodayScheduleItem
import kotlinx.coroutines.launch
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
    onToggleSubtask: (subtaskId: String) -> Unit = {},
    onTogglePause: () -> Unit = {},
    onSkipHabit: () -> Unit = {}
) {
    val colors = FormaTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val details = when (item) {
        is TodayScheduleItem.HabitItem -> {
            val habit = item.habit
            val defaultTime = when (habit.timeOfDay) {
                com.forma.app.domain.model.TimeOfDay.MORNING -> "Morning • 7:00 AM"
                com.forma.app.domain.model.TimeOfDay.AFTERNOON -> "Afternoon • 1:00 PM"
                com.forma.app.domain.model.TimeOfDay.EVENING -> "Evening • 6:00 PM"
                com.forma.app.domain.model.TimeOfDay.ANYTIME -> "Anytime today"
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
                subtasks = habit.subtasks,
                notes = habit.stackedCueText ?: "",
                streakCount = item.currentStreak,
                isWintering = habit.isWintering,
                canPause = true,
                isHabit = true,
                isSkipped = item.isSkippedToday
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
                streakCount = 0,
                isWintering = false,
                canPause = false,
                isHabit = false,
                isSkipped = false
            )
        }
    }

    val title = details.title
    val iconKey = details.iconKey
    val subtitle = details.subtitle
    val repeatLabel = details.repeatLabel
    val notes = details.notes
    val streakCount = details.streakCount

    // ── Zero Latency Optimistic In-Memory State ──────────────────────────
    var localIsDone by remember(details.isDone) { mutableStateOf(details.isDone) }
    var localSubtasks by remember(details.subtasks) { mutableStateOf(details.subtasks) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
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
                .padding(horizontal = 24.dp, vertical = 6.dp)
        ) {
            // ── 1. Top Header Row (Icon, Title, Instant Completion Halo) ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (localIsDone) colors.accentSoft else colors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        FormaIcon(
                            iconKey = iconKey,
                            contentDescription = title,
                            tint = if (localIsDone) colors.accent else colors.textPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = FormaTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (localIsDone) colors.textSecondary else colors.textPrimary,
                            textDecoration = if (localIsDone) TextDecoration.LineThrough else TextDecoration.None,
                            fontSize = 18.sp,
                            lineHeight = 23.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (details.isWintering) "Rest Sanctuary • Streak Preserved"
                            else if (details.isSkipped) "Skipped for Today • Rest Freely"
                            else subtitle,
                            style = FormaTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Checkbox status toggle with subtle halo (Instant frame 0 feedback)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (localIsDone) colors.accent else if (details.isWintering) colors.accentSoft else colors.surfaceVariant)
                        .border(1.5.dp, if (localIsDone || details.isWintering) colors.accent else colors.border.copy(alpha = 0.6f), CircleShape)
                        .formaPressEffect(targetScale = 0.88f) {
                            localIsDone = !localIsDone
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleComplete()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (localIsDone) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Completed",
                            tint = colors.onAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (details.isWintering) {
                        Icon(
                            imageVector = Icons.Rounded.AcUnit,
                            contentDescription = "Wintering - Streak Preserved",
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Tap to Complete",
                            tint = colors.textTertiary.copy(alpha = 0.35f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── 2. Full-Width Schedule Card ──────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surfaceVariant.copy(alpha = 0.45f))
                    .border(1.dp, colors.border.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SCHEDULE & CADENCE",
                            style = FormaTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textTertiary,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = FormaTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 13.5.sp
                        )
                        Text(
                            text = repeatLabel,
                            style = FormaTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }

            // ── 3. Dedicated Momentum & Share Poster Card (If Habit) ─────
            if (item is TodayScheduleItem.HabitItem) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.45f))
                        .border(1.dp, colors.border.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(colors.accentSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "MOMENTUM & STREAK",
                                    style = FormaTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textTertiary,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$streakCount Days Streak",
                                    style = FormaTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (streakCount > 0) "Active mindful flow • Continuous progress"
                                    else "Start day 1 today • Every milestone begins here",
                                    style = FormaTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Generous Apple Pill Share Action Button (Zero text wrapping!)
                        Box(
                            modifier = Modifier
                                .height(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.accentSoft)
                                .border(1.dp, colors.accent.copy(alpha = 0.30f), RoundedCornerShape(10.dp))
                                .formaPressEffect(targetScale = 0.92f) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    scope.launch {
                                        val result = MilestoneCardExporter.generateAndShareMilestone(
                                            context = context,
                                            habitTitle = title,
                                            streakDays = streakCount.coerceAtLeast(1)
                                        )
                                        result.onSuccess { intent -> context.startActivity(intent) }
                                    }
                                }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = "Share Milestone",
                                    tint = colors.accent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Share Card",
                                    style = FormaTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── 4. Wintering Mode Sanctuary Rest ─────────────────────────
            if (details.canPause) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (details.isWintering) colors.accentSoft.copy(alpha = 0.65f) else colors.surface)
                        .border(
                            width = 1.dp,
                            color = if (details.isWintering) colors.accent.copy(alpha = 0.50f) else colors.border.copy(alpha = 0.50f),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .formaPressEffect(targetScale = 0.98f) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTogglePause()
                        }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(if (details.isWintering) colors.accent else colors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AcUnit,
                                    contentDescription = null,
                                    tint = if (details.isWintering) colors.onAccent else colors.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Wintering & Rest Mode",
                                    fontWeight = FontWeight.Bold,
                                    color = if (details.isWintering) colors.accent else colors.textPrimary,
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = if (details.isWintering)
                                        "Hibernating · Streak is frozen & protected"
                                    else
                                        "Guilt-free pause for recovery. Streak never breaks",
                                    color = colors.textSecondary,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Status indicator pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (details.isWintering) colors.accent else colors.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (details.isWintering) "FROZEN" else "AWAKE",
                                fontWeight = FontWeight.Bold,
                                color = if (details.isWintering) colors.onAccent else colors.textTertiary,
                                fontSize = 10.sp,
                                letterSpacing = 0.6.sp
                            )
                        }
                    }
                }
            }

            // ── 5. Subtasks / Micro-Steps Checklist (Instant Ticking) ────
            if (localSubtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                val doneSubtasks = localSubtasks.count { it.completed }
                val progressFraction = doneSubtasks.toFloat() / localSubtasks.size.toFloat()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MICRO-STEPS & CHECKLIST",
                        style = FormaTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textTertiary,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "$doneSubtasks of ${localSubtasks.size} done",
                        style = FormaTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progressFraction)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    localSubtasks.forEach { subtask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceVariant.copy(alpha = 0.4f))
                                .border(1.dp, colors.border.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .formaPressEffect(targetScale = 0.97f) {
                                    // Immediate in-memory flip (0ms latency)
                                    localSubtasks = localSubtasks.map {
                                        if (it.id == subtask.id) it.copy(completed = !it.completed) else it
                                    }
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onToggleSubtask(subtask.id)
                                }
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
                                style = FormaTheme.typography.bodyMedium,
                                color = if (subtask.completed) colors.textTertiary else colors.textPrimary,
                                textDecoration = if (subtask.completed) TextDecoration.LineThrough else TextDecoration.None,
                                fontSize = 13.5.sp
                            )
                        }
                    }
                }
            }

            // ── 6. Notes & Habit Anchor ─────────────────────────────────
            if (notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "HABIT ANCHOR & NOTES",
                    style = FormaTheme.typography.labelSmall,
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
                        .border(1.dp, colors.border.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = notes,
                        style = FormaTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 7. Skip Habit Action (Guilt-Free Streak Protection) ───────
            if (details.isHabit && !localIsDone) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .formaPressEffect(targetScale = 0.97f) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDismiss()
                            onSkipHabit()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Skip for today",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Skip for Today • Streak Preserved",
                            style = FormaTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── 8. Action Buttons (Focus & Edit) ──────────────────────────
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
                        .formaPressEffect(targetScale = 0.94f) {
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
                            style = FormaTheme.typography.titleSmall,
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
                        .formaPressEffect(targetScale = 0.95f) {
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
                            style = FormaTheme.typography.titleSmall,
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
    val streakCount: Int,
    val isWintering: Boolean = false,
    val canPause: Boolean = false,
    val isHabit: Boolean = false,
    val isSkipped: Boolean = false
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
