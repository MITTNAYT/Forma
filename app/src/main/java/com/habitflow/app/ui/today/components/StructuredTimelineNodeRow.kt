package com.habitflow.app.ui.today.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.LuxuryIceBlue
import com.habitflow.app.core.designsystem.LuxuryIceBlueGlow
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.component.NotionRingToggle
import com.habitflow.app.core.designsystem.icon.HabitFlowIcon
import com.habitflow.app.domain.model.TodayScheduleItem
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun StructuredTimelineNodeRow(
    item: TodayScheduleItem,
    isFirst: Boolean,
    isLast: Boolean,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = NotionTheme.colors

    // Extract item details
    val (title, iconKey, colorTagHex, timeString, durationString, isRecurring, subtasksCount, isDone) = when (item) {
        is TodayScheduleItem.HabitItem -> {
            val habit = item.habit
            val defaultTime = when (habit.timeOfDay) {
                com.habitflow.app.domain.model.TimeOfDay.MORNING -> "07:00 AM"
                com.habitflow.app.domain.model.TimeOfDay.AFTERNOON -> "01:00 PM"
                com.habitflow.app.domain.model.TimeOfDay.EVENING -> "07:30 PM"
                com.habitflow.app.domain.model.TimeOfDay.ANYTIME -> "Anytime"
            }
            val timeFormatted = habit.reminderTimeMinutes?.let { minutes ->
                val h = minutes / 60
                val m = minutes % 60
                val amPm = if (h < 12) "AM" else "PM"
                val h12 = if (h % 12 == 0) 12 else h % 12
                String.format("%02d:%02d %s", h12, m, amPm)
            } ?: defaultTime

            ScheduleRowData(
                title = habit.name,
                iconKey = habit.icon,
                colorTagHex = habit.colorTag,
                timeString = timeFormatted,
                durationString = "Routine",
                isRecurring = true,
                subtasksCount = 0,
                isDone = item.isDoneToday
            )
        }
        is TodayScheduleItem.TimelineBlock -> {
            val task = item.item
            val (timeFormatted, durFormatted) = formatTaskTiming(task.startTime, task.endTime)
            ScheduleRowData(
                title = task.title,
                iconKey = task.icon,
                colorTagHex = task.colorTag,
                timeString = timeFormatted,
                durationString = durFormatted,
                isRecurring = task.isRecurring,
                subtasksCount = task.subtasks.size,
                isDone = task.completed
            )
        }
    }

    val tagColor = try {
        Color(android.graphics.Color.parseColor(colorTagHex))
    } catch (_: Exception) {
        colors.accent
    }

    val cardScale by animateFloatAsState(
        targetValue = if (isDone) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "row_scale"
    )

    val cardBgColor by animateColorAsState(
        targetValue = if (isDone) colors.surfaceVariant.copy(alpha = 0.5f) else colors.surfaceVariant,
        animationSpec = tween(200),
        label = "card_bg"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .scale(cardScale)
            .padding(horizontal = 16.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Crisp Slate Time
        Box(
            modifier = Modifier.width(62.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = timeString.take(8),
                style = NotionTheme.typography.labelSmall,
                color = if (isDone) colors.textTertiary else colors.textSecondary,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            )
        }

        // Timeline Track with Continuous Line & Connected Icon Node
        Box(
            modifier = Modifier
                .width(50.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            // Continuous Vertical Laser Line
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(colors.timelineLine)
            )

            // Circular Vector Icon Node with Glow
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isDone) colors.surfaceVariant else colors.surface)
                    .border(1.dp, if (isDone) colors.border else colors.border.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                HabitFlowIcon(
                    iconKey = iconKey,
                    contentDescription = title,
                    tint = if (isDone) colors.textTertiary else colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right Content: Luxury Elevated Card Container
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(cardBgColor)
                .border(1.dp, if (isDone) Color.Transparent else colors.border.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Time Interval / Duration Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (durationString.isNotBlank()) "$timeString • $durationString" else timeString,
                            style = NotionTheme.typography.labelSmall,
                            color = if (isDone) colors.textTertiary else colors.textSecondary,
                            fontSize = 11.sp
                        )

                        if (isRecurring) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.Repeat,
                                contentDescription = "Recurring",
                                tint = colors.textTertiary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Title
                    Text(
                        text = title,
                        style = NotionTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDone) colors.textTertiary else colors.textPrimary,
                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 2
                    )

                    if (subtasksCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Subtasks",
                                tint = colors.textTertiary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$subtasksCount subtasks",
                                style = NotionTheme.typography.labelSmall,
                                color = colors.textTertiary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Custom Ring Toggle with Spring Animation
                NotionRingToggle(
                    checked = isDone,
                    onToggle = onToggle,
                    accentColor = tagColor,
                    size = 26.dp
                )
            }
        }
    }
}

private data class ScheduleRowData(
    val title: String,
    val iconKey: String,
    val colorTagHex: String,
    val timeString: String,
    val durationString: String,
    val isRecurring: Boolean,
    val subtasksCount: Int,
    val isDone: Boolean
)

private fun formatTaskTiming(startTimeStr: String?, endTimeStr: String?): Pair<String, String> {
    if (startTimeStr == null) return Pair("Anytime", "")

    val formatter12h = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
    val startParsed = try { LocalTime.parse(startTimeStr) } catch (_: Exception) { null }
    val endParsed = try { endTimeStr?.let { LocalTime.parse(it) } } catch (_: Exception) { null }

    val formattedStart = startParsed?.format(formatter12h) ?: startTimeStr

    if (endParsed != null && startParsed != null) {
        val formattedEnd = endParsed.format(formatter12h)
        val minutes = ChronoUnit.MINUTES.between(startParsed, endParsed)
        val durationStr = if (minutes >= 60) {
            val hours = minutes / 60
            val remMinutes = minutes % 60
            if (remMinutes > 0) "${hours}h ${remMinutes}m" else "${hours}h"
        } else {
            "${minutes}m"
        }
        return Pair("$formattedStart - $formattedEnd", durationStr)
    }

    return Pair(formattedStart, "")
}
