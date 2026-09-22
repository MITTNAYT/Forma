package com.forma.app.ui.mindfulness

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
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
import com.forma.app.core.audio.ZenSoundscapeEngine
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.audio.ZenFeedbackManager
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.motion.formaPressEffect
import kotlinx.coroutines.delay

enum class BreathingTechnique(
    val title: String,
    val subtitle: String,
    val inhaleSec: Int,
    val holdInSec: Int,
    val exhaleSec: Int,
    val holdOutSec: Int
) {
    BOX_BREATHING(
        title = "Box Breathing",
        subtitle = "4-4-4-4 • Focus & Clarity",
        inhaleSec = 4,
        holdInSec = 4,
        exhaleSec = 4,
        holdOutSec = 4
    ),
    RELAXING_478(
        title = "4-7-8 Calm",
        subtitle = "4-7-8 • Deep Relaxation",
        inhaleSec = 4,
        holdInSec = 7,
        exhaleSec = 8,
        holdOutSec = 0
    )
}

enum class BreathPhase(val instruction: String) {
    INHALE("Inhale slowly..."),
    HOLD_IN("Hold your breath..."),
    EXHALE("Exhale gently..."),
    HOLD_OUT("Rest and be still...")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingExerciseSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val localContext = LocalContext.current
    val colors = FormaTheme.colors

    var selectedTechnique by remember { mutableStateOf(BreathingTechnique.BOX_BREATHING) }
    var isRunning by remember { mutableStateOf(true) }
    var currentPhase by remember { mutableStateOf(BreathPhase.INHALE) }
    var secondsInPhase by remember { mutableIntStateOf(0) }
    var completedCycles by remember { mutableIntStateOf(0) }

    val soundEngine = remember { ZenSoundscapeEngine() }

    DisposableEffect(Unit) {
        onDispose {
            soundEngine.stopSoundscape()
        }
    }

    // Rhythms controller
    LaunchedEffect(isRunning, selectedTechnique) {
        if (!isRunning) return@LaunchedEffect
        currentPhase = BreathPhase.INHALE
        secondsInPhase = 0

        while (isRunning) {
            val phaseDuration = when (currentPhase) {
                BreathPhase.INHALE -> selectedTechnique.inhaleSec
                BreathPhase.HOLD_IN -> selectedTechnique.holdInSec
                BreathPhase.EXHALE -> selectedTechnique.exhaleSec
                BreathPhase.HOLD_OUT -> selectedTechnique.holdOutSec
            }

            for (sec in 1..phaseDuration) {
                secondsInPhase = sec
                delay(1000)
                if (!isRunning) break
            }

            if (!isRunning) break

            // Phase transition
            ZenFeedbackManager.triggerTactileTick(localContext)
            soundEngine.playSingingBowlChime(2.0f)

            when (currentPhase) {
                BreathPhase.INHALE -> {
                    if (selectedTechnique.holdInSec > 0) currentPhase = BreathPhase.HOLD_IN
                    else currentPhase = BreathPhase.EXHALE
                }
                BreathPhase.HOLD_IN -> {
                    currentPhase = BreathPhase.EXHALE
                }
                BreathPhase.EXHALE -> {
                    if (selectedTechnique.holdOutSec > 0) {
                        currentPhase = BreathPhase.HOLD_OUT
                    } else {
                        currentPhase = BreathPhase.INHALE
                        completedCycles++
                    }
                }
                BreathPhase.HOLD_OUT -> {
                    currentPhase = BreathPhase.INHALE
                    completedCycles++
                }
            }
            secondsInPhase = 0
        }
    }

    // Animation scale for the Ensō breathing ring
    val targetScale = when (currentPhase) {
        BreathPhase.INHALE -> 1.25f
        BreathPhase.HOLD_IN -> 1.25f
        BreathPhase.EXHALE -> 0.85f
        BreathPhase.HOLD_OUT -> 0.85f
    }

    val phaseDurationMs = when (currentPhase) {
        BreathPhase.INHALE -> selectedTechnique.inhaleSec * 1000
        BreathPhase.HOLD_IN -> selectedTechnique.holdInSec * 1000
        BreathPhase.EXHALE -> selectedTechnique.exhaleSec * 1000
        BreathPhase.HOLD_OUT -> selectedTechnique.holdOutSec * 1000
    }.coerceAtLeast(500)

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(
            durationMillis = phaseDurationMs,
            easing = FastOutSlowInEasing
        ),
        label = "breath_scale"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                            .clip(CircleShape)
                            .background(colors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Spa,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "MINDFUL BREATHING",
                            style = FormaTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textTertiary,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.sp
                        )
                        Text(
                            text = selectedTechnique.title,
                            style = FormaTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 16.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Technique Selector Tabs (Oat & Matcha)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BreathingTechnique.entries.forEach { tech ->
                    val isSel = selectedTechnique == tech
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) colors.accentSoft else Color.Transparent)
                            .clickable {
                                selectedTechnique = tech
                                isRunning = true
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = tech.title,
                                style = FormaTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) colors.accent else colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Animated Ensō Breathing Orb
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Soft concentric halo (warm tea / sage)
                Canvas(
                    modifier = Modifier
                        .size(210.dp)
                        .scale(animatedScale)
                ) {
                    drawCircle(
                        color = Color(0xFFEDF3EB), // Pale Jade / Oat tint
                        radius = size.minDimension / 2
                    )
                    drawCircle(
                        color = Color(0xFFA0B39A).copy(alpha = 0.4f), // Soft sage outline
                        radius = size.minDimension / 2,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFF4E6542).copy(alpha = 0.15f), // Matcha inner ring
                        radius = size.minDimension / 2.8f
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentPhase.instruction,
                        style = FormaTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2B3327), // Warm deep slate
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$secondsInPhase s",
                        style = FormaTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF637852),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Cycles Completed Counter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Cycles completed: ",
                    style = FormaTheme.typography.bodySmall,
                    color = colors.textTertiary,
                    fontSize = 12.sp
                )
                Text(
                    text = "$completedCycles",
                    style = FormaTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Controls: Play/Pause, Reset, Finish
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        isRunning = false
                        completedCycles = 0
                        currentPhase = BreathPhase.INHALE
                        secondsInPhase = 0
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Reset",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(colors.accent)
                        .formaPressEffect(targetScale = 0.90f) {
                            isRunning = !isRunning
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        tint = colors.onAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.accentSoft)
                        .clickable {
                            ZenFeedbackManager.playTibetanBowl(localContext)
                            onDismiss()
                        }
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Done",
                        style = FormaTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
