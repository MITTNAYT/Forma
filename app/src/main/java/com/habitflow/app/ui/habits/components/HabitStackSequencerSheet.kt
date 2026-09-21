package com.habitflow.app.ui.habits.components

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.audio.ZenSoundscapeEngine
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.motion.formaPressEffect
import com.habitflow.app.domain.model.Habit
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitStackSequencerSheet(
    habitStack: List<Habit>,
    onCompleteHabit: (String) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val colors = NotionTheme.colors
    val soundEngine = remember { ZenSoundscapeEngine() }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    var secondsRemaining by remember { mutableIntStateOf(60) } // Default 1 min step or 5 min
    val stepDurationSeconds = 60 // 1 minute per step demo/focus pacing

    val currentHabit = habitStack.getOrNull(currentStepIndex)
    val isAllCompleted = currentStepIndex >= habitStack.size

    DisposableEffect(Unit) {
        onDispose {
            soundEngine.stopSoundscape()
        }
    }

    LaunchedEffect(currentStepIndex, isPlaying) {
        if (!isPlaying || isAllCompleted) return@LaunchedEffect
        secondsRemaining = stepDurationSeconds

        while (isPlaying && secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
            if (secondsRemaining <= 0) {
                // Step complete!
                currentHabit?.let { onCompleteHabit(it.id) }
                soundEngine.playSingingBowlChime(3.0f)
                if (currentStepIndex + 1 < habitStack.size) {
                    currentStepIndex++
                } else {
                    currentStepIndex++
                    isPlaying = false
                }
            }
        }
    }

    val progress = if (stepDurationSeconds > 0) {
        (secondsRemaining.toFloat() / stepDurationSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "stack_progress"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HABIT STACK SEQUENCER",
                        style = NotionTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Executing cue-chained rituals",
                        style = NotionTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!isAllCompleted && currentHabit != null) {
                // Central Active Habit Timer Ring
                Box(
                    modifier = Modifier.size(170.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(170.dp),
                        color = colors.surfaceVariant,
                        strokeWidth = 10.dp
                    )
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(170.dp),
                        color = colors.accent,
                        strokeWidth = 10.dp,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val mins = secondsRemaining / 60
                        val secs = secondsRemaining % 60
                        Text(
                            text = String.format("%02d:%02d", mins, secs),
                            style = NotionTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 32.sp
                        )
                        Text(
                            text = "Step ${currentStepIndex + 1} of ${habitStack.size}",
                            style = NotionTheme.typography.labelSmall,
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentHabit.name,
                    style = NotionTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                if (!currentHabit.stackedCueText.isNullOrBlank()) {
                    Text(
                        text = currentHabit.stackedCueText,
                        style = NotionTheme.typography.bodySmall,
                        color = colors.accent,
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Play / Pause & Skip Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                            .clickable { isPlaying = !isPlaying },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = colors.onAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant)
                            .border(1.dp, colors.border.copy(alpha = 0.5f), CircleShape)
                        .clickable {
                            onCompleteHabit(currentHabit.id)
                            soundEngine.playSingingBowlChime(2.0f)
                            if (currentStepIndex + 1 < habitStack.size) {
                                currentStepIndex++
                            } else {
                                currentStepIndex++
                                isPlaying = false
                            }
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FastForward,
                            contentDescription = "Next Ritual",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                // All Stack Rituals Completed Celebration
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.accentSoft)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Habit Stack Complete!",
                            style = NotionTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "All chained rituals successfully fulfilled with mindfulness.",
                            style = NotionTheme.typography.bodySmall,
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step-by-Step Chain Preview
            Text(
                text = "CUE CHAIN PLAYLIST",
                style = NotionTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textTertiary,
                letterSpacing = 1.1.sp,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(habitStack) { index, habit ->
                    val isCurrent = index == currentStepIndex
                    val isPast = index < currentStepIndex

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isCurrent) colors.accentSoft else colors.surface)
                            .border(
                                1.dp,
                                if (isCurrent) colors.accent.copy(alpha = 0.5f) else colors.border.copy(alpha = 0.5f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isPast) colors.accent else colors.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPast) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = colors.onAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${index + 1}",
                                            style = NotionTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) colors.accent else colors.textTertiary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = habit.name,
                                        style = NotionTheme.typography.titleSmall,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = colors.textPrimary,
                                        fontSize = 14.sp
                                    )
                                    if (!habit.stackedCueText.isNullOrBlank()) {
                                        Text(
                                            text = habit.stackedCueText,
                                            style = NotionTheme.typography.bodySmall,
                                            color = colors.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Text(
                                text = if (isPast) "Done" else if (isCurrent) "Now" else "Queued",
                                style = NotionTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) colors.accent else colors.textTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
