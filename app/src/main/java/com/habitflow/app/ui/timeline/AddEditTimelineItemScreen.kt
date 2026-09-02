package com.habitflow.app.ui.timeline

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.icon.HabitFlowIcon
import com.habitflow.app.domain.model.TimeOfDay
import com.habitflow.app.ui.timeline.components.IconPickerDialog
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTimelineItemScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditTimelineViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val colors = NotionTheme.colors
    val uiState by viewModel.uiState.collectAsState()

    var showIconPicker by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var newSubtaskText by remember { mutableStateOf("") }
    var showAddSubtaskField by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is AddEditTimelineEvent.Saved, is AddEditTimelineEvent.Deleted -> onNavigateBack()
            }
        }
    }

    val quickSuggestions = listOf(
        Pair("Hydrate 2L", "water_drop"),
        Pair("Deep Reading", "book"),
        Pair("Mindful Meditation", "self_improvement"),
        Pair("Cardio Run", "directions_run"),
        Pair("Deep Code Flow", "computer")
    )

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (uiState.title.isNotBlank()) colors.accent else colors.accent.copy(alpha = 0.4f))
                        .clickable(enabled = uiState.title.isNotBlank()) {
                            viewModel.save()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.isEditMode) "Save Changes" else if (uiState.creationType == CreationType.HABIT) "Save Ritual & Start Flow" else "Save Focused Task",
                        style = NotionTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onAccent,
                        fontSize = 16.sp
                    )
                }
            }
        },
        containerColor = colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.5f), CircleShape)
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = if (uiState.isEditMode) "Edit Commitment" else "New Ritual",
                        style = NotionTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 17.sp
                    )

                    if (uiState.isEditMode) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(colors.surface)
                                .border(1.dp, colors.border.copy(alpha = 0.5f), CircleShape)
                                .clickable { viewModel.delete() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(42.dp))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(18.dp)) }

            // 2. Type Selector Pill Switcher (Habit vs Task)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            Pair(CreationType.HABIT, "Daily Ritual (Repeats)"),
                            Pair(CreationType.TASK, "Single Task (One-time)")
                        ).forEach { (type, label) ->
                            val isSelected = uiState.creationType == type
                            val bg by animateColorAsState(
                                targetValue = if (isSelected) colors.accent else Color.Transparent,
                                label = "type_bg"
                            )
                            val textColor by animateColorAsState(
                                targetValue = if (isSelected) colors.onAccent else colors.textSecondary,
                                label = "type_text"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(bg)
                                    .clickable { viewModel.setCreationType(type) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
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

            // 3. Title & Icon Card + Suggestions
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Icon selector squircle
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.accentSoft)
                                    .border(1.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                    .clickable { showIconPicker = true },
                                contentAlignment = Alignment.Center
                            ) {
                                HabitFlowIcon(
                                    iconKey = uiState.icon,
                                    contentDescription = "Select Icon",
                                    tint = colors.accent,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Clean Minimal Title Input
                            TextField(
                                value = uiState.title,
                                onValueChange = { viewModel.setTitle(it) },
                                placeholder = {
                                    Text(
                                        text = if (uiState.creationType == CreationType.HABIT) "Name your daily ritual..." else "What's the intention?",
                                        style = NotionTheme.typography.titleMedium,
                                        color = colors.textTertiary,
                                        fontSize = 16.sp
                                    )
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                ),
                                textStyle = NotionTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Suggestion Chips
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickSuggestions.forEach { (name, icon) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(colors.surface)
                                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                    .clickable {
                                        viewModel.setTitle(name)
                                        viewModel.setIcon(icon)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    HabitFlowIcon(
                                        iconKey = icon,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = name,
                                        style = NotionTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(18.dp)) }

            // 4. Schedule, Cadence & Time Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Text(
                            text = "TIME OF DAY & RHYTHM",
                            style = NotionTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textTertiary,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Time of Day Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple(TimeOfDay.MORNING, "Morning", Icons.Rounded.WbSunny),
                                Triple(TimeOfDay.AFTERNOON, "Afternoon", Icons.Rounded.WbTwilight),
                                Triple(TimeOfDay.EVENING, "Evening", Icons.Rounded.NightsStay),
                                Triple(TimeOfDay.ANYTIME, "Anytime", Icons.Rounded.Spa)
                            ).forEach { (tod, label, icon) ->
                                val isSelected = uiState.timeOfDay == tod
                                val todBg by animateColorAsState(
                                    targetValue = if (isSelected) colors.accentSoft else colors.surfaceVariant,
                                    label = "tod_bg"
                                )
                                val todBorder = if (isSelected) colors.accent else Color.Transparent

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(todBg)
                                        .border(1.dp, todBorder, RoundedCornerShape(14.dp))
                                        .clickable { viewModel.setTimeOfDay(tod) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) colors.accent else colors.textTertiary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = label,
                                            style = NotionTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) colors.accent else colors.textPrimary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        // For Habits: Repeat Days Selector
                        if (uiState.creationType == CreationType.HABIT) {
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "REPEAT CADENCE",
                                style = NotionTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textTertiary,
                                letterSpacing = 1.2.sp,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                daysOfWeek.forEachIndexed { index, dayLetter ->
                                    val dayNum = index + 1
                                    val isSelected = uiState.repeatDays.contains(dayNum)

                                    val dayBg by animateColorAsState(
                                        targetValue = if (isSelected) colors.accent else colors.surfaceVariant,
                                        label = "day_bg_$dayNum"
                                    )
                                    val textColor by animateColorAsState(
                                        targetValue = if (isSelected) colors.onAccent else colors.textSecondary,
                                        label = "day_text_$dayNum"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(dayBg)
                                            .clickable { viewModel.toggleRepeatDay(dayNum) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayLetter,
                                            style = NotionTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(18.dp)) }

            // 5. Interactive Subtasks Builder
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CHECKABLE SUBTASKS (${uiState.subtasks.size})",
                                style = NotionTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textTertiary,
                                letterSpacing = 1.2.sp,
                                fontSize = 11.sp
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.accentSoft)
                                    .clickable { showAddSubtaskField = !showAddSubtaskField }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (showAddSubtaskField) "Cancel" else "+ Add Step",
                                    style = NotionTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (showAddSubtaskField) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newSubtaskText,
                                    onValueChange = { newSubtaskText = it },
                                    placeholder = { Text("Enter mini-step...", color = colors.textTertiary, fontSize = 13.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.accent,
                                        unfocusedBorderColor = colors.border,
                                        focusedTextColor = colors.textPrimary,
                                        unfocusedTextColor = colors.textPrimary
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (newSubtaskText.isNotBlank()) colors.accent else colors.surfaceVariant)
                                        .clickable(enabled = newSubtaskText.isNotBlank()) {
                                            viewModel.addSubtask(newSubtaskText)
                                            newSubtaskText = ""
                                            showAddSubtaskField = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "Add",
                                        tint = if (newSubtaskText.isNotBlank()) colors.onAccent else colors.textTertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        if (uiState.subtasks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            uiState.subtasks.forEachIndexed { index, subtask ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(colors.accentSoft),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                style = NotionTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.accent,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = subtask.title,
                                            style = NotionTheme.typography.bodyMedium,
                                            color = colors.textPrimary,
                                            fontSize = 13.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.removeSubtask(subtask.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Remove",
                                            tint = colors.textTertiary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(18.dp)) }

            // 6. Notes & Mindful Intentions Field
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Text(
                            text = "NOTES & REFLECTION",
                            style = NotionTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textTertiary,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TextField(
                            value = uiState.notes,
                            onValueChange = { viewModel.setNotes(it) },
                            placeholder = { Text("Add any mindful context or why this ritual matters...", color = colors.textTertiary, fontSize = 13.sp) },
                            minLines = 3,
                            maxLines = 5,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            textStyle = NotionTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (showIconPicker) {
        IconPickerDialog(
            selectedIcon = uiState.icon,
            onIconSelected = {
                viewModel.setIcon(it)
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }
}
