package com.forma.app.ui.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.domain.repository.FocusItemSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommitmentDetailSheet(
    item: FocusItemSummary,
    onDismiss: () -> Unit,
    onStartFocusSession: (FocusItemSummary) -> Unit
) {
    val colors = FormaTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (item.isHabit) "RITUAL COMMITMENT" else "TASK COMMITMENT",
                        style = FormaTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.title,
                        style = FormaTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 18.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
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

            Spacer(modifier = Modifier.height(24.dp))

            // Focus Metrics Bento Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val hrs = item.totalMinutes / 60
                            val mins = item.totalMinutes % 60
                            val timeStr = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"

                            Text(
                                text = "TOTAL FOCUS INVESTED",
                                style = FormaTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textTertiary,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = timeStr,
                                style = FormaTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 32.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(colors.accentSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val estimatedSessions = (item.totalMinutes / 25).coerceAtLeast(1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.surface)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "EST. SESSIONS",
                                    style = FormaTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textTertiary,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$estimatedSessions (~25m ea)",
                                    style = FormaTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.surface)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "FLOW TIER",
                                    style = FormaTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textTertiary,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (item.totalMinutes >= 120) "Deep Mastery" else "Emerging Habit",
                                    style = FormaTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    onStartFocusSession(item)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.onAccent
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start 25m Focus Session",
                    style = FormaTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
