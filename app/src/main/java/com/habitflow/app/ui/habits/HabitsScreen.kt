package com.habitflow.app.ui.habits

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
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
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.component.NotionButton
import com.habitflow.app.core.designsystem.component.NotionButtonStyle
import com.habitflow.app.core.designsystem.component.NotionDivider
import com.habitflow.app.core.designsystem.component.NotionTopAppBar
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.TimeOfDay
import com.habitflow.app.ui.habits.components.AddEditHabitDialog
import com.habitflow.app.ui.habits.components.HabitCard

@Composable
fun HabitsScreen(
    viewModel: HabitsViewModel = hiltViewModel()
) {
    val colors = NotionTheme.colors
    val uiState by viewModel.uiState.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }

    Scaffold(
        topBar = {
            NotionTopAppBar(
                title = "Habits",
                subtitle = "Unlimited recurring habits & streaks",
                actions = {
                    NotionButton(
                        text = "+ Habit",
                        onClick = {
                            editingHabit = null
                            showAddEditDialog = true
                        },
                        style = NotionButtonStyle.PRIMARY
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
                shape = NotionTheme.shapes.medium
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
        ) {
            // Filter Chips Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
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
                TimeOfDay.values().forEach { tod ->
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

            NotionDivider()

            val habitList = if (uiState.showArchived) uiState.archivedHabits else uiState.activeHabits

            if (habitList.isEmpty()) {
                EmptyHabitsState(
                    isArchivedView = uiState.showArchived,
                    onCreateHabit = {
                        editingHabit = null
                        showAddEditDialog = true
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(habitList, key = { it.id }) { habit ->
                        HabitCard(
                            habit = habit,
                            streakInfo = uiState.streaksMap[habit.id],
                            onEdit = {
                                editingHabit = habit
                                showAddEditDialog = true
                            },
                            onArchiveToggle = {
                                viewModel.archiveHabit(habit)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditHabitDialog(
            initialHabit = editingHabit,
            onDismiss = {
                showAddEditDialog = false
                editingHabit = null
            },
            onSave = { habit ->
                viewModel.saveHabit(habit)
                showAddEditDialog = false
                editingHabit = null
            },
            onDelete = { habit ->
                viewModel.deleteHabit(habit)
                showAddEditDialog = false
                editingHabit = null
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
    val colors = NotionTheme.colors

    Box(
        modifier = modifier
            .clip(NotionTheme.shapes.extraSmall)
            .background(if (isSelected) colors.textPrimary else colors.surfaceVariant)
            .border(
                1.dp,
                if (isSelected) Color.Transparent else colors.border,
                NotionTheme.shapes.extraSmall
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = NotionTheme.typography.labelMedium,
            color = if (isSelected) colors.surface else colors.textPrimary,
            fontSize = 12.sp
        )
    }
}

@Composable
fun EmptyHabitsState(
    isArchivedView: Boolean,
    onCreateHabit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = NotionTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isArchivedView) "📦" else "⚡",
            fontSize = 44.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isArchivedView) "No archived habits" else "Build your daily rituals",
            style = NotionTheme.typography.titleLarge,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isArchivedView) "Archived habits will appear here." else "HabitFlow gives you unlimited recurring habits, streaks, and analytics for free.",
            style = NotionTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (!isArchivedView) {
            Spacer(modifier = Modifier.height(24.dp))
            NotionButton(
                text = "+ Create First Habit",
                onClick = onCreateHabit,
                style = NotionButtonStyle.PRIMARY
            )
        }
    }
}
