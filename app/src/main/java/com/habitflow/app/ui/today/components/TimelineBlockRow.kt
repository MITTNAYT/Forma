package com.habitflow.app.ui.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.component.NotionCard
import com.habitflow.app.core.designsystem.component.NotionCheckbox
import com.habitflow.app.core.util.DateUtils
import com.habitflow.app.domain.model.TodayScheduleItem
import java.time.LocalTime

@Composable
fun TimelineBlockRow(
    item: TodayScheduleItem.TimelineBlock,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = NotionTheme.colors
    val task = item.item

    val startTimeParsed = task.startTime?.let { try { LocalTime.parse(it) } catch (_: Exception) { null } }
    val endTimeParsed = task.endTime?.let { try { LocalTime.parse(it) } catch (_: Exception) { null } }
    val timeRangeText = DateUtils.formatTimeRange(startTimeParsed, endTimeParsed)

    val tagColor = try {
        Color(android.graphics.Color.parseColor(task.colorTag))
    } catch (_: Exception) {
        colors.accent
    }

    NotionCard(
        modifier = modifier.fillMaxWidth(),
        shape = NotionTheme.shapes.small,
        backgroundColor = if (task.completed) colors.surfaceVariant.copy(alpha = 0.5f) else colors.surface,
        borderColor = if (task.completed) colors.border.copy(alpha = 0.6f) else colors.border,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left color accent bar (Structured-style)
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(36.dp)
                    .clip(NotionTheme.shapes.extraSmall)
                    .background(if (task.completed) colors.border else tagColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(NotionTheme.shapes.extraSmall)
                    .background(colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = task.icon, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = NotionTheme.typography.titleMedium,
                    color = if (task.completed) colors.textTertiary else colors.textPrimary,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                )

                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeRangeText,
                        style = NotionTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )

                    if (task.subtasks.isNotEmpty()) {
                        val doneCount = task.subtasks.count { it.completed }
                        val totalCount = task.subtasks.size
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$doneCount/$totalCount subtasks",
                            style = NotionTheme.typography.labelSmall,
                            color = if (doneCount == totalCount && totalCount > 0) colors.accent else colors.textTertiary
                        )
                    }
                }

                if (task.notes.isNotBlank() && !task.completed) {
                    Text(
                        text = task.notes,
                        style = NotionTheme.typography.bodySmall,
                        color = colors.textTertiary,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            NotionCheckbox(
                checked = task.completed,
                onCheckedChange = { onToggle() },
                accentColor = tagColor
            )
        }
    }
}
