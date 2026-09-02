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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.component.NotionRingToggle
import com.habitflow.app.core.designsystem.icon.HabitFlowIcon
import com.habitflow.app.domain.model.TodayScheduleItem
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun SpatialTimelineBlock(
    item: TodayScheduleItem,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = NotionTheme.colors

    // Extract item details
    val (title, iconKey, colorTagHex, timeString, durationMinutes, isRecurring, subtasksCount, isDone) = when (item) {
        is TodayScheduleItem.HabitItem -> {
            val habit = item.habit
            val defaultTime = when (habit.timeOfDay) {
                com.habitflow.app.domain.model.TimeOfDay.MORNING -> "07:30 AM"
                com.habitflow.app.domain.model.TimeOfDay.AFTERNOON -> "01:00 PM"
                com.habitflow.app.domain.model.TimeOfDay.EVENING -> "08:00 PM"
                com.habitflow.app.domain.model.TimeOfDay.ANYTIME -> "Anytime"
            }
            val timeFormatted = habit.reminderTimeMinutes?.let { minutes ->
                val h = minutes / 60
                val m = minutes % 60
                val amPm = if (h < 12) "AM" else "PM"
                val h12 = if (h % 12 == 0) 12 else h % 12
                String.format("%02d:%02d %s", h12, m, amPm)
            } ?: defaultTime

            ScheduleBlockData(
                title = habit.name,
                iconKey = habit.icon,
                colorTagHex = habit.colorTag,
                timeString = timeFormatted,
                durationMinutes = 30, // Default routine duration
                isRecurring = true,
                subtasksCount = 0,
                isDone = item.isDoneToday
            )
        }
        is TodayScheduleItem.TimelineBlock -> {
            val task = item.item
            val (timeFormatted, durMinutes) = parseTaskDuration(task.startTime, task.endTime)
            ScheduleBlockData(
                title = task.title,
                iconKey = task.icon,
                colorTagHex = task.colorTag,
                timeString = timeFormatted,
                durationMinutes = durMinutes,
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

    // Dynamic height scaling based on duration (Archetype A: Spatial Scaling)
    val blockMinHeight = when {
        durationMinutes <= 20 -> 64.dp
        durationMinutes <= 45 -> 78.dp
        durationMinutes <= 75 -> 92.dp
        else -> 110.dp
    }

    val blockScale by animateFloatAsState(
        targetValue = if (isDone) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "block_scale"
    )

    val blockBg by animateColorAsState(
        targetValue = if (isDone) colors.surfaceVariant.copy(alpha = 0.4f) else colors.surfaceVariant,
        animationSpec = tween(200),
        label = "block_bg"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(blockScale)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Left Column: Spatial Time Ruler
        Column(
            modifier = Modifier
                .width(64.dp)
                .padding(top = 4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = timeString.take(8),
                style = NotionTheme.typography.labelSmall,
                color = if (isDone) colors.textTertiary else colors.textSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp
            )
            if (durationMinutes > 0) {
                Text(
                    text = if (durationMinutes >= 60) "${durationMinutes / 60}h ${if (durationMinutes % 60 > 0) "${durationMinutes % 60}m" else ""}" else "${durationMinutes}m",
                    style = NotionTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontSize = 10.sp
                )
            }
        }

        // Spatial Time Block Container
        Box(
            modifier = Modifier
                .weight(1f)
                .height(blockMinHeight)
                .clip(RoundedCornerShape(18.dp))
                .background(blockBg)
                .border(
                    1.dp,
                    if (isDone) colors.border.copy(alpha = 0.3f) else tagColor.copy(alpha = 0.45f),
                    RoundedCornerShape(18.dp)
                )
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Vector Icon Node + Title & Details
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDone) colors.surface else tagColor.copy(alpha = 0.2f))
                            .border(1.dp, if (isDone) Color.Transparent else tagColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        HabitFlowIcon(
                            iconKey = iconKey,
                            contentDescription = title,
                            tint = if (isDone) colors.textTertiary else tagColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = NotionTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDone) colors.textTertiary else colors.textPrimary,
                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = 2,
                            fontSize = 15.sp
                        )

                        if (subtasksCount > 0 || isRecurring) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isRecurring) {
                                    Icon(
                                        imageVector = Icons.Rounded.Repeat,
                                        contentDescription = "Recurring",
                                        tint = colors.textTertiary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Ritual",
                                        style = NotionTheme.typography.labelSmall,
                                        color = colors.textTertiary,
                                        fontSize = 10.sp
                                    )
                                }

                                if (isRecurring && subtasksCount > 0) {
                                    Text(text = " • ", color = colors.textTertiary, fontSize = 10.sp)
                                }

                                if (subtasksCount > 0) {
                                    Icon(
                                        imageVector = Icons.Rounded.Menu,
                                        contentDescription = "Subtasks",
                                        tint = colors.textTertiary,
                                        modifier = Modifier.size(12.dp)
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
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Ring Checkoff Toggle
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

private data class ScheduleBlockData(
    val title: String,
    val iconKey: String,
    val colorTagHex: String,
    val timeString: String,
    val durationMinutes: Int,
    val isRecurring: Boolean,
    val subtasksCount: Int,
    val isDone: Boolean
)

private fun parseTaskDuration(startTimeStr: String?, endTimeStr: String?): Pair<String, Int> {
    if (startTimeStr == null) return Pair("Anytime", 30)

    val formatter12h = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
    val startParsed = try { LocalTime.parse(startTimeStr) } catch (_: Exception) { null }
    val endParsed = try { endTimeStr?.let { LocalTime.parse(it) } } catch (_: Exception) { null }

    val formattedStart = startParsed?.format(formatter12h) ?: startTimeStr

    if (endParsed != null && startParsed != null) {
        val minutes = ChronoUnit.MINUTES.between(startParsed, endParsed).toInt()
        val validMinutes = if (minutes > 0) minutes else 60
        return Pair(formattedStart, validMinutes)
    }

    return Pair(formattedStart, 60)
}
