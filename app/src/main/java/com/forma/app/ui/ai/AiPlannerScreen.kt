package com.forma.app.ui.ai

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forma.app.core.designsystem.LuxuryIceBlue
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.component.FormaCard
import com.forma.app.ui.settings.components.ProPaywallBottomSheet
import com.forma.app.ui.today.TodayViewModel

@Composable
fun AiPlannerScreen(
    onNavigateToTimeline: () -> Unit,
    todayViewModel: TodayViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val colors = FormaTheme.colors

    var promptText by remember { mutableStateOf("") }
    val isAiPlanning by todayViewModel.isAiPlanning.collectAsState()
    var showPaywall by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Greeting Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Forma Intelligence",
                    style = FormaTheme.typography.labelSmall,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "What do you need to accomplish today?",
                style = FormaTheme.typography.displayLarge,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // AI Briefing Recommendation Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Daily Briefing
                Box(
                    modifier = Modifier
                        .width(230.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                        .clickable { todayViewModel.requestAiDayPlan() }
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FileDownload,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily Briefing",
                                style = FormaTheme.typography.titleMedium,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Synthesize daily commitments, habits, and energy peaks into a coherent plan.",
                            style = FormaTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Card 2: Deep Work Structuring
                Box(
                    modifier = Modifier
                        .width(230.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                        .clickable { todayViewModel.requestAiDayPlan() }
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Psychology,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Deep Flow Matrix",
                                style = FormaTheme.typography.titleMedium,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Block high-cognitive tasks with 15-minute restorative recovery intervals.",
                            style = FormaTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Luxury Prompt Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surface)
                    .border(1.5.dp, colors.accent.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        placeholder = {
                            Text(
                                text = "Tell me your schedule or goals...",
                                color = colors.textTertiary,
                                style = FormaTheme.typography.bodyMedium
                            )
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
                            autoCorrectEnabled = true,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Send
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSend = {
                                if (promptText.isNotBlank()) {
                                    focusManager.clearFocus()
                                    todayViewModel.requestAiDayPlan()
                                    promptText = ""
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
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    if (isAiPlanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = colors.accent
                        )
                    } else {
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                todayViewModel.requestAiDayPlan()
                                promptText = ""
                            }
                        ) {
                            Icon(
                                imageVector = if (promptText.isNotBlank()) Icons.AutoMirrored.Rounded.ArrowForward else Icons.Rounded.CameraAlt,
                                contentDescription = "Submit",
                                tint = colors.accent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showPaywall) {
        ProPaywallBottomSheet(onDismiss = { showPaywall = false })
    }
}
