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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.icon.FormaIcon
import com.forma.app.core.designsystem.motion.formaPressEffect
import com.forma.app.domain.model.TimeOfDay
import com.forma.app.domain.model.TodayScheduleItem

/**
 * Apple HIG-inspired Quick Action / Haptic Touch context menu.
 * Surfaces contextual, high-velocity actions with instant visual feedback and haptic confirmation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitContextMenuSheet(
    item: TodayScheduleItem,
    onDismiss: () -> Unit,
    onStartFocus: (TodayScheduleItem) -> Unit,
    onToggleCompletion: (TodayScheduleItem) -> Unit,
    onToggleSkip: (TodayScheduleItem) -> Unit,
    onToggleWintering: (TodayScheduleItem) -> Unit,
    onEditHabit: (TodayScheduleItem) -> Unit,
    isReadOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = FormaTheme.colors
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val (title, iconKey, subtitle, streak, isDone, isSkipped, isWintering, accentColor) = when (item) {
        is TodayScheduleItem.HabitItem -> {
            val habit = item.habit
            val timeDesc = when (habit.timeOfDay) {
                TimeOfDay.MORNING -> "Morning • 15 min"
                TimeOfDay.AFTERNOON -> "Afternoon • 25 min"
                TimeOfDay.EVENING -> "Evening • 20 min"
                TimeOfDay.ANYTIME -> "Anytime today"
            }
            val accent = try {
                Color(android.graphics.Color.parseColor(habit.colorTag))
            } catch (_: Exception) {
                colors.accent
            }
            Octad(
                habit.name,
                habit.icon,
                timeDesc,
                item.currentStreak,
                item.isDoneToday,
                item.isSkippedToday,
                habit.isWintering,
                accent
            )
        }
        is TodayScheduleItem.TimelineBlock -> {
            val task = item.item
            val timeDesc = if (task.startTime != null && task.endTime != null) {
                "${task.startTime} - ${task.endTime}"
            } else if (task.startTime != null) {
                "At ${task.startTime}"
            } else {
                "Intention"
            }
            val accent = try {
                Color(android.graphics.Color.parseColor(task.colorTag))
            } catch (_: Exception) {
                colors.accent
            }
            Octad(
                task.title,
                task.icon,
                timeDesc,
                0,
                task.completed,
                false,
                false,
                accent
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
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(colors.border.copy(alpha = 0.7f))
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            // ── Apple HIG Header Preview ────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accentColor.copy(alpha = 0.16f))
                        .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    FormaIcon(
                        iconKey = iconKey,
                        contentDescription = title,
                        modifier = Modifier.size(24.dp),
                        tint = accentColor
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = title,
                            style = FormaTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (streak > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.accentSoft)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = colors.accent,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "$streak d",
                                    style = FormaTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isWintering) "Wintering Sanctuary • Guilt-free pause"
                        else if (isSkipped) "Rested today • Streak preserved"
                        else subtitle,
                        style = FormaTheme.typography.bodySmall,
                        color = if (isWintering || isSkipped) colors.accent else colors.textSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // ── Grouped Apple Context Action List ───────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceVariant.copy(alpha = 0.45f))
                    .border(1.dp, colors.border.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            ) {
                // Action 1: Focus Sprint
                ContextActionRow(
                    icon = Icons.Rounded.Timer,
                    iconTint = colors.accent,
                    iconBackground = colors.accent.copy(alpha = 0.14f),
                    title = "Begin Focus Sprint",
                    subtitle = "25-minute deep focus session",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onStartFocus(item)
                    }
                )

                HorizontalDivider(color = colors.border.copy(alpha = 0.45f), thickness = 0.6.dp)

                // Action 2: Toggle Completion
                if (!isReadOnly) {
                    ContextActionRow(
                        icon = if (isDone) Icons.AutoMirrored.Rounded.Undo else Icons.Rounded.Check,
                        iconTint = if (isDone) colors.textSecondary else accentColor,
                        iconBackground = if (isDone) colors.surfaceVariant else accentColor.copy(alpha = 0.14f),
                        title = if (isDone) "Mark as Incomplete" else "Mark Completed",
                        subtitle = if (isDone) "Reopen this ritual for today" else "Log ritual progress and celebrate",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleCompletion(item)
                        }
                    )

                    HorizontalDivider(color = colors.border.copy(alpha = 0.45f), thickness = 0.6.dp)
                }

                // Action 3: Skip / Rest Today (Only for Habit items)
                if (item is TodayScheduleItem.HabitItem && !isReadOnly) {
                    ContextActionRow(
                        icon = Icons.Rounded.SkipNext,
                        iconTint = if (isSkipped) colors.accent else colors.textSecondary,
                        iconBackground = if (isSkipped) colors.accent.copy(alpha = 0.14f) else colors.surfaceVariant,
                        title = if (isSkipped) "Resume for Today" else "Rest Today (Preserve Streak)",
                        subtitle = if (isSkipped) "Include back into today's deck" else "Guilt-free rest without streak break",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleSkip(item)
                        }
                    )

                    HorizontalDivider(color = colors.border.copy(alpha = 0.45f), thickness = 0.6.dp)
                }

                // Action 4: Wintering Sanctuary Mode (Only for Habit items)
                if (item is TodayScheduleItem.HabitItem) {
                    ContextActionRow(
                        icon = Icons.Rounded.AcUnit,
                        iconTint = if (isWintering) colors.accent else colors.textTertiary,
                        iconBackground = if (isWintering) colors.accentSoft else colors.surfaceVariant,
                        title = if (isWintering) "Wake from Hibernation" else "Enter Wintering Sanctuary",
                        subtitle = if (isWintering) "Reactivate ritual into active schedule" else "Pause ritual indefinitely without penalties",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleWintering(item)
                        }
                    )

                    HorizontalDivider(color = colors.border.copy(alpha = 0.45f), thickness = 0.6.dp)
                }

                // Action 5: Edit Ritual Details
                ContextActionRow(
                    icon = Icons.Rounded.Edit,
                    iconTint = colors.textPrimary,
                    iconBackground = colors.surfaceVariant,
                    title = "Edit Ritual Details",
                    subtitle = "Adjust cues, reminders, subtasks, or timing",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onEditHabit(item)
                    }
                )
            }
        }
    }
}

@Composable
private fun ContextActionRow(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = FormaTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .formaPressEffect(targetScale = 0.98f) { onClick() }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = FormaTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                fontSize = 14.5.sp
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                style = FormaTheme.typography.bodySmall,
                color = colors.textSecondary,
                fontSize = 11.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private data class Octad<A, B, C, D, E, F, G, H>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G,
    val eighth: H
)
