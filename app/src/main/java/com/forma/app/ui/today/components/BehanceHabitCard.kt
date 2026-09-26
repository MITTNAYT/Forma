package com.forma.app.ui.today.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Redo
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.icon.FormaIcon
import com.forma.app.core.designsystem.motion.FormaMotion
import com.forma.app.core.designsystem.motion.formaPressEffect
import com.forma.app.domain.model.TodayScheduleItem
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
    onSkip: () -> Unit = {},
    onUnskip: () -> Unit = {},
    modifier: Modifier = Modifier,
    index: Int = 0
) {
    val colors = FormaTheme.colors
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Extract item details
    val cardData = when (item) {
        is TodayScheduleItem.HabitItem -> {
            val habit = item.habit
            val defaultTime = when (habit.timeOfDay) {
                com.forma.app.domain.model.TimeOfDay.MORNING -> "Morning • 15 min"
                com.forma.app.domain.model.TimeOfDay.AFTERNOON -> "Afternoon • 25 min"
                com.forma.app.domain.model.TimeOfDay.EVENING -> "Evening • 20 min"
                com.forma.app.domain.model.TimeOfDay.ANYTIME -> "Anytime today"
            }
            val timeFormatted = habit.reminderTimeMinutes?.let { minutes ->
                val h = minutes / 60
                val m = minutes % 60
                val amPm = if (h < 12) "AM" else "PM"
                val h12 = if (h % 12 == 0) 12 else h % 12
                String.format("%d:%02d %s • %s", h12, m, amPm, habit.timeOfDay.displayName)
            } ?: defaultTime

            val doneCount = habit.subtasks.count { it.completed }
            val totalCount = habit.subtasks.size
            val isPeach = habit.name.contains("water", ignoreCase = true) || habit.name.contains("read", ignoreCase = true)

            BehanceCardData(
                title = habit.name,
                iconKey = habit.icon,
                colorTagHex = habit.colorTag,
                subtitle = timeFormatted,
                isDone = item.isDoneToday,
                isPeachTile = isPeach,
                cueText = habit.stackedCueText,
                isWintering = habit.isWintering,
                isSkipped = item.isSkippedToday,
                isHabit = true,
                totalSubtasks = totalCount,
                doneSubtasks = doneCount
            )
        }
        is TodayScheduleItem.TimelineBlock -> {
            val task = item.item
            val timing = formatBehanceTiming(task.startTime, task.endTime)
            val doneCount = task.subtasks.count { it.completed }
            val totalCount = task.subtasks.size
            val isPeach = task.title.contains("water", ignoreCase = true) || task.title.contains("read", ignoreCase = true)

            BehanceCardData(
                title = task.title,
                iconKey = task.icon,
                colorTagHex = task.colorTag,
                subtitle = timing,
                isDone = task.completed,
                isPeachTile = isPeach,
                cueText = null,
                isWintering = false,
                isSkipped = false,
                isHabit = false,
                totalSubtasks = totalCount,
                doneSubtasks = doneCount
            )
        }
    }

    val title = cardData.title
    val iconKey = cardData.iconKey
    val subtitle = cardData.subtitle
    val isDone = cardData.isDone
    val isSkipped = cardData.isSkipped
    val isWintering = cardData.isWintering
    val isPeachTile = cardData.isPeachTile

    // ── Parse Habit Custom Color Accent ──────────────────────────────────
    val habitAccent = remember(cardData.colorTagHex) {
        try {
            if (!cardData.colorTagHex.isNullOrBlank()) {
                Color(android.graphics.Color.parseColor(cardData.colorTagHex))
            } else null
        } catch (_: Exception) {
            null
        }
    } ?: colors.accent

    // ── Zero Latency Optimistic State (Frame 0 Feedback) ───────────────
    var localIsDone by remember(cardData.isDone) { mutableStateOf(cardData.isDone) }

    val tileBg = when {
        localIsDone -> colors.surfaceVariant.copy(alpha = 0.6f)
        isSkipped -> colors.surfaceVariant.copy(alpha = 0.4f)
        else -> habitAccent.copy(alpha = 0.14f)
    }
    val tileIconColor = when {
        localIsDone -> colors.textTertiary
        isSkipped -> colors.textTertiary
        else -> habitAccent
    }

    val cardScale by animateFloatAsState(
        targetValue = if (localIsDone || isSkipped) 0.985f else 1f,
        animationSpec = FormaMotion.snappyFloat,
        label = "card_scale"
    )

    // Checkbox bounce animation & spark burst
    val checkScale = remember { Animatable(1f) }
    val sparkProgress = remember { Animatable(0f) }

    val handleToggleWithBurst = {
        val nextState = !localIsDone
        localIsDone = nextState // Immediate visual commit on touch (0ms)
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
        if (nextState) {
            scope.launch {
                sparkProgress.snapTo(0f)
                sparkProgress.animateTo(1f, animationSpec = tween(durationMillis = 300, easing = FormaMotion.naturalDecelerate))
                sparkProgress.snapTo(0f)
            }
        }
        onToggle()
    }

    val formattedIndex = String.format("%02d", index + 1)
    val isHero = (index == 0 && !localIsDone && !isSkipped && !isWintering)

    // Architectural Inset Card with refined border and lighting
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (isSkipped) colors.surface.copy(alpha = 0.70f)
                else colors.surface
            )
            .border(
                width = if (isHero) 1.5.dp else 1.dp,
                color = when {
                    isHero -> habitAccent.copy(alpha = 0.55f)
                    isSkipped -> colors.border.copy(alpha = 0.35f)
                    localIsDone -> colors.border.copy(alpha = 0.40f)
                    !cardData.colorTagHex.isNullOrBlank() -> habitAccent.copy(alpha = 0.35f)
                    else -> colors.border.copy(alpha = 0.65f)
                },
                shape = RoundedCornerShape(22.dp)
            )
            .formaPressEffect(targetScale = 0.985f) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Tier 1: Clean Editorial Header & Action Cluster ────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Metabar: Category pill and hero badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (localIsDone || isSkipped) colors.surfaceVariant
                                else habitAccent.copy(alpha = 0.14f)
                            )
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isWintering) "WINTERING REST" else if (isSkipped) "RESTED TODAY" else subtitle.uppercase(),
                            style = FormaTheme.typography.labelSmall.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                fontFeatureSettings = "tnum"
                            ),
                            fontWeight = FontWeight.Bold,
                            color = if (localIsDone || isSkipped) colors.textSecondary else habitAccent,
                            fontSize = 9.5.sp,
                            letterSpacing = 0.8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right Actions: Skip & Focus Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isSkipped) {
                        // Undo Skip Action
                        Box(
                            modifier = Modifier
                                .height(26.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surfaceVariant)
                                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .formaPressEffect(targetScale = 0.90f) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onUnskip()
                                }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Undo,
                                    contentDescription = "Undo Skip",
                                    tint = colors.accent,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Undo",
                                    style = FormaTheme.typography.labelSmall.copy(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    } else if (!localIsDone && !isWintering) {
                        // Skip Button
                        if (cardData.isHabit) {
                            Box(
                                modifier = Modifier
                                    .height(26.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.surfaceVariant.copy(alpha = 0.6f))
                                    .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .formaPressEffect(targetScale = 0.90f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSkip()
                                    }
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.SkipNext,
                                        contentDescription = "Skip for today",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Skip",
                                        style = FormaTheme.typography.labelSmall.copy(
                                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                                        ),
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Focus Clock Button
                        Box(
                            modifier = Modifier
                                .height(26.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(habitAccent.copy(alpha = 0.12f))
                                .border(1.dp, habitAccent.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .formaPressEffect(targetScale = 0.90f) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onStartFocus()
                                }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Timer,
                                    contentDescription = "Start Focus",
                                    tint = habitAccent,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Focus",
                                    style = FormaTheme.typography.labelSmall.copy(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = habitAccent,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Tier 2: Main Content Row (Icon, Title, Details, Checkbox) ─
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sculptural Icon Squircle
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(tileBg)
                            .border(
                                1.dp,
                                if (isHero) habitAccent.copy(alpha = 0.5f) else habitAccent.copy(alpha = 0.25f),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        FormaIcon(
                            iconKey = iconKey,
                            contentDescription = title,
                            tint = tileIconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Title & Progress Column
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = FormaTheme.typography.titleMedium.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            fontWeight = FontWeight.Bold,
                            color = when {
                                localIsDone -> colors.textTertiary
                                isSkipped -> colors.textTertiary
                                isWintering -> colors.textSecondary
                                else -> colors.textPrimary
                            },
                            fontSize = 15.5.sp,
                            lineHeight = 21.sp,
                            letterSpacing = (-0.2).sp,
                            textDecoration = if (localIsDone) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Cue / Habit Anchor
                        if (!cardData.cueText.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Link,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cardData.cueText,
                                    style = FormaTheme.typography.bodySmall.copy(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    color = colors.textSecondary,
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else if (isSkipped) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Streak protected • Rest freely today",
                                style = FormaTheme.typography.bodySmall.copy(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                ),
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }

                        // Micro-Steps Progress Track
                        if (cardData.totalSubtasks > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .clip(CircleShape)
                                        .background(colors.surfaceVariant)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction = cardData.subtasksProgress)
                                            .height(3.dp)
                                            .clip(CircleShape)
                                            .background(habitAccent)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${cardData.doneSubtasks}/${cardData.totalSubtasks}",
                                    style = FormaTheme.typography.labelSmall.copy(
                                        fontFeatureSettings = "tnum",
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right Interactive Element (Frost Medallion / Instant Checkbox)
                if (isWintering) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.accentSoft)
                            .border(1.dp, colors.accent.copy(alpha = 0.35f), CircleShape)
                            .formaPressEffect(targetScale = 0.92f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AcUnit,
                            contentDescription = "Wintering - Streak Preserved",
                            tint = colors.accent,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                } else if (isSkipped) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.accentSoft)
                            .border(1.dp, colors.accent.copy(alpha = 0.30f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FastForward,
                            contentDescription = "Skipped",
                            tint = colors.accent,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                } else {
                    // Checkbox with Spark Burst Canvas & Instant Spring Toggle
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (sparkProgress.value > 0f) {
                            Canvas(modifier = Modifier.size(36.dp)) {
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val progress = sparkProgress.value
                                val radius = 13.dp.toPx() + (progress * 9.dp.toPx())
                                val alpha = (1f - progress).coerceIn(0f, 1f)

                                val particleCount = 6
                                for (i in 0 until particleCount) {
                                    val angle = (i * (360.0 / particleCount) + 15.0) * (Math.PI / 180.0)
                                    val x = center.x + (radius * cos(angle)).toFloat()
                                    val y = center.y + (radius * sin(angle)).toFloat()
                                    drawCircle(
                                        color = habitAccent.copy(alpha = alpha),
                                        radius = (2.5.dp.toPx() * (1f - progress)).coerceAtLeast(1f),
                                        center = Offset(x, y)
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = checkScale.value
                                    scaleY = checkScale.value
                                }
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (localIsDone) habitAccent else Color.Transparent)
                                .border(
                                    1.5.dp,
                                    if (localIsDone) habitAccent else colors.border,
                                    CircleShape
                                )
                                .clickable { handleToggleWithBurst() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (localIsDone) {
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
}

private data class BehanceCardData(
    val title: String,
    val iconKey: String,
    val colorTagHex: String,
    val subtitle: String,
    val isDone: Boolean,
    val isPeachTile: Boolean,
    val cueText: String? = null,
    val isWintering: Boolean = false,
    val isSkipped: Boolean = false,
    val isHabit: Boolean = false,
    val totalSubtasks: Int = 0,
    val doneSubtasks: Int = 0
) {
    val subtasksProgress: Float
        get() = if (totalSubtasks > 0) doneSubtasks.toFloat() / totalSubtasks.toFloat() else 0f
}

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
