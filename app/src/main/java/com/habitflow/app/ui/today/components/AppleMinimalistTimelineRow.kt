package com.habitflow.app.ui.today.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
fun AppleMinimalistTimelineRow(
    item: TodayScheduleItem,
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

            AppleRowData(
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
            val (timeFormatted, durFormatted) = formatAppleTiming(task.startTime, task.endTime)
            AppleRowData(
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

    val rowScale by animateFloatAsState(
        targetValue = if (isDone) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "apple_row_scale"
    )

    // Archetype B (Apple Minimalist): Clean, breathable cardless row with hairline divider
    Column(
        modifier = modifier
            .fillMaxWidth()
            .scale(rowScale)
            .padding(horizontal = 20.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() }
                .padding(vertical = 12.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tactile Spring Ring Toggle on the Left (Things 3 style)
                NotionRingToggle(
                    checked = isDone,
                    onToggle = onToggle,
                    accentColor = tagColor,
                    size = 24.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Vector Icon Glyph in subtle pill
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDone) colors.surfaceVariant.copy(alpha = 0.4f) else colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    HabitFlowIcon(
                        iconKey = iconKey,
                        contentDescription = title,
                        tint = if (isDone) colors.textTertiary else colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Title and Metadata
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = NotionTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDone) colors.textTertiary else colors.textPrimary,
                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                        fontSize = 16.sp,
                        letterSpacing = (-0.2).sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (durationString.isNotBlank()) "$timeString • $durationString" else timeString,
                            style = NotionTheme.typography.labelSmall,
                            color = colors.textTertiary,
                            fontSize = 12.sp
                        )

                        if (isRecurring) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.Repeat,
                                contentDescription = "Routine",
                                tint = colors.textTertiary,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        if (subtasksCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Subtasks",
                                tint = colors.textTertiary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$subtasksCount",
                                style = NotionTheme.typography.labelSmall,
                                color = colors.textTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Subtle color tag dot on the right
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isDone) colors.textTertiary.copy(alpha = 0.3f) else tagColor)
            )
        }

        // Hairline divider between items
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 46.dp)
                .height(0.6.dp)
                .background(colors.border.copy(alpha = 0.4f))
        )
    }
}

private data class AppleRowData(
    val title: String,
    val iconKey: String,
    val colorTagHex: String,
    val timeString: String,
    val durationString: String,
    val isRecurring: Boolean,
    val subtasksCount: Int,
    val isDone: Boolean
)

private fun formatAppleTiming(startTimeStr: String?, endTimeStr: String?): Pair<String, String> {
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
