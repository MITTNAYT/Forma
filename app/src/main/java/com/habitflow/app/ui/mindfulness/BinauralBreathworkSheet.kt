package com.habitflow.app.ui.mindfulness

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.audio.BinauralBeatsEngine
import com.habitflow.app.core.audio.BinauralMode
import com.habitflow.app.core.designsystem.NotionTheme
import kotlinx.coroutines.delay

enum class BinauralBreathPattern(val title: String, val inhale: Int, val hold1: Int, val exhale: Int, val hold2: Int) {
    BOX("Box Breathing (4-4-4-4)", 4, 4, 4, 4),
    PRANAYAMA("4-7-8 Relaxation", 4, 7, 8, 0),
    COHERENCE("Heart Coherence (5.5s)", 5, 0, 5, 0)
}

enum class BinauralBreathPhase {
    INHALE, HOLD_IN, EXHALE, HOLD_OUT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinauralBreathworkSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val colors = NotionTheme.colors
    val haptic = LocalHapticFeedback.current
    val binauralEngine = remember { BinauralBeatsEngine() }

    var isRunning by remember { mutableStateOf(true) }
    var selectedPattern by remember { mutableStateOf(BinauralBreathPattern.BOX) }
    var currentPhase by remember { mutableStateOf(BinauralBreathPhase.INHALE) }
    var phaseSecondsRemaining by remember { mutableIntStateOf(selectedPattern.inhale) }
    var completedCycles by remember { mutableIntStateOf(0) }

    // Binaural Sound State
    var binauralEnabled by remember { mutableStateOf(true) }
    var selectedBinauralMode by remember { mutableStateOf(BinauralMode.ALPHA) }
    var soundVolume by remember { mutableFloatStateOf(0.4f) }

    DisposableEffect(Unit) {
        onDispose {
            binauralEngine.stop()
        }
    }

    LaunchedEffect(binauralEnabled, selectedBinauralMode, isRunning, soundVolume) {
        if (binauralEnabled && isRunning) {
            binauralEngine.start(selectedBinauralMode, soundVolume)
        } else {
            binauralEngine.stop()
        }
    }

    // Breath Timing Loop
    LaunchedEffect(isRunning, selectedPattern) {
        if (!isRunning) return@LaunchedEffect

        while (isRunning) {
            // Phase 1: Inhale
            currentPhase = BinauralBreathPhase.INHALE
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            for (sec in selectedPattern.inhale downTo 1) {
                phaseSecondsRemaining = sec
                delay(1000)
            }

            // Phase 2: Hold In
            if (selectedPattern.hold1 > 0 && isRunning) {
                currentPhase = BinauralBreathPhase.HOLD_IN
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                for (sec in selectedPattern.hold1 downTo 1) {
                    phaseSecondsRemaining = sec
                    delay(1000)
                }
            }

            // Phase 3: Exhale
            if (isRunning) {
                currentPhase = BinauralBreathPhase.EXHALE
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                for (sec in selectedPattern.exhale downTo 1) {
                    phaseSecondsRemaining = sec
                    delay(1000)
                }
            }

            // Phase 4: Hold Out
            if (selectedPattern.hold2 > 0 && isRunning) {
                currentPhase = BinauralBreathPhase.HOLD_OUT
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                for (sec in selectedPattern.hold2 downTo 1) {
                    phaseSecondsRemaining = sec
                    delay(1000)
                }
            }

            completedCycles++
        }
    }

    // Orb Scale Animation
    val targetScale = when (currentPhase) {
        BinauralBreathPhase.INHALE -> 1.45f
        BinauralBreathPhase.HOLD_IN -> 1.45f
        BinauralBreathPhase.EXHALE -> 0.85f
        BinauralBreathPhase.HOLD_OUT -> 0.85f
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (isRunning) targetScale else 1.0f,
        animationSpec = tween(
            durationMillis = when (currentPhase) {
                BinauralBreathPhase.INHALE -> selectedPattern.inhale * 1000
                BinauralBreathPhase.EXHALE -> selectedPattern.exhale * 1000
                else -> 400
            },
            easing = FastOutSlowInEasing
        ),
        label = "breath_scale"
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
                .fillMaxHeight(0.92f)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SelfImprovement,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "BINAURAL BREATHWORK",
                            style = NotionTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Dual-Frequency Coherence",
                            style = NotionTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pattern Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BinauralBreathPattern.entries.forEach { pattern ->
                    val isSelected = selectedPattern == pattern
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) colors.accent else colors.surfaceVariant)
                            .clickable {
                                selectedPattern = pattern
                                phaseSecondsRemaining = pattern.inhale
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pattern.name,
                            style = NotionTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) colors.onAccent else colors.textPrimary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Central Animated Breathing Orb with Haptic Aura
            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Glow Aura
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .scale(animatedScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    colors.accent.copy(alpha = 0.35f),
                                    colors.accentMuted.copy(alpha = 0.12f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Main Core Orb
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(animatedScale * 0.95f)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(colors.accent, colors.accentMuted)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (currentPhase) {
                                BinauralBreathPhase.INHALE -> "INHALE"
                                BinauralBreathPhase.HOLD_IN -> "HOLD"
                                BinauralBreathPhase.EXHALE -> "EXHALE"
                                BinauralBreathPhase.HOLD_OUT -> "HOLD"
                            },
                            style = NotionTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "$phaseSecondsRemaining",
                            style = NotionTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 32.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Play / Pause & Stats Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colors.accent)
                        .clickable { isRunning = !isRunning },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = colors.onAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = "$completedCycles breath cycles completed",
                    style = NotionTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Binaural Beats Controller Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Headphones,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Binaural Beats (Wear Headphones)",
                                style = NotionTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 11.5.sp
                            )
                        }

                        Text(
                            text = if (binauralEnabled) "ON" else "OFF",
                            style = NotionTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (binauralEnabled) colors.accent else colors.textTertiary,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (binauralEnabled) colors.accentSoft else colors.surfaceVariant)
                                .clickable { binauralEnabled = !binauralEnabled }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (binauralEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            BinauralMode.entries.forEach { mode ->
                                val isSelected = selectedBinauralMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) colors.accentSoft else colors.surfaceVariant.copy(alpha = 0.5f))
                                        .border(
                                            1.dp,
                                            if (isSelected) colors.accent else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedBinauralMode = mode }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${mode.targetHz} Hz",
                                        style = NotionTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) colors.accent else colors.textSecondary,
                                        fontSize = 10.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
