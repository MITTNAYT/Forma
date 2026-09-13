package com.habitflow.app.ui.pomodoro

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.core.audio.AmbientSound
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.motion.formaPressEffect
import com.habitflow.app.domain.model.TodayScheduleItem

@Composable
fun PomodoroScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: PomodoroViewModel = hiltViewModel()
) {
    val colors = NotionTheme.colors
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val selectedMode by viewModel.selectedMode.collectAsState()
    val selectedSound by viewModel.selectedSound.collectAsState()
    val totalDuration by viewModel.totalDurationSeconds.collectAsState()
    val timeRemaining by viewModel.timeRemainingSeconds.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val secondsElapsed by viewModel.secondsElapsedThisSession.collectAsState()
    val totalFocusToday by viewModel.totalFocusSecondsToday.collectAsState()
    val selectedItem by viewModel.selectedItem.collectAsState()
    val schedule by viewModel.todaySchedule.collectAsState()

    val progress = if (totalDuration > 0) {
        (timeRemaining.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 300f),
        label = "pomodoro_progress"
    )

    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    val totalMinutesToday = (totalFocusToday + secondsElapsed) / 60

    // Ambient breathing pulses & aura rings for deep flow state
    val infiniteTransition = rememberInfiniteTransition(label = "pomodoro_pulse")
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_pulse"
    )
    val auraPulse1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_pulse_1"
    )
    val auraPulse2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_pulse_2"
    )

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is PomodoroUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is PomodoroUiEvent.SessionFinished -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    Toast.makeText(context, "Flow session complete! Mindful time recorded.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        containerColor = colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Editorial Minimalist Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "FOCUS & CADENCE",
                        style = NotionTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textTertiary,
                        letterSpacing = 1.5.sp,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Zen Focus",
                        style = NotionTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 28.sp,
                        letterSpacing = (-0.6).sp
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(18.dp)) }

            // 2. Mode Selector Pill Bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PomodoroMode.entries.forEach { mode ->
                            val isSelected = selectedMode == mode
                            val bg by animateColorAsState(
                                targetValue = if (isSelected) colors.accent else Color.Transparent,
                                animationSpec = tween(durationMillis = 180),
                                label = "mode_bg"
                            )
                            val textColor by animateColorAsState(
                                targetValue = if (isSelected) colors.onAccent else colors.textSecondary,
                                animationSpec = tween(durationMillis = 180),
                                label = "mode_text"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(bg)
                                    .formaPressEffect(targetScale = 0.94f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.selectMode(mode)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.title,
                                    style = NotionTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(18.dp)) }

            // 3. Intention Attachment Carousel (Attach today's task or general focus)
            item {
                val scrollState = rememberScrollState()
                val scheduleItems = schedule?.items ?: emptyList()

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ACTIVE COMMITMENT",
                        style = NotionTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textTertiary,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // General Focus Option
                        val isGeneral = selectedItem == null
                        val generalBg by animateColorAsState(
                            targetValue = if (isGeneral) colors.accentSoft else colors.surface,
                            label = "gen_bg"
                        )
                        val generalBorder = if (isGeneral) colors.accent else colors.border.copy(alpha = 0.5f)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(generalBg)
                                .border(1.dp, generalBorder, RoundedCornerShape(16.dp))
                                .clickable { viewModel.selectScheduleItem(null) }
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Spa,
                                    contentDescription = null,
                                    tint = if (isGeneral) colors.accent else colors.textTertiary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "General Flow",
                                    style = NotionTheme.typography.labelSmall,
                                    fontWeight = if (isGeneral) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isGeneral) colors.accent else colors.textPrimary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Scheduled Today Items
                        scheduleItems.forEach { item ->
                            val itemTitle = when (item) {
                                is TodayScheduleItem.HabitItem -> item.habit.name
                                is TodayScheduleItem.TimelineBlock -> item.item.title
                            }
                            val isChosen = selectedItem?.id == item.id
                            val itemBg by animateColorAsState(
                                targetValue = if (isChosen) colors.accentSoft else colors.surface,
                                label = "item_bg_${item.id}"
                            )
                            val itemBorder = if (isChosen) colors.accent else colors.border.copy(alpha = 0.5f)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(itemBg)
                                    .border(1.dp, itemBorder, RoundedCornerShape(16.dp))
                                    .clickable { viewModel.selectScheduleItem(item) }
                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isChosen) colors.accent else colors.textTertiary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = itemTitle,
                                        style = NotionTheme.typography.labelSmall,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isChosen) colors.accent else colors.textPrimary,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(28.dp)) }

            // 4. Hero Dial Clock Ring with Concentric Breathing Aura
            item {
                androidx.compose.foundation.layout.BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val rawSize = maxWidth * 0.68f
                    val dialSize = if (rawSize < 200.dp) 200.dp else if (rawSize > 250.dp) 250.dp else rawSize
                    val innerRingSize = dialSize - 28.dp
                    val outerDiscSize = dialSize - 10.dp
                    val timeFontSize = if (dialSize < 220.dp) 38.sp else 44.sp

                    Box(
                        modifier = Modifier
                            .size(dialSize * 1.25f),
                        contentAlignment = Alignment.Center
                    ) {
                        // Concentric Breathing Aura Rings (When running in flow state)
                        if (isRunning) {
                            Box(
                                modifier = Modifier
                                    .size(dialSize * 1.22f)
                                    .scale(auraPulse2)
                                    .clip(CircleShape)
                                    .background(colors.accent.copy(alpha = 0.05f))
                            )
                            Box(
                                modifier = Modifier
                                    .size(dialSize * 1.10f)
                                    .scale(auraPulse1)
                                    .clip(CircleShape)
                                    .background(colors.accent.copy(alpha = 0.09f))
                                    .border(1.dp, colors.accent.copy(alpha = 0.15f), CircleShape)
                            )
                        }

                        // Outer Soft Ambient Disc
                        Box(
                            modifier = Modifier
                                .size(outerDiscSize)
                                .scale(if (isRunning) ambientPulse else 1f)
                                .clip(CircleShape)
                                .background(colors.surface)
                                .border(1.dp, colors.border.copy(alpha = 0.6f), CircleShape)
                        )

                        // Track Ring
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(innerRingSize),
                            color = colors.surfaceVariant,
                            strokeWidth = 9.dp
                        )

                        // Active Sweep Arc
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(innerRingSize),
                            color = colors.accent,
                            strokeWidth = 9.dp,
                            strokeCap = StrokeCap.Round
                        )

                        // Central Typography Readout
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = formattedTime,
                                style = NotionTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = timeFontSize,
                                letterSpacing = (-1.2).sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isRunning) colors.accentSoft else colors.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isRunning) "Deep in Flow" else "Paused",
                                    style = NotionTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRunning) colors.accent else colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            // 5. Tactile Floating Control Dock with -5m, Reset, Orb, +5m
            item {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(32.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant)
                            .formaPressEffect(targetScale = 0.88f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.resetTimer()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Reset Clock",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // -5 Min Quick Subtractor
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.surfaceVariant)
                            .formaPressEffect(targetScale = 0.92f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.subtractFiveMinutes()
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "-5m",
                            style = NotionTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 12.sp
                        )
                    }

                    // Main Central Play/Pause Orb with tactile press feedback
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(
                                elevation = if (isRunning) 10.dp else 4.dp,
                                shape = CircleShape,
                                ambientColor = colors.accent.copy(alpha = 0.35f),
                                spotColor = colors.accent.copy(alpha = 0.45f)
                            )
                            .clip(CircleShape)
                            .background(colors.accent)
                            .formaPressEffect(targetScale = 0.90f) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.togglePlayPause()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Start",
                            tint = colors.onAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // +5 Min Quick Adder
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.surfaceVariant)
                            .formaPressEffect(targetScale = 0.92f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.addFiveMinutes()
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.MoreTime,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "+5m",
                                style = NotionTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // 6. Organized Focus Soundscapes Rail
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ORGANIC FOCUS SOUNDSCAPES",
                            style = NotionTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textTertiary,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.sp
                        )

                        if (selectedSound != AmbientSound.OFF && isRunning) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SoundscapeEqualizer(tint = colors.accent)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Synthesizing Live",
                                    style = NotionTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AmbientSound.entries.forEach { sound ->
                            val isSelected = selectedSound == sound
                            val cardBg by animateColorAsState(
                                targetValue = if (isSelected) colors.accentSoft else colors.surface,
                                label = "sound_card_bg_${sound.name}"
                            )
                            val cardBorder = if (isSelected) colors.accent else colors.border.copy(alpha = 0.5f)
                            val icon = getSoundscapeIcon(sound)

                            Box(
                                modifier = Modifier
                                    .width(170.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(cardBg)
                                    .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                                    .formaPressEffect(targetScale = 0.95f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setAmbientSound(sound)
                                    }
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) colors.accent else colors.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = sound.displayName,
                                                tint = if (isSelected) colors.onAccent else colors.textPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        if (isSelected && isRunning && sound != AmbientSound.OFF) {
                                            SoundscapeEqualizer(tint = colors.accent)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = sound.displayName,
                                        style = NotionTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) colors.accent else colors.textPrimary,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = sound.description,
                                        style = NotionTheme.typography.bodySmall,
                                        color = colors.textSecondary,
                                        fontSize = 10.5.sp,
                                        lineHeight = 14.sp,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // 7. Daily Telemetry Status Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.accentSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Timer,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "$totalMinutesToday mins focused today",
                                    style = NotionTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Accumulated across all rituals",
                                    style = NotionTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (secondsElapsed > 30) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.accent)
                                    .clickable { viewModel.finishAndLog() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Save",
                                    style = NotionTheme.typography.labelSmall,
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
    }
}

@Composable
private fun SoundscapeEqualizer(tint: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "sound_eq")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq_h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq_h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq_h3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(16.dp)
    ) {
        Box(modifier = Modifier.width(2.5.dp).height(h1.dp).clip(CircleShape).background(tint))
        Box(modifier = Modifier.width(2.5.dp).height(h2.dp).clip(CircleShape).background(tint))
        Box(modifier = Modifier.width(2.5.dp).height(h3.dp).clip(CircleShape).background(tint))
    }
}

private fun getSoundscapeIcon(sound: AmbientSound): ImageVector {
    return when (sound) {
        AmbientSound.OFF -> Icons.Rounded.VolumeOff
        AmbientSound.RAIN -> Icons.Rounded.WaterDrop
        AmbientSound.KYOTO_BELL -> Icons.Rounded.NotificationsActive
        AmbientSound.ZEN_DRONE -> Icons.Rounded.Spa
        AmbientSound.FOREST_STREAM -> Icons.Rounded.Waves
        AmbientSound.BROWN_NOISE -> Icons.Rounded.Air
        AmbientSound.WHITE_NOISE -> Icons.Rounded.GraphicEq
    }
}

