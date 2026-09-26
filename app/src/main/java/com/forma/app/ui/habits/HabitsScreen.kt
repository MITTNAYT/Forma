package com.forma.app.ui.habits

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.component.FormaButton
import com.forma.app.core.designsystem.component.FormaButtonStyle
import com.forma.app.core.designsystem.component.FormaDivider
import com.forma.app.core.designsystem.motion.formaStaggeredEntrance
import com.forma.app.core.designsystem.component.FormaTopAppBar
import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.TimeOfDay
import com.forma.app.ui.habits.components.HabitCard
import com.forma.app.ui.habits.components.HabitStackSequencerSheet
import com.forma.app.ui.habits.components.HabitTemplatesSheet
import com.forma.app.ui.timeline.CreationType
import com.forma.app.ui.timeline.components.AddEditTimelineSheet
import com.forma.app.ui.today.components.InlineQuickEntryBar

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun HabitsScreen(
    viewModel: HabitsViewModel = hiltViewModel()
) {
    val colors = FormaTheme.colors
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }
    var showStackSequencerSheet by remember { mutableStateOf(false) }
    var showTemplatesSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FormaTopAppBar(
                title = "Habits",
                subtitle = "Unlimited recurring habits & streaks",
                actions = {
                    FormaButton(
                        text = "Templates",
                        onClick = { showTemplatesSheet = true },
                        style = FormaButtonStyle.OUTLINE
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (uiState.activeHabits.isNotEmpty() && !uiState.showArchived) {
                        FormaButton(
                            text = "▶ Flow",
                            onClick = { showStackSequencerSheet = true },
                            style = FormaButtonStyle.OUTLINE
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    FormaButton(
                        text = "+ Habit",
                        onClick = {
                            editingHabit = null
                            showAddEditDialog = true
                        },
                        style = FormaButtonStyle.PRIMARY
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingHabit = null
                    showAddEditDialog = true
                },
                containerColor = colors.accent,
                contentColor = Color.White,
                shape = FormaTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "New Habit"
                )
            }
        },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            // Quick Inline Habit Creator Bar
            InlineQuickEntryBar(
                onQuickAdd = { name, _ ->
                    viewModel.quickAddHabitInline(name)
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = "Add new recurring habit..."
            )

            // Live Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.5f), androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colors.textTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
                    androidx.compose.foundation.text.BasicTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier.weight(1f),
                        textStyle = FormaTheme.typography.bodyMedium.copy(
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
                            lineHeight = 20.sp
                        ),
                        singleLine = true,
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.accent),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Search,
                            autoCorrectEnabled = true
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSearch = { focusManager.clearFocus() }
                        ),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (uiState.searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search habits, intentions, or tags...",
                                        style = FormaTheme.typography.bodyMedium.copy(
                                            platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
                                            lineHeight = 20.sp
                                        ),
                                        color = colors.textTertiary,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    if (uiState.searchQuery.isNotEmpty()) {
                        androidx.compose.material3.IconButton(
                            onClick = { viewModel.setSearchQuery("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Filter Chips Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All Active"
                FilterChip(
                    text = "All Active",
                    isSelected = !uiState.showArchived && uiState.selectedTimeOfDayFilter == null,
                    onClick = {
                        viewModel.setShowArchived(false)
                        viewModel.setTimeOfDayFilter(null)
                    }
                )

                // Time of Day chips
                TimeOfDay.entries.forEach { tod ->
                    FilterChip(
                        text = tod.displayName,
                        isSelected = !uiState.showArchived && uiState.selectedTimeOfDayFilter == tod,
                        onClick = {
                            viewModel.setShowArchived(false)
                            viewModel.setTimeOfDayFilter(tod)
                        }
                    )
                }

                // Archived chip
                FilterChip(
                    text = "Archived (${uiState.archivedHabits.size})",
                    isSelected = uiState.showArchived,
                    onClick = {
                        viewModel.setShowArchived(true)
                    }
                )
            }

            FormaDivider()

            val habitList = if (uiState.showArchived) uiState.archivedHabits else uiState.activeHabits

            if (habitList.isEmpty()) {
                EmptyHabitsState(
                    isArchivedView = uiState.showArchived,
                    onCreateHabit = {
                        editingHabit = null
                        showAddEditDialog = true
                    },
                    onBrowseTemplates = {
                        showTemplatesSheet = true
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(
                        items = habitList,
                        key = { _, habit -> habit.id },
                        contentType = { _, _ -> "HabitCard" }
                    ) { index, habit ->
                        HabitCard(
                            habit = habit,
                            streakInfo = uiState.streaksMap[habit.id],
                            modifier = Modifier.formaStaggeredEntrance(index),
                            onEdit = {
                                editingHabit = habit
                                showAddEditDialog = true
                            },
                            onArchiveToggle = {
                                viewModel.archiveHabit(habit)
                            },
                            onWinteringToggle = {
                                viewModel.toggleWintering(habit)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditTimelineSheet(
            itemId = editingHabit?.id,
            initialCreationType = CreationType.HABIT,
            onDismiss = {
                showAddEditDialog = false
                editingHabit = null
            }
        )
    }


    if (showStackSequencerSheet && uiState.activeHabits.isNotEmpty()) {
        HabitStackSequencerSheet(
            habitStack = uiState.activeHabits,
            onCompleteHabit = { habitId ->
                viewModel.completeHabit(habitId)
            },
            onDismiss = {
                showStackSequencerSheet = false
            }
        )
    }

    if (showTemplatesSheet) {
        HabitTemplatesSheet(
            onAddHabit = { habit ->
                viewModel.saveHabit(habit)
            },
            onDismiss = {
                showTemplatesSheet = false
            }
        )
    }
}

@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FormaTheme.colors

    Box(
        modifier = modifier
            .clip(FormaTheme.shapes.extraSmall)
            .background(if (isSelected) colors.textPrimary else colors.surfaceVariant)
            .border(
                1.dp,
                if (isSelected) Color.Transparent else colors.border,
                FormaTheme.shapes.extraSmall
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = FormaTheme.typography.labelMedium,
            color = if (isSelected) colors.surface else colors.textPrimary,
            fontSize = 12.sp
        )
    }
}

@Composable
fun EmptyHabitsState(
    isArchivedView: Boolean,
    onCreateHabit: () -> Unit,
    onBrowseTemplates: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = FormaTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(colors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isArchivedView) Icons.Rounded.Archive else Icons.Rounded.Spa,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isArchivedView) "No archived rituals" else "A clean slate.",
            style = FormaTheme.typography.titleLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isArchivedView) "Archived rituals will appear here peacefully." else "Add a quiet moment to anchor your day.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (!isArchivedView) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FormaButton(
                    text = "Templates",
                    onClick = onBrowseTemplates,
                    style = FormaButtonStyle.OUTLINE
                )
                FormaButton(
                    text = "+ Create Ritual",
                    onClick = onCreateHabit,
                    style = FormaButtonStyle.PRIMARY
                )
            }
        }
    }
}
