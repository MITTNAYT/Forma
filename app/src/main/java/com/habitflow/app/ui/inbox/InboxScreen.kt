package com.habitflow.app.ui.inbox

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.component.NotionButton
import com.habitflow.app.core.designsystem.component.NotionButtonStyle
import com.habitflow.app.core.designsystem.component.NotionCard
import com.habitflow.app.core.designsystem.component.NotionCheckbox
import com.habitflow.app.domain.model.TimelineItem

@Composable
fun InboxScreen(
    onNavigateToCreateTask: () -> Unit,
    onNavigateToEditTask: (itemId: String) -> Unit,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val colors = NotionTheme.colors
    val inboxItems by viewModel.inboxItems.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateTask,
                containerColor = colors.accent,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 8.dp, end = 8.dp)
                    .size(58.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "New Inbox Task",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            // Header
            Text(
                text = "Inbox",
                style = NotionTheme.typography.displayLarge,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )

            if (inboxItems.isEmpty()) {
                // Exact replica of Image 4 inspo empty state
                EmptyInboxInspoState(
                    onNewTask = onNavigateToCreateTask
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(inboxItems, key = { it.id }) { item ->
                        InboxTaskRow(
                            item = item,
                            onToggle = { viewModel.toggleComplete(item) },
                            onSchedule = { viewModel.scheduleToToday(item) },
                            onClick = { onNavigateToEditTask(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyInboxInspoState(
    onNewTask: () -> Unit,
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
        // Glowing Blue Tray Glyph (matching inspo image 4)
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.accentMuted.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Inbox,
                contentDescription = "Inbox",
                tint = colors.accent,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your Unstructured Thoughts",
            style = NotionTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Capture tasks and thoughts as they come. Move them to your timeline when you're ready to schedule.",
            style = NotionTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Large Rounded Button matching inspo
        NotionButton(
            text = "+ New Inbox Task",
            onClick = onNewTask,
            style = NotionButtonStyle.PRIMARY,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(48.dp)
        )
    }
}

@Composable
fun InboxTaskRow(
    item: TimelineItem,
    onToggle: () -> Unit,
    onSchedule: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = NotionTheme.colors

    NotionCard(
        modifier = modifier.fillMaxWidth(),
        shape = NotionTheme.shapes.small,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NotionCheckbox(
                    checked = item.completed,
                    onCheckedChange = { onToggle() }
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.title,
                        style = NotionTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                    if (item.notes.isNotBlank()) {
                        Text(
                            text = item.notes,
                            style = NotionTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            maxLines = 1
                        )
                    }
                }
            }

            // Quick Move to Today action
            IconButton(
                onClick = onSchedule,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarToday,
                    contentDescription = "Schedule to Today",
                    tint = colors.accent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
