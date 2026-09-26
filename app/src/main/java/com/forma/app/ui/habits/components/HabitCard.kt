package com.forma.app.ui.habits.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.component.FormaCard
import com.forma.app.core.designsystem.component.StreakBadge
import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.HabitStreakInfo
import com.forma.app.ui.today.components.EnergyIndicator

@Composable
fun HabitCard(
    habit: Habit,
    streakInfo: HabitStreakInfo?,
    onEdit: () -> Unit,
    onArchiveToggle: () -> Unit,
    onWinteringToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = FormaTheme.colors
    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")

    val habitAccent = androidx.compose.runtime.remember(habit.colorTag) {
        try {
            if (!habit.colorTag.isNullOrBlank()) {
                Color(android.graphics.Color.parseColor(habit.colorTag))
            } else null
        } catch (_: Exception) {
            null
        }
    } ?: colors.accent

    FormaCard(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!habit.colorTag.isNullOrBlank()) {
                    Modifier.border(1.dp, habitAccent.copy(alpha = 0.30f), FormaTheme.shapes.small)
                } else Modifier
            ),
        shape = FormaTheme.shapes.small,
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
                            .clip(FormaTheme.shapes.extraSmall)
                            .background(
                                if (habit.isWintering) colors.accentSoft 
                                else habitAccent.copy(alpha = 0.14f)
                            )
                            .border(
                                1.dp,
                                habitAccent.copy(alpha = 0.30f),
                                FormaTheme.shapes.extraSmall
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        com.forma.app.core.designsystem.icon.FormaIcon(
                            iconKey = habit.icon,
                            contentDescription = habit.name,
                            tint = if (habit.isWintering) colors.accent else habitAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = habit.name,
                                style = FormaTheme.typography.titleMedium,
                                color = colors.textPrimary,
                                maxLines = 2,
                                lineHeight = 20.sp,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (habit.isWintering) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.accentSoft)
                                        .border(1.dp, colors.accent.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.AcUnit,
                                            contentDescription = null,
                                            tint = colors.accent,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Wintering",
                                            style = FormaTheme.typography.labelSmall.copy(
                                                platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
                                                fontFeatureSettings = "tnum"
                                            ),
                                            color = colors.accent,
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
                                style = FormaTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )

                            if (habit.reminderTimeMinutes != null) {
                                val h = habit.reminderTimeMinutes / 60
                                val m = habit.reminderTimeMinutes % 60
                                val amPm = if (h < 12) "AM" else "PM"
                                val h12 = if (h % 12 == 0) 12 else h % 12
                                val timeStr = String.format("%d:%02d %s", h12, m, amPm)
                                Text(
                                    text = " • $timeStr",
                                    style = FormaTheme.typography.labelSmall,
                                    color = colors.textTertiary,
                                    fontSize = 11.sp
                                )
                            }

                            if (habit.subtasks.isNotEmpty()) {
                                Text(
                                    text = " • ${habit.subtasks.size} steps",
                                    style = FormaTheme.typography.labelSmall,
                                    color = habitAccent,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))
                            EnergyIndicator(energyLevel = habit.energyLevel)
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
                            tint = if (habit.isWintering) colors.accent else colors.textTertiary.copy(alpha = 0.6f),
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
                                .clip(FormaTheme.shapes.extraSmall)
                                .background(
                                    if (isScheduled) {
                                        if (!habit.colorTag.isNullOrBlank()) habitAccent else colors.textPrimary
                                    } else colors.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNames[i - 1],
                                style = FormaTheme.typography.labelSmall,
                                color = if (isScheduled) {
                                    if (!habit.colorTag.isNullOrBlank()) colors.onAccent else colors.surface
                                } else colors.textTertiary,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                streakInfo?.let {
                    Text(
                        text = "Best: ${it.longestStreak}d",
                        style = FormaTheme.typography.labelSmall,
                        color = colors.textTertiary
                    )
                }
            }
        }
    }
}
