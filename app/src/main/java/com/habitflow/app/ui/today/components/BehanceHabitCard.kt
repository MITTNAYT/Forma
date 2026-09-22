package com.habitflow.app.ui.today.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.FormaTheme
import com.habitflow.app.core.designsystem.motion.FormaMotion
import com.habitflow.app.core.designsystem.motion.formaPressEffect
import com.habitflow.app.core.designsystem.icon.HabitFlowIcon
import com.habitflow.app.domain.model.TodayScheduleItem
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BehanceHabitCard(
    item: TodayScheduleItem,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onStartFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FormaTheme.colors
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Extract item details
    val cardData = when (item) {
        is TodayScheduleItem.HabitItem -> {
            val habit = item.habit
            val defaultTime = when (habit.timeOfDay) {
                com.habitflow.app.domain.model.TimeOfDay.MORNING -> "7:00 AM • 15 min"
                com.habitflow.app.domain.model.TimeOfDay.AFTERNOON -> "1:00 PM • 25 min"
                com.habitflow.app.domain.model.TimeOfDay.EVENING -> "6:00 PM • 20 min"
                com.habitflow.app.domain.model.TimeOfDay.ANYTIME -> "Anytime today • 20 min"
            }
            val timeFormatted = habit.reminderTimeMinutes?.let { minutes ->
                val h = minutes / 60
                val m = minutes % 60
                val amPm = if (h < 12) "AM" else "PM"
                val h12 = if (h % 12 == 0) 12 else h % 12
                String.format("%d:%02d %s", h12, m, amPm)
            } ?: defaultTime

            val isPeach = habit.name.contains("water", ignoreCase = true) || habit.name.contains("read", ignoreCase = true)

            BehanceCardData(
                title = habit.name,
                iconKey = habit.icon,
                colorTagHex = habit.colorTag,
                subtitle = timeFormatted,
                isDone = item.isDoneToday,
                isPeachTile = isPeach,
                cueText = habit.stackedCueText,
                isWintering = habit.isWintering
            )
        }
        is TodayScheduleItem.TimelineBlock -> {
            val task = item.item
            val timing = formatBehanceTiming(task.startTime, task.endTime)
            val isPeach = task.title.contains("water", ignoreCase = true) || task.title.contains("read", ignoreCase = true)

            BehanceCardData(
                title = task.title,
                iconKey = task.icon,
                colorTagHex = task.colorTag,
                subtitle = timing,
                isDone = task.completed,
                isPeachTile = isPeach,
                cueText = null,
                isWintering = false
            )
        }
    }

    val title = cardData.title
    val iconKey = cardData.iconKey
    val subtitle = cardData.subtitle
    val isDone = cardData.isDone
    val isPeachTile = cardData.isPeachTile

    val tileBg = if (isPeachTile) colors.accentSoft else colors.surfaceVariant
    val tileIconColor = colors.accent

    val cardScale by animateFloatAsState(
        targetValue = if (isDone) 0.99f else 1f,
        animationSpec = FormaMotion.snappyFloat,
        label = "card_scale"
    )

    // Checkbox bounce animation & spark burst
    val checkScale = remember { Animatable(1f) }
    val sparkProgress = remember { Animatable(0f) }

    val handleToggleWithBurst = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch {
            checkScale.snapTo(0.85f)
            checkScale.animateTo(
                targetValue = 1.25f,
                animationSpec = FormaMotion.bouncyFloat
            )
            checkScale.animateTo(
                targetValue = 1f,
                animationSpec = FormaMotion.snappyFloat
            )
        }
        if (!isDone) {
            scope.launch {
                sparkProgress.snapTo(0f)
                sparkProgress.animateTo(1f, animationSpec = tween(durationMillis = 320, easing = FormaMotion.naturalDecelerate))
                sparkProgress.snapTo(0f)
            }
        }
        onToggle()
    }

    // Warm Clean Card with GPU-accelerated press and scale
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
            .formaPressEffect(targetScale = 0.975f) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
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
                // Left Pastel Squircle Tile
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(tileBg),
                    contentAlignment = Alignment.Center
                ) {
                    HabitFlowIcon(
                        iconKey = iconKey,
                        contentDescription = title,
                        tint = tileIconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Title and Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = FormaTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDone || cardData.isWintering) colors.textSecondary else colors.textPrimary,
                            fontSize = 16.sp,
                            letterSpacing = (-0.2).sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (cardData.isWintering) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🍵 Wintering",
                                style = FormaTheme.typography.labelSmall,
                                color = colors.accent,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.accentSoft)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = subtitle,
                        style = FormaTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    if (!cardData.cueText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "↳ ${cardData.cueText}",
                            style = FormaTheme.typography.bodySmall,
                            color = colors.textTertiary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Action Cluster: Focus Timer Button + Checkbox
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pomodoro Focus Clock Trigger Pill
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.accentSoft)
                        .clickable { onStartFocus() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = "Start Focus Clock",
                        tint = colors.accent,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Checkbox with Spark Burst Canvas & Spring Scale
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Particle Spark Burst
                    if (sparkProgress.value > 0f) {
                        Canvas(modifier = Modifier.size(40.dp)) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val progress = sparkProgress.value
                            val radius = 14.dp.toPx() + (progress * 10.dp.toPx())
                            val alpha = (1f - progress).coerceIn(0f, 1f)

                            val particleCount = 6
                            for (i in 0 until particleCount) {
                                val angle = (i * (360.0 / particleCount) + 15.0) * (Math.PI / 180.0)
                                val x = center.x + (radius * cos(angle)).toFloat()
                                val y = center.y + (radius * sin(angle)).toFloat()
                                drawCircle(
                                    color = colors.accent.copy(alpha = alpha),
                                    radius = (2.5.dp.toPx() * (1f - progress)).coerceAtLeast(1f),
                                    center = Offset(x, y)
                                )
                            }
                        }
                    }

                    // Checkbox Circle
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = checkScale.value
                                scaleY = checkScale.value
                            }
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isDone) colors.accent else Color.Transparent)
                            .border(
                                1.5.dp,
                                if (isDone) colors.accent else colors.border,
                                CircleShape
                            )
                            .clickable { handleToggleWithBurst() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Completed",
                                tint = colors.onAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class BehanceCardData(
    val title: String,
    val iconKey: String,
    val colorTagHex: String,
    val subtitle: String,
    val isDone: Boolean,
    val isPeachTile: Boolean,
    val cueText: String? = null,
    val isWintering: Boolean = false
)

private fun formatBehanceTiming(startTimeStr: String?, endTimeStr: String?): String {
    if (startTimeStr == null) return "Anytime today"

    val formatter12h = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    val startParsed = try { LocalTime.parse(startTimeStr) } catch (_: Exception) { null }
    val endParsed = try { endTimeStr?.let { LocalTime.parse(it) } } catch (_: Exception) { null }

    val formattedStart = startParsed?.format(formatter12h) ?: startTimeStr

    if (endParsed != null && startParsed != null) {
        val minutes = ChronoUnit.MINUTES.between(startParsed, endParsed)
        return "$formattedStart • ${minutes} min"
    }

    return formattedStart
}
