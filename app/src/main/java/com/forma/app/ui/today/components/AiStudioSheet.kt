package com.forma.app.ui.today.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.forma.app.domain.repository.AiPlanPreset
import com.forma.app.ui.today.TodayViewModel
import kotlinx.coroutines.delay

enum class AiStudioMode(val title: String, val icon: ImageVector) {
    DAY_PLAN("Day Agenda", Icons.Rounded.AutoAwesome),
    HABIT_ARCHITECT("Habit Architect", Icons.Rounded.Spa),
    GOAL_DECOMPOSE("Goal Breakdown", Icons.Rounded.Bolt)
}

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
                delay(2200)
                currentLoadingMsgIndex = (currentLoadingMsgIndex + 1) % loadingMessages.size
            }
        }
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
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.accentSoft),
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
                        Text(
                            text = "Forma AI Studio",
                            style = FormaTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Generate tasks, discover habits & break down goals",
                            style = FormaTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            fontSize = 11.5.sp
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

            // Mode Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceVariant)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AiStudioMode.entries.forEach { mode ->
                    val isSelected = activeMode == mode
                    val bg by animateColorAsState(
                        targetValue = if (isSelected) colors.accent else Color.Transparent,
                        label = "mode_bg"
                    )
                    val fg by animateColorAsState(
                        targetValue = if (isSelected) colors.onAccent else colors.textSecondary,
                        label = "mode_fg"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(bg)
                            .formaPressEffect(targetScale = 0.94f) {
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
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = mode.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = fg,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Content Area
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Quick Inspiration Chips
                item {
                    val promptChips = when (activeMode) {
                        AiStudioMode.DAY_PLAN -> listOf(
                            "Deep Work Sprint (3 Blocks)",
                            "Exam Prep & Active Recall",
                            "Mindful Self-Care Sunday",
                            "Fitness & Mobility Day",
                            "Dopamine Detox & Focus"
                        )
                        AiStudioMode.HABIT_ARCHITECT -> listOf(
                            "Morning Energy Stack",
                            "Nighttime Sleep Sanctuary",
                            "High-Cognition Deep Work",
                            "Mental Resilience & Breath",
                            "Physical Fitness & Hydration"
                        )
                        AiStudioMode.GOAL_DECOMPOSE -> listOf(
                            "Launch Mobile App in 30 Days",
                            "Run First 10K Marathon",
                            "Learn Kotlin & Android Dev",
                            "Read 12 Books This Year",
                            "Establish Daily Morning Meditation"
                        )
                    }

                    Column {
                        Text(
                            text = "INSPIRATION CHIPS",
                            fontWeight = FontWeight.Bold,
                            color = colors.textTertiary,
                            fontSize = 10.sp,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            promptChips.forEach { chipText ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.surfaceVariant)
                                        .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                        .formaPressEffect(targetScale = 0.95f) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            promptText = chipText
                                            focusManager.clearFocus()
                                            when (activeMode) {
                                                AiStudioMode.DAY_PLAN -> viewModel.generateAiPlan(prompt = chipText)
                                                AiStudioMode.HABIT_ARCHITECT -> viewModel.generateAiPlan(prompt = chipText)
                                                AiStudioMode.GOAL_DECOMPOSE -> viewModel.decomposeGoalWithAi(chipText)
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = chipText,
                                        style = FormaTheme.typography.bodySmall,
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Prompt Input Box
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surfaceVariant)
                            .border(1.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = promptText,
                                onValueChange = { promptText = it },
                                placeholder = {
                                    Text(
                                        text = when (activeMode) {
                                            AiStudioMode.DAY_PLAN -> "E.g., 2 meetings at 10am & 3pm, study biology, gym..."
                                            AiStudioMode.HABIT_ARCHITECT -> "E.g., Habits to reduce stress and boost morning energy..."
                                            AiStudioMode.GOAL_DECOMPOSE -> "E.g., Break down launching a tech startup into milestones..."
                                        },
                                        color = colors.textTertiary,
                                        fontSize = 12.5.sp
                                    )
                                },
                                singleLine = false,
                                maxLines = 3,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    autoCorrectEnabled = true,
                                    imeAction = ImeAction.Send
                                ),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (promptText.isNotBlank()) {
                                            focusManager.clearFocus()
                                            when (activeMode) {
                                                AiStudioMode.DAY_PLAN -> viewModel.generateAiPlan(prompt = promptText)
                                                AiStudioMode.HABIT_ARCHITECT -> viewModel.generateAiPlan(prompt = promptText)
                                                AiStudioMode.GOAL_DECOMPOSE -> viewModel.decomposeGoalWithAi(promptText)
                                            }
                                        }
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    cursorColor = colors.accent
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            if (isAiPlanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp,
                                    color = colors.accent
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(colors.accent)
                                        .formaPressEffect(targetScale = 0.90f) {
                                            focusManager.clearFocus()
                                            val effectivePrompt = promptText.ifBlank {
                                                when (activeMode) {
                                                    AiStudioMode.DAY_PLAN -> "High focus day"
                                                    AiStudioMode.HABIT_ARCHITECT -> "Mindful daily habits"
                                                    AiStudioMode.GOAL_DECOMPOSE -> "Master daily habits"
                                                }
                                            }
                                            when (activeMode) {
                                                AiStudioMode.DAY_PLAN -> viewModel.generateAiPlan(prompt = effectivePrompt)
                                                AiStudioMode.HABIT_ARCHITECT -> viewModel.generateAiPlan(prompt = effectivePrompt)
                                                AiStudioMode.GOAL_DECOMPOSE -> viewModel.decomposeGoalWithAi(effectivePrompt)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = "Generate",
                                        tint = colors.onAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Loading Status Text
                if (isAiPlanning) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.accentSoft)
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = colors.accent
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = loadingMessages[currentLoadingMsgIndex],
                                    style = FormaTheme.typography.bodySmall,
                                    color = colors.accent,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // AI Results Preview
                aiPreviewResult?.let { result ->
                    // Summary & Coaching Tips Card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(colors.accentSoft.copy(alpha = 0.5f))
                                .border(1.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lightbulb,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = result.title.ifBlank { "Mindful Flow Blueprint" },
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary,
                                        fontSize = 14.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = result.summary,
                                    style = FormaTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                    lineHeight = 17.sp,
                                    fontSize = 12.sp
                                )
                                if (result.tips.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        result.tips.forEach { tip ->
                                            Text(
                                                text = "• $tip",
                                                style = FormaTheme.typography.bodySmall,
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

                    // Section 1: Generated Tasks
                    if (result.tasks.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "GENERATED TIMELINE TASKS (${result.tasks.size})",
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textTertiary,
                                    fontSize = 10.5.sp,
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    text = "${selectedTaskIds.size} Selected",
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.accent,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        items(result.tasks, key = { it.id }) { task ->
                            val isSelected = selectedTaskIds.contains(task.id)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.surfaceVariant)
                                    .border(
                                        1.dp,
                                        if (isSelected) colors.accent.copy(alpha = 0.7f) else colors.border.copy(alpha = 0.4f),
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
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(colors.accentSoft),
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
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.textPrimary,
                                                    fontSize = 13.5.sp
                                                )
                                                if (task.startTime != null && task.endTime != null) {
                                                    Text(
                                                        text = "${task.startTime} – ${task.endTime}",
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
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) colors.accent else colors.surface)
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
                                            style = FormaTheme.typography.bodySmall,
                                            color = colors.textSecondary,
                                            fontSize = 11.5.sp,
                                            lineHeight = 15.sp
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
                                                        style = FormaTheme.typography.bodySmall,
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

                    // Section 2: Suggested Habits & Rituals
                    if (result.suggestedHabits.isNotEmpty()) {
                        item {
                            Text(
                                text = "RECOMMENDED HABITS & RITUALS (${result.suggestedHabits.size})",
                                fontWeight = FontWeight.Bold,
                                color = colors.textTertiary,
                                fontSize = 10.5.sp,
                                letterSpacing = 1.2.sp
                            )
                        }

                        items(result.suggestedHabits, key = { it.id }) { habit ->
                            val isAdded = addedHabitIds.contains(habit.id)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.surfaceVariant)
                                    .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(colors.accentSoft),
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
                                                        color = colors.accent,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "${habit.repeatDays.size} days/wk",
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
                                            .background(if (isAdded) colors.accent else colors.surface)
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
                                                text = if (isAdded) "Added" else "Add Ritual",
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

            // Primary Bottom Action
            aiPreviewResult?.let { result ->
                val selectedTasks = result.tasks.filter { selectedTaskIds.contains(it.id) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
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
                        .padding(vertical = 15.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Apply ${selectedTasks.size} Tasks to Today's Timeline",
                        fontWeight = FontWeight.Bold,
                        color = colors.onAccent,
                        fontSize = 14.5.sp,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
