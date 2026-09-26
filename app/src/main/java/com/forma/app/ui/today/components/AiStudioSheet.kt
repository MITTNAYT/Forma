package com.forma.app.ui.today.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.forma.app.ui.today.TodayViewModel
import kotlinx.coroutines.delay

enum class StudioWorkspace(val label: String, val icon: ImageVector) {
    PROMPT("Studio Prompt", Icons.Rounded.Tune),
    BLUEPRINTS("Blueprints Library", Icons.Rounded.Layers),
    CANVAS("Draft Canvas", Icons.Rounded.AutoAwesome)
}

enum class AiStudioMode(val label: String, val subtitle: String, val icon: ImageVector) {
    DAY_PLAN("Day Agenda", "Timeboxed schedule", Icons.Rounded.Schedule),
    HABIT_ARCHITECT("Habit Architect", "Atomic rituals", Icons.Rounded.Spa),
    GOAL_DECOMPOSE("Goal Breakdown", "Milestone roadmap", Icons.Rounded.Layers)
}

private data class StudioPreset(
    val title: String,
    val category: String,
    val description: String,
    val prompt: String,
    val mode: AiStudioMode,
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

    var currentWorkspace by remember {
        mutableStateOf(if (aiPreviewResult != null) StudioWorkspace.CANVAS else StudioWorkspace.PROMPT)
    }
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
        if (aiPreviewResult != null) {
            currentWorkspace = StudioWorkspace.CANVAS
        }
    }

    // Auto navigate to canvas when planning begins
    LaunchedEffect(isAiPlanning) {
        if (isAiPlanning) {
            currentWorkspace = StudioWorkspace.CANVAS
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

    // 9 Curated studio blueprints
    val allBlueprints = listOf(
        StudioPreset(
            title = "Deep Work Sprint",
            category = "Day Agenda",
            description = "3 focused 90-minute cognition blocks with structured physical recovery.",
            prompt = "Deep work sprint with 3 focused 90-minute blocks, midday nutrition, and evening reflection",
            mode = AiStudioMode.DAY_PLAN,
            icon = Icons.Rounded.Timer
        ),
        StudioPreset(
            title = "Restorative Sunday",
            category = "Day Agenda",
            description = "Gentle alignment, offline reading, whole meal prep, and nervous system reset.",
            prompt = "Mindful Sunday reset: morning walk, healthy meal prep, reading session, and week planning",
            mode = AiStudioMode.DAY_PLAN,
            icon = Icons.Rounded.Spa
        ),
        StudioPreset(
            title = "Executive Flow",
            category = "Day Agenda",
            description = "Top 3 strategic priorities, email batching at 2pm, and clean shutdown.",
            prompt = "High-impact workday: top 3 strategic priorities, email batching at 2pm, and end-of-day shutdown",
            mode = AiStudioMode.DAY_PLAN,
            icon = Icons.Rounded.Bolt
        ),
        StudioPreset(
            title = "Morning Ignition Stack",
            category = "Keystone Rituals",
            description = "500ml hydration, direct morning sunlight, 10m breathwork, and mobility stretch.",
            prompt = "Design a powerful 4-part morning ignition habit stack: hydrate, morning sunlight, breathwork, and light mobility",
            mode = AiStudioMode.HABIT_ARCHITECT,
            icon = Icons.Rounded.WbSunny
        ),
        StudioPreset(
            title = "Deep Sleep Sanctuary",
            category = "Keystone Rituals",
            description = "Digital shutdown at 9pm, ambient dimming, herbal tea, and reflection log.",
            prompt = "Build an evening wind-down routine to ensure 8 hours of deep restorative sleep",
            mode = AiStudioMode.HABIT_ARCHITECT,
            icon = Icons.Rounded.Spa
        ),
        StudioPreset(
            title = "Daily Focus Shield",
            category = "Keystone Rituals",
            description = "Park devices in another room, 2-minute desk reset, and 20 pages reading.",
            prompt = "Construct habits to eliminate digital distraction and cultivate daily uninterrupted focus",
            mode = AiStudioMode.HABIT_ARCHITECT,
            icon = Icons.Rounded.Psychology
        ),
        StudioPreset(
            title = "Ship MVP in 30 Days",
            category = "Milestone Roadmaps",
            description = "Deconstruct architecture, core feature sprints, testing, and launch timeline.",
            prompt = "Break down building and launching a minimum viable product into actionable weekly milestones and daily tasks",
            mode = AiStudioMode.GOAL_DECOMPOSE,
            icon = Icons.Rounded.Computer
        ),
        StudioPreset(
            title = "Run First 10K",
            category = "Milestone Roadmaps",
            description = "8-week progressive mileage builder, mobility days, and recovery rituals.",
            prompt = "Deconstruct training for a 10K run over 8 weeks into progressive distance runs and recovery habits",
            mode = AiStudioMode.GOAL_DECOMPOSE,
            icon = Icons.Rounded.FitnessCenter
        ),
        StudioPreset(
            title = "Read 20 Books a Year",
            category = "Milestone Roadmaps",
            description = "Daily 25-minute quiet reading slot with note capture system.",
            prompt = "Break down reading 20 books into daily habits, time allocations, and reflection rituals",
            mode = AiStudioMode.GOAL_DECOMPOSE,
            icon = Icons.Rounded.School
        )
    )

    val quickInspirationTokens = listOf(
        "+ 90m Deep Focus Block",
        "+ Morning Sunlight & Walk",
        "+ 2L Daily Hydration",
        "+ 15m Mobility Stretch",
        "+ Screen Sunset 45m",
        "+ Evening Gratitude Reset"
    )

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
            // ── 1. Editorial Header ────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(colors.accent.copy(alpha = 0.14f))
                            .border(1.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
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
                                    text = "GEMINI 2.0",
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
                            fontSize = 16.sp
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

            // ── 2. Top Workspace Free Navigation Selector ───────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceVariant.copy(alpha = 0.6f))
                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StudioWorkspace.entries.forEach { workspace ->
                        val isSelected = currentWorkspace == workspace
                        val bg by animateColorAsState(
                            targetValue = if (isSelected) colors.surface else Color.Transparent,
                            label = "ws_bg_${workspace.name}"
                        )
                        val fg by animateColorAsState(
                            targetValue = if (isSelected) colors.accent else colors.textSecondary,
                            label = "ws_fg_${workspace.name}"
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
                                    currentWorkspace = workspace
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = workspace.icon,
                                    contentDescription = null,
                                    tint = fg,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = workspace.label,
                                    style = FormaTheme.typography.labelSmall.copy(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = fg,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                                if (workspace == StudioWorkspace.CANVAS && aiPreviewResult != null) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(colors.accent)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── 3. Workspace Body Content ──────────────────────────────
            when (currentWorkspace) {
                // ── Workspace A: Studio Prompt ─────────────────────────
                StudioWorkspace.PROMPT -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Mode Switcher
                        item {
                            Column {
                                Text(
                                    text = "SELECT STUDIO MODE",
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
                                            label = "mode_tab_bg_${mode.name}"
                                        )
                                        val fg by animateColorAsState(
                                            targetValue = if (isSelected) colors.accent else colors.textSecondary,
                                            label = "mode_tab_fg_${mode.name}"
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
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
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
                            }
                        }

                        // Architectural Prompt Input Box
                        item {
                            Column {
                                Text(
                                    text = "ARCHITECTURAL INTENTION PROMPT",
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
                                                .height(84.dp),
                                            contentAlignment = Alignment.TopStart
                                        ) {
                                            if (promptText.isEmpty()) {
                                                Text(
                                                    text = when (activeMode) {
                                                        AiStudioMode.DAY_PLAN -> "Describe your day: e.g. Team standup at 10am, finish presentation, workout, read 30m..."
                                                        AiStudioMode.HABIT_ARCHITECT -> "Describe your desired lifestyle: e.g. High energy morning, better sleep, stress reduction..."
                                                        AiStudioMode.GOAL_DECOMPOSE -> "Enter an ambition: e.g. Launch a side project in 30 days, run a 10K, read 20 books..."
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
                                                    onDone = { focusManager.clearFocus() }
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

                                        if (promptText.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                Text(
                                                    text = "Clear Text",
                                                    style = FormaTheme.typography.labelSmall.copy(
                                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                    ),
                                                    color = colors.textTertiary,
                                                    modifier = Modifier
                                                        .clickable { promptText = "" }
                                                        .padding(4.dp),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Quick Inspiration Tokens
                        item {
                            Column {
                                Text(
                                    text = "FAST INSPIRATION TOKENS",
                                    style = FormaTheme.typography.labelSmall.copy(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        letterSpacing = 1.2.sp
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    quickInspirationTokens.forEach { token ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(colors.surfaceVariant.copy(alpha = 0.7f))
                                                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                                .formaPressEffect(targetScale = 0.94f) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    promptText = if (promptText.isBlank()) token.removePrefix("+ ")
                                                    else "$promptText, ${token.removePrefix("+ ")}"
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = token,
                                                style = FormaTheme.typography.labelSmall.copy(
                                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                ),
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.textPrimary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Primary Synthesize Button
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.accent)
                                    .formaPressEffect(targetScale = 0.97f) {
                                        focusManager.clearFocus()
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        val effectivePrompt = promptText.ifBlank {
                                            when (activeMode) {
                                                AiStudioMode.DAY_PLAN -> "High focus balanced day"
                                                AiStudioMode.HABIT_ARCHITECT -> "Foundational daily energy rituals"
                                                AiStudioMode.GOAL_DECOMPOSE -> "Master daily discipline"
                                            }
                                        }
                                        currentWorkspace = StudioWorkspace.CANVAS
                                        when (activeMode) {
                                            AiStudioMode.DAY_PLAN -> viewModel.generateAiPlan(prompt = effectivePrompt)
                                            AiStudioMode.HABIT_ARCHITECT -> viewModel.generateAiPlan(prompt = "Design foundational daily habits and routines for: $effectivePrompt")
                                            AiStudioMode.GOAL_DECOMPOSE -> viewModel.decomposeGoalWithAi(effectivePrompt)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = colors.onAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Synthesize Architectural Blueprint",
                                        style = FormaTheme.typography.titleMedium.copy(
                                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onAccent,
                                        fontSize = 14.5.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // ── Workspace B: Blueprints Library ─────────────────────
                StudioWorkspace.BLUEPRINTS -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "TAP ANY BLUEPRINT TO SYNTHESIZE",
                                style = FormaTheme.typography.labelSmall.copy(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    letterSpacing = 1.2.sp
                                ),
                                fontWeight = FontWeight.Bold,
                                color = colors.accent,
                                fontSize = 10.sp
                            )
                        }

                        items(allBlueprints, key = { it.title }) { preset ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.surface)
                                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .formaPressEffect(targetScale = 0.98f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        promptText = preset.prompt
                                        activeMode = preset.mode
                                        currentWorkspace = StudioWorkspace.CANVAS
                                        when (preset.mode) {
                                            AiStudioMode.DAY_PLAN -> viewModel.generateAiPlan(prompt = preset.prompt)
                                            AiStudioMode.HABIT_ARCHITECT -> viewModel.generateAiPlan(prompt = preset.prompt)
                                            AiStudioMode.GOAL_DECOMPOSE -> viewModel.decomposeGoalWithAi(preset.prompt)
                                        }
                                    }
                                    .padding(15.dp)
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
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(colors.accent.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = preset.icon,
                                                contentDescription = null,
                                                tint = colors.accent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = preset.title,
                                                    style = FormaTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                    ),
                                                    color = colors.textPrimary,
                                                    fontSize = 14.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(colors.surfaceVariant)
                                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = preset.category,
                                                        style = FormaTheme.typography.labelSmall.copy(
                                                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                        ),
                                                        color = colors.textSecondary,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = preset.description,
                                                style = FormaTheme.typography.bodySmall.copy(
                                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                                    lineHeight = 16.sp
                                                ),
                                                color = colors.textSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(colors.accentSoft)
                                            .border(1.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 9.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Draft",
                                                style = FormaTheme.typography.labelSmall.copy(
                                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                ),
                                                fontWeight = FontWeight.Bold,
                                                color = colors.accent,
                                                fontSize = 11.sp
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                                contentDescription = null,
                                                tint = colors.accent,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Workspace C: Draft Canvas (Results & Commit) ────────
                StudioWorkspace.CANVAS -> {
                    if (isAiPlanning) {
                        // Mindful Drafting Animation
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(colors.accent.copy(alpha = 0.12f))
                                        .border(1.5.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(22.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        strokeWidth = 3.dp,
                                        color = colors.accent
                                    )
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "SYNTHESIZING ROUTINE",
                                    style = FormaTheme.typography.labelSmall.copy(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        letterSpacing = 1.6.sp
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = loadingMessages[currentLoadingMsgIndex],
                                    style = FormaTheme.typography.bodyMedium.copy(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.5.sp
                                )
                            }
                        }
                    } else if (aiPreviewResult != null) {
                        val result = aiPreviewResult!!
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Summary Blueprint Card
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

                            // Section A: Timeline Blocks
                            if (result.tasks.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "TIMELINE BLOCKS (${result.tasks.size})",
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

                        Spacer(modifier = Modifier.height(10.dp))

                        // Primary Bottom Action: Apply Blueprint
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
                    } else {
                        // Serene Empty Canvas State
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(colors.surfaceVariant.copy(alpha = 0.6f))
                                        .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Layers,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(18.dp))
                                Text(
                                    text = "Your Studio Canvas is Open",
                                    style = FormaTheme.typography.titleMedium.copy(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 17.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Choose an instant blueprint from the library or design your custom routine in Studio Prompt.",
                                    style = FormaTheme.typography.bodySmall.copy(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        lineHeight = 17.sp
                                    ),
                                    color = colors.textSecondary,
                                    fontSize = 12.5.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colors.accent)
                                            .formaPressEffect(targetScale = 0.94f) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                currentWorkspace = StudioWorkspace.BLUEPRINTS
                                            }
                                            .padding(horizontal = 14.dp, vertical = 9.dp)
                                    ) {
                                        Text(
                                            text = "Browse Blueprints",
                                            style = FormaTheme.typography.labelSmall.copy(
                                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            color = colors.onAccent,
                                            fontSize = 11.5.sp
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colors.surfaceVariant)
                                            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                            .formaPressEffect(targetScale = 0.94f) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                currentWorkspace = StudioWorkspace.PROMPT
                                            }
                                            .padding(horizontal = 14.dp, vertical = 9.dp)
                                    ) {
                                        Text(
                                            text = "Compose Prompt",
                                            style = FormaTheme.typography.labelSmall.copy(
                                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary,
                                            fontSize = 11.5.sp
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
}
