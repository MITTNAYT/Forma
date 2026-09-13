package com.habitflow.app.ui.reflection

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.NotionTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EveningReflectionSheet(
    viewModel: DailyReflectionViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val gratitudeNote by viewModel.gratitudeNote.collectAsState()
    val mindfulnessScore by viewModel.mindfulnessScore.collectAsState()
    var rolloverTasks by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NotionTheme.colors.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NotionTheme.colors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bedtime,
                            contentDescription = null,
                            tint = NotionTheme.colors.accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Evening Sanctuary",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NotionTheme.colors.textPrimary
                        )
                        Text(
                            text = "Wind down and release the day",
                            fontSize = 12.sp,
                            color = NotionTheme.colors.textSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = NotionTheme.colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Mindfulness Rating (1 to 5 stars)
            Text(
                text = "Mindfulness & Flow Today",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = NotionTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (score in 1..5) {
                    val isFilled = score <= mindfulnessScore
                    IconButton(onClick = { viewModel.setMindfulnessScore(score) }) {
                        Icon(
                            imageVector = if (isFilled) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = "Rating $score",
                            tint = if (isFilled) NotionTheme.colors.accent else NotionTheme.colors.textSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gratitude note
            Text(
                text = "One Gratitude Note",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = NotionTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = gratitudeNote,
                onValueChange = { viewModel.setGratitudeNote(it) },
                placeholder = {
                    Text(
                        text = "What brought you peace or joy today?",
                        fontSize = 13.sp,
                        color = NotionTheme.colors.textSecondary.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp),
                shape = RoundedCornerShape(14.dp),
                singleLine = false,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NotionTheme.colors.accent,
                    unfocusedBorderColor = NotionTheme.colors.border,
                    focusedContainerColor = NotionTheme.colors.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = NotionTheme.colors.surfaceVariant.copy(alpha = 0.3f),
                    focusedTextColor = NotionTheme.colors.textPrimary,
                    unfocusedTextColor = NotionTheme.colors.textPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rollover incomplete tasks toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NotionTheme.colors.surfaceVariant.copy(alpha = 0.35f))
                    .clickable { rolloverTasks = !rolloverTasks }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rolloverTasks,
                    onCheckedChange = { rolloverTasks = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = NotionTheme.colors.accent,
                        checkmarkColor = NotionTheme.colors.onAccent
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Mindful Rollover",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NotionTheme.colors.textPrimary
                    )
                    Text(
                        text = "Shift today's unfinished tasks cleanly to tomorrow",
                        fontSize = 11.sp,
                        color = NotionTheme.colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.saveEveningReflection(rolloverIncompleteTasks = rolloverTasks)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NotionTheme.colors.accent,
                    contentColor = NotionTheme.colors.onAccent
                )
            ) {
                Text(
                    text = "Conclude Day with Peace",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
