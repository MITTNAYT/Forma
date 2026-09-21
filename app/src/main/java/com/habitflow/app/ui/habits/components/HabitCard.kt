package com.habitflow.app.ui.habits.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.component.NotionCard
import com.habitflow.app.core.designsystem.component.StreakBadge
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.HabitStreakInfo
import com.habitflow.app.ui.today.components.EnergyIndicator

@Composable
fun HabitCard(
    habit: Habit,
    streakInfo: HabitStreakInfo?,
    onEdit: () -> Unit,
    onArchiveToggle: () -> Unit,
    onWinteringToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = NotionTheme.colors
    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")

    NotionCard(
        modifier = modifier.fillMaxWidth(),
        shape = NotionTheme.shapes.small,
        onClick = onEdit
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(NotionTheme.shapes.extraSmall)
                            .background(if (habit.isWintering) Color(0xFFE3EBE7) else colors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        com.habitflow.app.core.designsystem.icon.HabitFlowIcon(
                            iconKey = habit.icon,
                            contentDescription = habit.name,
                            tint = if (habit.isWintering) Color(0xFF6B8780) else colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = habit.name,
                                style = NotionTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            if (habit.isWintering) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(NotionTheme.shapes.extraSmall)
                                        .background(Color(0xFFE1EAE6))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.AcUnit,
                                            contentDescription = null,
                                            tint = Color(0xFF4A7268),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Wintering",
                                            style = NotionTheme.typography.labelSmall,
                                            color = Color(0xFF4A7268),
                                            fontSize = 9.5.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.padding(top = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = habit.timeOfDay.displayName,
                                style = NotionTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )

                            Spacer(modifier = Modifier.width(6.dp))
                            EnergyIndicator(energyLevel = habit.energyLevel)

                            if (habit.reminderTimeMinutes != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Rounded.Notifications,
                                    contentDescription = "Reminder enabled",
                                    tint = colors.textTertiary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    streakInfo?.let {
                        StreakBadge(streakCount = it.currentStreak)
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Wintering Streak Freeze Toggle
                    IconButton(
                        onClick = onWinteringToggle,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AcUnit,
                            contentDescription = if (habit.isWintering) "Resume Habit" else "Freeze / Wintering Mode",
                            tint = if (habit.isWintering) Color(0xFF4A7268) else colors.textTertiary.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onArchiveToggle,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (habit.archived) Icons.Rounded.Unarchive else Icons.Rounded.Archive,
                            contentDescription = if (habit.archived) "Unarchive" else "Archive",
                            tint = colors.textTertiary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Repeat days indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..7) {
                        val isScheduled = habit.repeatDays.isEmpty() || habit.repeatDays.contains(i)
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(NotionTheme.shapes.extraSmall)
                                .background(if (isScheduled) colors.textPrimary else colors.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNames[i - 1],
                                style = NotionTheme.typography.labelSmall,
                                color = if (isScheduled) colors.surface else colors.textTertiary,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                streakInfo?.let {
                    Text(
                        text = "Best: ${it.longestStreak}d",
                        style = NotionTheme.typography.labelSmall,
                        color = colors.textTertiary
                    )
                }
            }
        }
    }
}
