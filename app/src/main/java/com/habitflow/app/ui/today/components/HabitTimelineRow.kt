package com.habitflow.app.ui.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.FormaTheme
import com.habitflow.app.core.designsystem.component.FormaCard
import com.habitflow.app.core.designsystem.component.FormaRingToggle
import com.habitflow.app.core.designsystem.component.StreakBadge
import com.habitflow.app.domain.model.EnergyLevel
import com.habitflow.app.domain.model.TodayScheduleItem

@Composable
fun HabitTimelineRow(
    item: TodayScheduleItem.HabitItem,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FormaTheme.colors
    val habit = item.habit

    FormaCard(
        modifier = modifier.fillMaxWidth(),
        shape = FormaTheme.shapes.small,
        backgroundColor = if (item.isDoneToday) colors.surfaceVariant.copy(alpha = 0.5f) else colors.surface,
        borderColor = if (item.isDoneToday) colors.border.copy(alpha = 0.6f) else colors.border
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
                // Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(FormaTheme.shapes.extraSmall)
                        .background(colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = habit.icon, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = habit.name,
                        style = FormaTheme.typography.titleMedium,
                        color = if (item.isDoneToday) colors.textTertiary else colors.textPrimary,
                        textDecoration = if (item.isDoneToday) TextDecoration.LineThrough else TextDecoration.None
                    )

                    Row(
                        modifier = Modifier.padding(top = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = habit.timeOfDay.displayName,
                            style = FormaTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Energy dots indicator
                        EnergyIndicator(energyLevel = habit.energyLevel)

                        if (item.currentStreak > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            StreakBadge(streakCount = item.currentStreak)
                        }
                    }
                }
            }

            FormaRingToggle(
                checked = item.isDoneToday,
                onToggle = onToggle
            )
        }
    }
}

@Composable
fun EnergyIndicator(energyLevel: EnergyLevel, modifier: Modifier = Modifier) {
    val colors = FormaTheme.colors

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..3) {
            val filled = i <= energyLevel.dotCount
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (filled) colors.textSecondary else colors.border)
            )
        }
    }
}
