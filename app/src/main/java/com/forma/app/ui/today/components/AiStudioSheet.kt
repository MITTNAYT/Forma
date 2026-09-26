package com.forma.app.ui.today.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.motion.formaPressEffect
import com.forma.app.domain.model.AiGenerationResult
import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.TimelineItem
import com.forma.app.ui.today.TodayViewModel
import kotlinx.coroutines.delay

enum class AiStudioMode(val label: String, val subtitle: String, val icon: ImageVector) {
    DAY_PLAN("Day Agenda", "Timeboxed schedule", Icons.Rounded.Schedule),
    HABIT_ARCHITECT("Habit Architect", "Atomic rituals", Icons.Rounded.Spa),
    GOAL_DECOMPOSE("Goal Breakdown", "Milestone roadmap", Icons.Rounded.Layers)
}

private data class StudioPreset(
    val title: String,
    val description: String,
    val prompt: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiStudioSheet(
    viewModel: TodayViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = FormaTheme.colors
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    val isAiPlanning by viewModel.isAiPlanning.collectAsState()
    val aiPreviewResult by viewModel.aiPreviewResult.collectAsState()

    var activeMode by remember { mutableStateOf(AiStudioMode.DAY_PLAN) }
    var promptText by remember { mutableStateOf("") }
    var isInputFocused by remember { mutableStateOf(false) }

    val addedHabitIds = remember { mutableStateListOf<String>() }
    val selectedTaskIds = remember { mutableStateListOf<String>() }

    // Sync selected tasks when preview arrives
    LaunchedEffect(aiPreviewResult) {
        selectedTaskIds.clear()
        aiPreviewResult?.tasks?.forEach { task ->
            selectedTaskIds.add(task.id)
        }
    }

    // Rotating mindful loading messages during generation
    val loadingMessages = listOf(
        "Consulting Gemini Intelligence...",
        "Harmonizing deep focus with daily energy flow...",
        "Structuring realistic timeboxes & checklists...",
        "Crafting keystone habits and micro-rituals..."
    )
    var currentLoadingMsgIndex by remember { mutableStateOf(0) }
    LaunchedEffect(isAiPlanning) {
        if (isAiPlanning) {
            currentLoadingMsgIndex = 0
            while (true) {
                delay(2000)
                currentLoadingMsgIndex = (currentLoadingMsgIndex + 1) % loadingMessages.size
            }
        }
    }

    // Curated high-value studio presets per mode
    val dayPresets = listOf(
        StudioPreset(
            title = "Deep Work Sprint",
            description = "3 high-cognition timeblocks with structured recovery",
            prompt = "Deep work sprint with 3 focused 90-minute blocks, midday nutrition, and evening reflection",
            icon = Icons.Rounded.Timer
        ),
        StudioPreset(
            title = "Restorative Sunday",
            description = "Gentle alignment, meal prep, reading, and mental reset",
            prompt = "Mindful Sunday reset: morning walk, healthy meal prep, reading session, and week planning",
            icon = Icons.Rounded.Spa
        ),
        StudioPreset(
            title = "Executive Flow",
            description = "High-priority tasks, async communication, and strategic review",
            prompt = "High-impact workday: top 3 strategic priorities, email batching at 2pm, and end-of-day shutdown",
            icon = Icons.Rounded.Bolt
        )
    )

    val habitPresets = listOf(
        StudioPreset(
            title = "Morning Ignition Stack",
            description = "Hydration, direct sunlight, 10m breathwork, and mobility",
            prompt = "Design a powerful 4-part morning ignition habit stack: hydrate, morning sunlight, breathwork, and light mobility",
            icon = Icons.Rounded.WbSunny
        ),
        StudioPreset(
            title = "Deep Sleep Sanctuary",
            description = "Screen shutdown, dim lighting, chamomile tea, and journal",
            prompt = "Build an evening wind-down routine to ensure 8 hours of deep restorative sleep",
            icon = Icons.Rounded.Spa
        ),
        StudioPreset(
            title = "Daily Focus Shield",
            description = "Phone lockdown, clean desk ritual, and 20 pages reading",
            prompt = "Construct habits to eliminate digital distraction and cultivate daily uninterrupted focus",
            icon = Icons.Rounded.Psychology
        )
    )

    val goalPresets = listOf(
        StudioPreset(
            title = "Ship MVP in 30 Days",
            description = "Core features, testing checklist, and launch schedule",
            prompt = "Break down building and launching a minimum viable product into actionable weekly milestones and daily tasks",
            icon = Icons.Rounded.Computer
        ),
        StudioPreset(
            title = "Run First 10K",
            description = "Progressive mileage, active recovery, and mobility work",
            prompt = "Deconstruct training for a 10K run over 8 weeks into progressive distance runs and recovery habits",
            icon = Icons.Rounded.FitnessCenter
        ),
        StudioPreset(
            title = "Read 20 Books a Year",
            description = "Daily 25-minute reading slot with note capture system",
            prompt = "Break down reading 20 books into daily habits, time allocations, and reflection rituals",
            icon = Icons.Rounded.School
        )
    )

    val currentPresets = when (activeMode) {
        AiStudioMode.DAY_PLAN -> dayPresets
        AiStudioMode.HABIT_ARCHITECT -> habitPresets
        AiStudioMode.GOAL_DECOMPOSE -> goalPresets
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.border)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
        ) {
            // ── Editorial Bauhaus Studio Header ────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.accent.copy(alpha = 0.14f))
                            .border(1.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FORMA STUDIO",
                                style = FormaTheme.typography.labelSmall.copy(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    letterSpacing = 1.6.sp
                                ),
                                fontWeight = FontWeight.Bold,
                                color = colors.accent,
                                fontSize = 10.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.surfaceVariant)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "GEMINI",
                                    style = FormaTheme.typography.labelSmall.copy(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    color = colors.textSecondary,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Architectural Routine Synthesizer",
                            style = FormaTheme.typography.titleMedium.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 17.sp
                        )
                    }
                }

                IconButton(
                    onClick = {
                        viewModel.clearAiPreview()
                        onDismiss()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Segmented Mode Switcher ────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceVariant)
                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AiStudioMode.entries.forEach { mode ->
                    val isSelected = activeMode == mode
                    val bg by animateColorAsState(
                        targetValue = if (isSelected) colors.surface else Color.Transparent,
                        label = "mode_tab_bg"
                    )
                    val fg by animateColorAsState(
                        targetValue = if (isSelected) colors.accent else colors.textSecondary,
                        label = "mode_tab_fg"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(bg)
                            .then(
                                if (isSelected) Modifier.border(1.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(11.dp))
                                else Modifier
                            )
                            .formaPressEffect(targetScale = 0.95f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                activeMode = mode
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = mode.icon,
                                contentDescription = null,
                                tint = fg,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mode.label,
                                style = FormaTheme.typography.labelSmall.copy(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                ),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = fg,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Main Scrollable Canvas ─────────────────────────────────
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. One-Tap Curated Blueprints
                item {
                    Column {
                        Text(
                            text = "INSTANT BLUEPRINT TEMPLATES",
                            style = FormaTheme.typography.labelSmall.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                letterSpacing = 1.2.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            currentPresets.forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .width(220.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(colors.surfaceVariant.copy(alpha = 0.7f))
                                        .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                        .formaPressEffect(targetScale = 0.96f) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            promptText = preset.prompt
                                            focusManager.clearFocus()
                                            when (activeMode) {
                                                AiStudioMode.DAY_PLAN -> viewModel.generateAiPlan(prompt = preset.prompt)
                                                AiStudioMode.HABIT_ARCHITECT -> viewModel.generateAiPlan(prompt = preset.prompt)
                                                AiStudioMode.GOAL_DECOMPOSE -> viewModel.decomposeGoalWithAi(preset.prompt)
                                            }
                                        }
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(colors.accent.copy(alpha = 0.12f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = preset.icon,
                                                    contentDescription = null,
                                                    tint = colors.accent,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = preset.title,
                                                style = FormaTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                ),
                                                color = colors.textPrimary,
                                                fontSize = 13.sp,
                                                maxLines = 1
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = preset.description,
                                            style = FormaTheme.typography.bodySmall.copy(
                                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                                lineHeight = 15.sp
                                            ),
                                            color = colors.textSecondary,
                                            fontSize = 11.sp,
                                            maxLines = 2
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "TAP TO DRAFT",
                                                style = FormaTheme.typography.labelSmall.copy(
                                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                                    letterSpacing = 0.8.sp
                                                ),
                                                fontWeight = FontWeight.Bold,
                                                color = colors.accent,
                                                fontSize = 9.5.sp
                                            )
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                                contentDescription = null,
                                                tint = colors.accent,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Studio Drafting Prompt Box with Pixel-Aligned Cursor
                item {
                    Column {
                        Text(
                            text = "CUSTOM STUDIO PROMPT",
                            style = FormaTheme.typography.labelSmall.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                letterSpacing = 1.2.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(colors.surface)
                                .border(
                                    width = if (isInputFocused) 1.5.dp else 1.dp,
                                    color = if (isInputFocused) colors.accent else colors.border.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp),
                                    contentAlignment = Alignment.TopStart
                                ) {
                                    if (promptText.isEmpty()) {
                                        Text(
                                            text = when (activeMode) {
                                                AiStudioMode.DAY_PLAN -> "Describe your day: e.g. Team standup at 10am, finish presentation, workout, read 30m..."
                                                AiStudioMode.HABIT_ARCHITECT -> "Describe your desired lifestyle: e.g. High energy morning, better sleep, stress reduction..."
                                                AiStudioMode.GOAL_DECOMPOSE -> "Enter an ambition: e.g. Launch a side SaaS project in 30 days, run a marathon..."
                                            },
                                            style = FormaTheme.typography.bodyMedium.copy(
                                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                                lineHeight = 18.sp
                                            ),
                                            color = colors.textTertiary,
                                            fontSize = 13.sp
                                        )
                                    }

                                    BasicTextField(
                                        value = promptText,
                                        onValueChange = { promptText = it },
                                        singleLine = false,
                                        cursorBrush = SolidColor(colors.accent),
                                        keyboardOptions = KeyboardOptions(
                                            capitalization = KeyboardCapitalization.Sentences,
                                            autoCorrectEnabled = true,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                focusManager.clearFocus()
                                            }
                                        ),
                                        textStyle = FormaTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.5.sp,
                                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                                            color = colors.textPrimary,
                                            lineHeight = 19.sp
                                        ),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable { isInputFocused = true }
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (promptText.isNotBlank()) {
                                        Text(
                                            text = "Clear",
                                            style = FormaTheme.typography.labelSmall.copy(
                                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                                            ),
                                            color = colors.textTertiary,
                                            modifier = Modifier
                                                .clickable { promptText = "" }
                                                .padding(4.dp),
                                            fontSize = 11.sp
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.width(1.dp))
                                    }

                                    // Tactile Generate Button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colors.accent)
                                            .formaPressEffect(targetScale = 0.94f) {
                                                focusManager.clearFocus()
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                val effectivePrompt = promptText.ifBlank {
                                                    when (activeMode) {
                                                        AiStudioMode.DAY_PLAN -> "High focus balanced day"
                                                        AiStudioMode.HABIT_ARCHITECT -> "Foundational daily energy rituals"
                                                        AiStudioMode.GOAL_DECOMPOSE -> "Master daily discipline"
                                                    }
                                                }
                                                when (activeMode) {
                                                    AiStudioMode.DAY_PLAN -> viewModel.generateAiPlan(prompt = effectivePrompt)
                                                    AiStudioMode.HABIT_ARCHITECT -> viewModel.generateAiPlan(prompt = "Design foundational daily habits and routines for: $effectivePrompt")
                                                    AiStudioMode.GOAL_DECOMPOSE -> viewModel.decomposeGoalWithAi(effectivePrompt)
                                                }
                                            }
                                            .padding(horizontal = 16.dp, vertical = 9.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isAiPlanning) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(14.dp),
                                                    strokeWidth = 2.dp,
                                                    color = colors.onAccent
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Rounded.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = colors.onAccent,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isAiPlanning) "Synthesizing..." else "Synthesize Plan",
                                                style = FormaTheme.typography.labelSmall.copy(
                                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                ),
                                                fontWeight = FontWeight.Bold,
                                                color = colors.onAccent,
                                                fontSize = 11.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Studio Mindful Generation Status
                if (isAiPlanning) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.accent.copy(alpha = 0.10f))
                                .border(1.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = colors.accent
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = loadingMessages[currentLoadingMsgIndex],
                                    style = FormaTheme.typography.bodySmall.copy(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    color = colors.accent,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // 4. Synthesized Blueprint Results
                aiPreviewResult?.let { result ->
                    // Overview Summary Card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(colors.accent.copy(alpha = 0.08f))
                                .border(1.2.dp, colors.accent.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lightbulb,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = result.title.ifBlank { "Architectural Blueprint" },
                                        style = FormaTheme.typography.titleMedium.copy(
                                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = result.summary,
                                    style = FormaTheme.typography.bodySmall.copy(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        lineHeight = 17.sp
                                    ),
                                    color = colors.textSecondary,
                                    fontSize = 12.sp
                                )
                                if (result.tips.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        result.tips.forEach { tip ->
                                            Text(
                                                text = "• $tip",
                                                style = FormaTheme.typography.bodySmall.copy(
                                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                ),
                                                color = colors.accent,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section A: Synthesized Tasks
                    if (result.tasks.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "SYNTHESIZED TIMELINE BLOCKS (${result.tasks.size})",
                                    style = FormaTheme.typography.labelSmall.copy(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        letterSpacing = 1.2.sp
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    fontSize = 10.5.sp
                                )
                                Text(
                                    text = "${selectedTaskIds.size} Selected",
                                    style = FormaTheme.typography.labelSmall.copy(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        items(result.tasks, key = { it.id }) { task ->
                            val isSelected = selectedTaskIds.contains(task.id)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.surface)
                                    .border(
                                        1.dp,
                                        if (isSelected) colors.accent.copy(alpha = 0.7f) else colors.border.copy(alpha = 0.5f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        if (isSelected) selectedTaskIds.remove(task.id)
                                        else selectedTaskIds.add(task.id)
                                    }
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(colors.accent.copy(alpha = 0.12f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = when (task.icon) {
                                                        "computer" -> Icons.Rounded.Computer
                                                        "fitness_center" -> Icons.Rounded.FitnessCenter
                                                        "spa" -> Icons.Rounded.Spa
                                                        "school" -> Icons.Rounded.School
                                                        "timer" -> Icons.Rounded.Timer
                                                        "wb_sunny" -> Icons.Rounded.WbSunny
                                                        "bolt" -> Icons.Rounded.Bolt
                                                        else -> Icons.Rounded.AutoAwesome
                                                    },
                                                    contentDescription = null,
                                                    tint = colors.accent,
                                                    modifier = Modifier.size(17.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = task.title,
                                                    style = FormaTheme.typography.bodyMedium.copy(
                                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                    ),
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.textPrimary,
                                                    fontSize = 13.5.sp
                                                )
                                                if (task.startTime != null && task.endTime != null) {
                                                    Text(
                                                        text = "${task.startTime} – ${task.endTime}",
                                                        style = FormaTheme.typography.labelSmall.copy(
                                                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                        ),
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = colors.accent,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }

                                        // Selection toggle checkbox
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) colors.accent else colors.surfaceVariant)
                                                .border(1.dp, if (isSelected) colors.accent else colors.border, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Check,
                                                    contentDescription = null,
                                                    tint = colors.onAccent,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (task.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = task.notes,
                                            style = FormaTheme.typography.bodySmall.copy(
                                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                                lineHeight = 15.sp
                                            ),
                                            color = colors.textSecondary,
                                            fontSize = 11.5.sp
                                        )
                                    }

                                    if (task.subtasks.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            task.subtasks.forEach { sub ->
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(colors.accent)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = sub.title,
                                                        style = FormaTheme.typography.bodySmall.copy(
                                                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                        ),
                                                        color = colors.textSecondary,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section B: Keystone Habits
                    if (result.suggestedHabits.isNotEmpty()) {
                        item {
                            Text(
                                text = "RECOMMENDED RITUALS (${result.suggestedHabits.size})",
                                style = FormaTheme.typography.labelSmall.copy(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    letterSpacing = 1.2.sp
                                ),
                                fontWeight = FontWeight.Bold,
                                color = colors.accent,
                                fontSize = 10.5.sp
                            )
                        }

                        items(result.suggestedHabits, key = { it.id }) { habit ->
                            val isAdded = addedHabitIds.contains(habit.id)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.surface)
                                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(colors.accent.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Spa,
                                                contentDescription = null,
                                                tint = colors.accent,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = habit.name,
                                                style = FormaTheme.typography.bodyMedium.copy(
                                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                ),
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary,
                                                fontSize = 13.5.sp
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(colors.accent.copy(alpha = 0.12f))
                                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = habit.timeOfDay.name,
                                                        style = FormaTheme.typography.labelSmall.copy(
                                                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                        ),
                                                        color = colors.accent,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "${habit.repeatDays.size} days/wk",
                                                    style = FormaTheme.typography.labelSmall.copy(
                                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                    ),
                                                    color = colors.textTertiary,
                                                    fontSize = 10.5.sp
                                                )
                                            }
                                        }
                                    }

                                    // Add habit button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isAdded) colors.accent else colors.accent.copy(alpha = 0.12f))
                                            .border(1.dp, colors.accent, RoundedCornerShape(10.dp))
                                        .formaPressEffect(targetScale = 0.92f) {
                                            if (!isAdded) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                addedHabitIds.add(habit.id)
                                                viewModel.addSingleHabit(habit)
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isAdded) Icons.Rounded.Check else Icons.Rounded.Add,
                                                contentDescription = null,
                                                tint = if (isAdded) colors.onAccent else colors.accent,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isAdded) "Adopted" else "Adopt",
                                                style = FormaTheme.typography.labelSmall.copy(
                                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                ),
                                                fontWeight = FontWeight.Bold,
                                                color = if (isAdded) colors.onAccent else colors.accent,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Primary Bottom Action: Apply Blueprint ─────────────────
            aiPreviewResult?.let { result ->
                val selectedTasks = result.tasks.filter { selectedTaskIds.contains(it.id) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.accent)
                        .formaPressEffect(targetScale = 0.96f) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.applyAiPlanBatch(
                                tasks = selectedTasks,
                                habits = emptyList(), // Habits are added via individual taps
                                onComplete = {
                                    onDismiss()
                                }
                            )
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Commit ${selectedTasks.size} Blocks to Timeline",
                        style = FormaTheme.typography.titleMedium.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            letterSpacing = 0.4.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = colors.onAccent,
                        fontSize = 14.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
