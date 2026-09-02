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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.icon.HabitFlowIcon
import com.habitflow.app.domain.model.TodayScheduleItem
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun CyberPrecisionTimelineRow(
    item: TodayScheduleItem,
    index: Int,
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
                com.habitflow.app.domain.model.TimeOfDay.MORNING -> "07:30_AM"
                com.habitflow.app.domain.model.TimeOfDay.AFTERNOON -> "01:00_PM"
                com.habitflow.app.domain.model.TimeOfDay.EVENING -> "08:00_PM"
                com.habitflow.app.domain.model.TimeOfDay.ANYTIME -> "ANYTIME"
            }
            val timeFormatted = habit.reminderTimeMinutes?.let { minutes ->
                val h = minutes / 60
                val m = minutes % 60
                val amPm = if (h < 12) "AM" else "PM"
                val h12 = if (h % 12 == 0) 12 else h % 12
                String.format("%02d:%02d_%s", h12, m, amPm)
            } ?: defaultTime

            CyberRowData(
                title = habit.name.uppercase(),
                iconKey = habit.icon,
                colorTagHex = habit.colorTag,
                timeString = timeFormatted,
                durationString = "ROUTINE",
                isRecurring = true,
                subtasksCount = 0,
                isDone = item.isDoneToday
            )
        }
        is TodayScheduleItem.TimelineBlock -> {
            val task = item.item
            val (timeFormatted, durFormatted) = formatCyberTiming(task.startTime, task.endTime)
            CyberRowData(
                title = task.title.uppercase(),
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
        Color(0xFFFF334B) // Industrial Phosphor Red
    }

    val rowScale by animateFloatAsState(
        targetValue = if (isDone) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "cyber_row_scale"
    )

    // Archetype C: Industrial Cyber-Precision Squircle Card
    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(rowScale)
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDone) Color(0xFF08090C) else Color(0xFF0F1117))
            .border(
                1.5.dp,
                if (isDone) Color(0xFF1B1E28) else Color(0xFF2B3142),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mechanical LED Checkbox [ ■ ]
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isDone) Color.White else Color(0xFF171A24))
                        .border(1.5.dp, if (isDone) Color.White else Color(0xFF3B435A), RoundedCornerShape(6.dp))
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Done",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Vector Icon Glyph in Industrial Squircle
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDone) Color(0xFF14161F) else Color(0xFF1E2330))
                        .border(1.dp, Color(0xFF323A4E), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    HabitFlowIcon(
                        iconKey = iconKey,
                        contentDescription = title,
                        tint = if (isDone) Color(0xFF555F7A) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Technical Monospace Metadata & Title
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "// SEQ_${String.format("%02d", index + 1)} • $timeString",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isDone) Color(0xFF4B556D) else Color(0xFF8B9BB4),
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )

                        if (isRecurring) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "[RITUAL]",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isDone) Color(0xFF4B556D) else Color(0xFFFF5252),
                                fontSize = 9.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = title,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isDone) Color(0xFF555F7A) else Color.White,
                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )

                    if (subtasksCount > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "SUBTASKS // $subtasksCount ACTIVE",
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF5E6B87),
                            fontSize = 9.sp
                        )
                    }
                }
            }

            // Industrial Status Glyph
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isDone) Color(0xFF141720) else tagColor.copy(alpha = 0.15f))
                    .border(1.dp, if (isDone) Color(0xFF262C3D) else tagColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isDone) "DONE" else "EXEC",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) Color(0xFF555F7A) else tagColor,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

private data class CyberRowData(
    val title: String,
    val iconKey: String,
    val colorTagHex: String,
    val timeString: String,
    val durationString: String,
    val isRecurring: Boolean,
    val subtasksCount: Int,
    val isDone: Boolean
)

private fun formatCyberTiming(startTimeStr: String?, endTimeStr: String?): Pair<String, String> {
    if (startTimeStr == null) return Pair("ANYTIME", "")

    val formatter12h = DateTimeFormatter.ofPattern("hh:mm_a", Locale.getDefault())
    val startParsed = try { LocalTime.parse(startTimeStr) } catch (_: Exception) { null }
    val endParsed = try { endTimeStr?.let { LocalTime.parse(it) } } catch (_: Exception) { null }

    val formattedStart = startParsed?.format(formatter12h) ?: startTimeStr

    if (endParsed != null && startParsed != null) {
        val formattedEnd = endParsed.format(formatter12h)
        val minutes = ChronoUnit.MINUTES.between(startParsed, endParsed)
        val durationStr = if (minutes >= 60) {
            val hours = minutes / 60
            val remMinutes = minutes % 60
            if (remMinutes > 0) "${hours}H_${remMinutes}M" else "${hours}H"
        } else {
            "${minutes}M"
        }
        return Pair("$formattedStart - $formattedEnd", durationStr)
    }

    return Pair(formattedStart, "")
}
