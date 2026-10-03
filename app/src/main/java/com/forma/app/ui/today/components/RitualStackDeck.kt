package com.forma.app.ui.today.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Redo
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.component.FormaButton
import com.forma.app.core.designsystem.component.FormaButtonStyle
import com.forma.app.core.designsystem.component.FormaEmblem
import com.forma.app.core.designsystem.icon.FormaIcon
import com.forma.app.core.designsystem.motion.FormaMotion
import com.forma.app.core.designsystem.motion.formaPressEffect
import com.forma.app.domain.model.TimeOfDay
import com.forma.app.domain.model.TodayScheduleItem
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * High-performance 3D cascading swipable card stack / deck for Today's rituals.
 * Allows users to hyper-focus on one ritual at a time with tactile gestures:
 * - Swipe right: Completes ritual with haptic confirmation and celebratory burst.
 * - Swipe left: Defers or skips ritual to the back of the deck.
 * - Concentric sub-card peek animations (0.95f, 0.90f scales).
 */
@Composable
fun RitualStackDeck(
    items: List<TodayScheduleItem>,
    onToggle: (TodayScheduleItem) -> Unit,
    onClick: (TodayScheduleItem) -> Unit,
    onStartFocus: (TodayScheduleItem) -> Unit,
    onSkip: (TodayScheduleItem) -> Unit,
    onUnskip: (TodayScheduleItem) -> Unit,
    onSwitchToListMode: () -> Unit,
    isReadOnly: Boolean = false,
    onReadOnlyAttempt: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = FormaTheme.colors
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Maintain local deck ordering so cards can be skipped to the back or completed
    var deckItems by remember(items, isReadOnly) {
        mutableStateOf(if (isReadOnly) items else items.filter { !it.isCompleted && !it.isSkipped })
    }
    val allCompletedOrSkipped = items.isNotEmpty() && deckItems.isEmpty() && !isReadOnly

    // History stack for undo capability
    val historyStack = remember { mutableStateListOf<TodayScheduleItem>() }

    // Swipe gesture animatables
    val dragOffsetX = remember { Animatable(0f) }
    val dragOffsetY = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val swipeThresholdPx = with(density) { 110.dp.toPx() }
    val screenWidthPx = with(density) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }

    // Top active item
    val topItem = deckItems.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Deck Meta Header: Card counter + Undo + Mode switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.accent.copy(alpha = 0.14f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (deckItems.isNotEmpty()) {
                            "RITUAL 01 OF ${deckItems.size.toString().padStart(2, '0')}"
                        } else {
                            "ALL RITUALS COMPLETE"
                        },
                        style = FormaTheme.typography.labelSmall.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            fontFeatureSettings = "tnum"
                        ),
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        fontSize = 10.sp,
                        letterSpacing = 1.1.sp
                    )
                }

                if (historyStack.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceVariant)
                            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .formaPressEffect(targetScale = 0.90f) {
                                val lastItem = historyStack.removeLastOrNull()
                                if (lastItem != null) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    deckItems = listOf(lastItem) + deckItems
                                    if (lastItem.isCompleted) {
                                        onToggle(lastItem)
                                    } else if (lastItem.isSkipped) {
                                        onUnskip(lastItem)
                                    }
                                }
                            }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Undo,
                                contentDescription = "Undo",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Undo",
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
            }

            // Switch to list view button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceVariant)
                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .formaPressEffect(targetScale = 0.92f) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSwitchToListMode()
                    }
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.FormatListBulleted,
                        contentDescription = "List View",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "List View",
                        style = FormaTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // ── 3D Card Stack Container ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (allCompletedOrSkipped || deckItems.isEmpty()) {
                // Zen All-Clear Card
                ZenDeckCompletedCard(
                    totalItemsCount = items.size,
                    onSwitchToListMode = onSwitchToListMode,
                    onResetDeck = {
                        deckItems = items
                    }
                )
            } else {
                // Render up to 3 cards in depth order (3 -> 2 -> 1)
                val visibleCards = deckItems.take(3)
                val dragFraction = (dragOffsetX.value / swipeThresholdPx).coerceIn(-1.5f, 1.5f)
                val absFraction = abs(dragFraction).coerceIn(0f, 1f)

                // Background 3rd Card
                if (visibleCards.size >= 3) {
                    val card3 = visibleCards[2]
                    val scale3 = 0.90f + (0.05f * absFraction)
                    val offsetY3 = (28.dp.value - (14.dp.value * absFraction)).dp
                    val alpha3 = 0.60f + (0.15f * absFraction)

                    DeckItemCard(
                        item = card3,
                        modifier = Modifier
                            .zIndex(1f)
                            .offset(y = offsetY3)
                            .scale(scale3)
                            .graphicsLayer { alpha = alpha3 },
                        isTopCard = false
                    )
                }

                // Middle 2nd Card
                if (visibleCards.size >= 2) {
                    val card2 = visibleCards[1]
                    val scale2 = 0.95f + (0.05f * absFraction)
                    val offsetY2 = (14.dp.value - (14.dp.value * absFraction)).dp
                    val alpha2 = 0.85f + (0.15f * absFraction)

                    DeckItemCard(
                        item = card2,
                        modifier = Modifier
                            .zIndex(2f)
                            .offset(y = offsetY2)
                            .scale(scale2)
                            .graphicsLayer { alpha = alpha2 },
                        isTopCard = false
                    )
                }

                // Top Interactive Card (Gesture Bound)
                if (topItem != null) {
                    val tiltDegrees = (dragOffsetX.value / 20f).coerceIn(-14f, 14f)

                    Box(
                        modifier = Modifier
                            .zIndex(3f)
                            .offset {
                                IntOffset(
                                    x = dragOffsetX.value.roundToInt(),
                                    y = dragOffsetY.value.roundToInt()
                                )
                            }
                            .graphicsLayer {
                                rotationZ = tiltDegrees
                            }
                            .pointerInput(topItem.id) {
                                detectDragGestures(
                                    onDragStart = { isDragging = true },
                                    onDragEnd = {
                                        isDragging = false
                                        val currentX = dragOffsetX.value
                                        if (isReadOnly) {
                                            if (kotlin.math.abs(currentX) > swipeThresholdPx * 0.4f) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onReadOnlyAttempt()
                                            }
                                            scope.launch {
                                                dragOffsetX.animateTo(0f, spring(dampingRatio = 0.76f, stiffness = Spring.StiffnessMediumLow))
                                            }
                                            scope.launch {
                                                dragOffsetY.animateTo(0f, spring(dampingRatio = 0.76f, stiffness = Spring.StiffnessMediumLow))
                                            }
                                            return@detectDragGestures
                                        }
                                        if (currentX > swipeThresholdPx) {
                                            // Swipe Right -> COMPLETE
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            scope.launch {
                                                dragOffsetX.animateTo(
                                                    targetValue = screenWidthPx * 1.2f,
                                                    animationSpec = tween(180, easing = FastOutSlowInEasing)
                                                )
                                                historyStack.add(topItem)
                                                deckItems = deckItems.drop(1)
                                                onToggle(topItem)
                                                dragOffsetX.snapTo(0f)
                                                dragOffsetY.snapTo(0f)
                                            }
                                        } else if (currentX < -swipeThresholdPx) {
                                            // Swipe Left -> SKIP / DEFER TO BACK
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            scope.launch {
                                                dragOffsetX.animateTo(
                                                    targetValue = -screenWidthPx * 1.2f,
                                                    animationSpec = tween(180, easing = FastOutSlowInEasing)
                                                )
                                                // Cycle card to the end of deck
                                                historyStack.add(topItem)
                                                deckItems = deckItems.drop(1) + topItem
                                                onSkip(topItem)
                                                dragOffsetX.snapTo(0f)
                                                dragOffsetY.snapTo(0f)
                                            }
                                        } else {
                                            // Spring back to resting center
                                            scope.launch {
                                                dragOffsetX.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = spring(
                                                        dampingRatio = 0.76f,
                                                        stiffness = Spring.StiffnessMediumLow
                                                    )
                                                )
                                            }
                                            scope.launch {
                                                dragOffsetY.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = spring(
                                                        dampingRatio = 0.76f,
                                                        stiffness = Spring.StiffnessMediumLow
                                                    )
                                                )
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        isDragging = false
                                        scope.launch { dragOffsetX.snapTo(0f); dragOffsetY.snapTo(0f) }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        scope.launch {
                                            dragOffsetX.snapTo(dragOffsetX.value + dragAmount.x)
                                            dragOffsetY.snapTo(dragOffsetY.value + dragAmount.y * 0.35f)
                                        }
                                    }
                                )
                            }
                    ) {
                        DeckItemCard(
                            item = topItem,
                            isTopCard = true,
                            isReadOnly = isReadOnly,
                            onReadOnlyAttempt = onReadOnlyAttempt,
                            onClick = { onClick(topItem) },
                            onStartFocus = {
                                if (isReadOnly) {
                                    onReadOnlyAttempt()
                                } else {
                                    onStartFocus(topItem)
                                }
                            },
                            onComplete = {
                                if (isReadOnly) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onReadOnlyAttempt()
                                } else {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    scope.launch {
                                        dragOffsetX.animateTo(
                                            targetValue = screenWidthPx * 1.2f,
                                            animationSpec = tween(180)
                                        )
                                        historyStack.add(topItem)
                                        deckItems = deckItems.drop(1)
                                        onToggle(topItem)
                                        dragOffsetX.snapTo(0f)
                                        dragOffsetY.snapTo(0f)
                                    }
                                }
                            },
                            onSkip = {
                                if (isReadOnly) {
                                    onReadOnlyAttempt()
                                } else {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    scope.launch {
                                        dragOffsetX.animateTo(
                                            targetValue = -screenWidthPx * 1.2f,
                                            animationSpec = tween(180)
                                        )
                                        historyStack.add(topItem)
                                        deckItems = deckItems.drop(1) + topItem
                                        onSkip(topItem)
                                        dragOffsetX.snapTo(0f)
                                        dragOffsetY.snapTo(0f)
                                    }
                                }
                            },
                            dragProgress = dragOffsetX.value / swipeThresholdPx
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Gesture Guidance Footer
        if (isReadOnly) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Past day is locked to preserve honest progress",
                    style = FormaTheme.typography.bodySmall,
                    color = colors.textTertiary,
                    fontSize = 11.5.sp
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = null,
                        tint = colors.textTertiary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Swipe Left to Skip",
                        style = FormaTheme.typography.bodySmall,
                        color = colors.textTertiary,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Swipe Right to Complete",
                        style = FormaTheme.typography.bodySmall,
                        color = colors.textTertiary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

/**
 * Visual surface of each stacked ritual card.
 */
@Composable
private fun DeckItemCard(
    item: TodayScheduleItem,
    modifier: Modifier = Modifier,
    isTopCard: Boolean = false,
    isReadOnly: Boolean = false,
    onReadOnlyAttempt: () -> Unit = {},
    onClick: () -> Unit = {},
    onStartFocus: () -> Unit = {},
    onComplete: () -> Unit = {},
    onSkip: () -> Unit = {},
    dragProgress: Float = 0f
) {
    val colors = FormaTheme.colors

    val title: String
    val iconKey: String
    val subtitle: String
    val cueText: String?
    val colorTagHex: String?
    val totalSubtasks: Int
    val doneSubtasks: Int

    when (item) {
        is TodayScheduleItem.HabitItem -> {
            val habit = item.habit
            title = habit.name
            iconKey = habit.icon
            cueText = habit.stackedCueText
            colorTagHex = habit.colorTag
            totalSubtasks = habit.subtasks.size
            doneSubtasks = habit.subtasks.count { it.completed }
            subtitle = habit.reminderTimeMinutes?.let { minutes ->
                val h = minutes / 60
                val m = minutes % 60
                val amPm = if (h < 12) "AM" else "PM"
                val h12 = if (h % 12 == 0) 12 else h % 12
                String.format("%d:%02d %s • %s", h12, m, amPm, habit.timeOfDay.displayName)
            } ?: habit.timeOfDay.displayName
        }
        is TodayScheduleItem.TimelineBlock -> {
            val task = item.item
            title = task.title
            iconKey = task.icon
            cueText = null
            colorTagHex = task.colorTag
            totalSubtasks = task.subtasks.size
            doneSubtasks = task.subtasks.count { it.completed }
            subtitle = task.startTime ?: "Anytime today"
        }
    }

    val habitAccent = remember(colorTagHex) {
        try {
            if (!colorTagHex.isNullOrBlank()) {
                Color(android.graphics.Color.parseColor(colorTagHex))
            } else null
        } catch (_: Exception) {
            null
        }
    } ?: colors.accent

    // Swipe direction dynamic overlays
    val isSwipingRight = dragProgress > 0.15f
    val isSwipingLeft = dragProgress < -0.15f
    val swipeAlpha = (abs(dragProgress) * 0.8f).coerceIn(0f, 0.45f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(350.dp)
            .shadow(
                elevation = if (isTopCard) 12.dp else 4.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color.Black.copy(alpha = 0.10f),
                spotColor = Color.Black.copy(alpha = 0.14f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(colors.surface)
            .border(
                width = if (isTopCard) 1.5.dp else 1.dp,
                color = when {
                    isSwipingRight -> colors.accent.copy(alpha = 0.8f)
                    isSwipingLeft -> colors.textTertiary.copy(alpha = 0.6f)
                    isTopCard -> habitAccent.copy(alpha = 0.45f)
                    else -> colors.border.copy(alpha = 0.5f)
                },
                shape = RoundedCornerShape(26.dp)
            )
            .padding(20.dp)
    ) {
        // Drag directional indicator banner behind content
        if (isSwipingRight) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.accent.copy(alpha = swipeAlpha)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    modifier = Modifier.padding(end = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COMPLETE",
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.accent,
                        letterSpacing = 1.6.sp,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        } else if (isSwipingLeft) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.surfaceVariant.copy(alpha = swipeAlpha)),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.padding(start = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SKIP / LATER",
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textSecondary,
                        letterSpacing = 1.6.sp,
                        fontSize = 18.sp
                    )
                }
            }
        }

        // Main Card Inner Content
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Section 1: Header Category pill & Focus sprint button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(habitAccent.copy(alpha = 0.14f))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = subtitle.uppercase(),
                        style = FormaTheme.typography.labelSmall.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            fontFeatureSettings = "tnum"
                        ),
                        fontWeight = FontWeight.Bold,
                        color = habitAccent,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                }

                if (isTopCard) {
                    if (isReadOnly) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surfaceVariant.copy(alpha = 0.7f))
                                .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .clickable { onReadOnlyAttempt() }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = "Locked",
                                    tint = colors.textTertiary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (item.isCompleted) "COMPLETED" else "PAST RECORD",
                                    style = FormaTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isCompleted) habitAccent else colors.textTertiary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(habitAccent.copy(alpha = 0.12f))
                                .border(1.dp, habitAccent.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .formaPressEffect(targetScale = 0.92f) { onStartFocus() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Timer,
                                    contentDescription = "Focus",
                                    tint = habitAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Focus Sprint",
                                    style = FormaTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = habitAccent,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Big Focal Icon & Habit Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isTopCard) { onClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(habitAccent.copy(alpha = 0.15f))
                        .border(1.dp, habitAccent.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    FormaIcon(
                        iconKey = iconKey,
                        contentDescription = title,
                        tint = habitAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = title,
                    style = FormaTheme.typography.headlineMedium.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        letterSpacing = (-0.6).sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontSize = 22.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!cueText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Link,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Anchor: $cueText",
                            style = FormaTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Micro-steps progress track
                if (totalSubtasks > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val progress = doneSubtasks.toFloat() / totalSubtasks.toFloat()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .height(5.dp)
                                    .clip(CircleShape)
                                    .background(habitAccent)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$doneSubtasks of $totalSubtasks steps",
                            style = FormaTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                            fontWeight = FontWeight.Bold,
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Section 3: Bottom 1-Tap Manual Controls
            if (isTopCard) {
                if (isReadOnly) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surfaceVariant)
                            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable { onReadOnlyAttempt() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = colors.textTertiary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (item.isCompleted) "Completed on this day · Locked" else "Past Day Record · Locked",
                                style = FormaTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isCompleted) habitAccent else colors.textTertiary,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    // Skip button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surfaceVariant)
                            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .formaPressEffect(targetScale = 0.95f) { onSkip() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = "Skip",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Skip / Later",
                                style = FormaTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textSecondary,
                                fontSize = 12.5.sp
                            )
                        }
                    }

                    // Complete button
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.accent)
                            .formaPressEffect(targetScale = 0.95f) { onComplete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Complete",
                                tint = colors.onAccent,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Complete Ritual",
                                style = FormaTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onAccent,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
}

/**
 * Calming Zen Completion card displayed when the deck is fully cleared.
 */
@Composable
private fun ZenDeckCompletedCard(
    totalItemsCount: Int,
    onSwitchToListMode: () -> Unit,
    onResetDeck: () -> Unit
) {
    val colors = FormaTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(colors.surface)
            .border(1.5.dp, colors.accent.copy(alpha = 0.45f), RoundedCornerShape(26.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(colors.accentSoft)
                    .border(1.5.dp, colors.accent.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Spa,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Sanctuary in Flow",
                style = FormaTheme.typography.headlineMedium.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    letterSpacing = (-0.5).sp
                ),
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "All rituals for this time window have been checked or preserved. Rest freely in the present moment.",
                style = FormaTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceVariant)
                        .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .formaPressEffect(targetScale = 0.94f) { onResetDeck() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Review All",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Review Deck",
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.accent)
                        .formaPressEffect(targetScale = 0.94f) { onSwitchToListMode() }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.FormatListBulleted,
                            contentDescription = "List View",
                            tint = colors.onAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "View Timeline List",
                            fontWeight = FontWeight.Bold,
                            color = colors.onAccent,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

private val TodayScheduleItem.isSkipped: Boolean
    get() = (this as? TodayScheduleItem.HabitItem)?.isSkippedToday == true
