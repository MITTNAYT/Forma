package com.habitflow.app.ui.today.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.util.DateUtils
import java.time.LocalDate

@Composable
fun DateStripHeader(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = NotionTheme.colors
    val today = DateUtils.today()
    val weekDates = DateUtils.getWeekDates(selectedDate)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Month and navigation row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = selectedDate.format(DateUtils.MONTH_DAY_FORMATTER),
                    style = NotionTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
                Text(
                    text = if (selectedDate == today) "Today" else selectedDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = NotionTheme.typography.bodySmall,
                    color = if (selectedDate == today) colors.accent else colors.textSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onDateSelected(selectedDate.minusWeeks(1)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "Previous Week",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (selectedDate != today) {
                    Text(
                        text = "Today",
                        style = NotionTheme.typography.labelMedium,
                        color = colors.accent,
                        modifier = Modifier
                            .clip(NotionTheme.shapes.extraSmall)
                            .background(colors.accentMuted)
                            .clickable { onDateSelected(today) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = { onDateSelected(selectedDate.plusWeeks(1)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Next Week",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Week strip (Mon-Sun)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDates.forEach { date ->
                val isSelected = date == selectedDate
                val isToday = date == today

                val cellBg by animateColorAsState(
                    targetValue = if (isSelected) colors.textPrimary else Color.Transparent,
                    label = "date_strip_bg"
                )
                val dayTextColor by animateColorAsState(
                    targetValue = if (isSelected) colors.surface else colors.textPrimary,
                    label = "date_strip_text"
                )
                val dayOfWeekColor by animateColorAsState(
                    targetValue = if (isSelected) colors.surface.copy(alpha = 0.8f) else colors.textTertiary,
                    label = "date_strip_dow"
                )

                Column(
                    modifier = Modifier
                        .width(42.dp)
                        .clip(NotionTheme.shapes.small)
                        .background(cellBg)
                        .border(
                            1.dp,
                            if (isSelected) Color.Transparent else if (isToday) colors.accent.copy(alpha = 0.5f) else colors.border,
                            NotionTheme.shapes.small
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onDateSelected(date)
                        }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date.dayOfWeek.name.take(1),
                        style = NotionTheme.typography.labelSmall,
                        color = dayOfWeekColor,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = date.dayOfMonth.toString(),
                        style = NotionTheme.typography.titleMedium,
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                        color = dayTextColor
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // Small indicator dot for today
                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) colors.surface else colors.accent)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                }
            }
        }
    }
}
