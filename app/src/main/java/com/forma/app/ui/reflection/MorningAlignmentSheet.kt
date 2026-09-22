package com.forma.app.ui.reflection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.domain.model.EnergyLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorningAlignmentSheet(
    viewModel: DailyReflectionViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val keystone1 by viewModel.keystone1.collectAsState()
    val keystone2 by viewModel.keystone2.collectAsState()
    val keystone3 by viewModel.keystone3.collectAsState()
    val energyLevel by viewModel.energyLevel.collectAsState()
    val dailyQuote by viewModel.dailyQuote.collectAsState()
    val isSynthesizing by viewModel.isSynthesizing.collectAsState()
    val aiSynthesisResult by viewModel.aiSynthesisResult.collectAsState()
    val focusManager = LocalFocusManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FormaTheme.colors.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
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
                            .background(FormaTheme.colors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LightMode,
                            contentDescription = null,
                            tint = FormaTheme.colors.accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Morning Clarity",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FormaTheme.colors.textPrimary
                        )
                        Text(
                            text = "Set your 3 mindful anchors for today",
                            fontSize = 12.sp,
                            color = FormaTheme.colors.textSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = FormaTheme.colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Daily Zen Quote Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(FormaTheme.colors.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, FormaTheme.colors.border, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = dailyQuote,
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 18.sp,
                    color = FormaTheme.colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Energy Level Selector
            Text(
                text = "Today's Energy Rhythm",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = FormaTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EnergyOption(
                    title = "Gentle Flow",
                    selected = energyLevel == EnergyLevel.LOW,
                    onClick = { viewModel.setEnergyLevel(EnergyLevel.LOW) },
                    modifier = Modifier.weight(1f)
                )
                EnergyOption(
                    title = "Balanced",
                    selected = energyLevel == EnergyLevel.MEDIUM,
                    onClick = { viewModel.setEnergyLevel(EnergyLevel.MEDIUM) },
                    modifier = Modifier.weight(1f)
                )
                EnergyOption(
                    title = "Deep Focus",
                    selected = energyLevel == EnergyLevel.HIGH,
                    onClick = { viewModel.setEnergyLevel(EnergyLevel.HIGH) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gemini 1.5 Flash AI Day Synthesis Action
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(FormaTheme.colors.surface)
                    .border(1.dp, FormaTheme.colors.accent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .clickable(enabled = !isSynthesizing) {
                        viewModel.synthesizeDayWithAi()
                    }
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
                                .background(FormaTheme.colors.accentSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = FormaTheme.colors.accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Gemini Day Synthesis",
                                style = FormaTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = FormaTheme.colors.textPrimary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isSynthesizing) "Synthesizing mindful anchors..." else "Auto-align schedule & habit stacks",
                                style = FormaTheme.typography.bodySmall,
                                color = FormaTheme.colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (isSynthesizing) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = FormaTheme.colors.accent,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Align Schedule",
                            style = FormaTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = FormaTheme.colors.accent,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (aiSynthesisResult != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(FormaTheme.colors.accentSoft)
                        .border(1.dp, FormaTheme.colors.accent.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "CADENCE INSIGHT",
                            style = FormaTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = FormaTheme.colors.accent,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        )
                        Text(
                            text = aiSynthesisResult!!.energyCadenceNote,
                            style = FormaTheme.typography.bodySmall,
                            color = FormaTheme.colors.textPrimary,
                            fontSize = 12.sp
                        )
                        if (aiSynthesisResult!!.habitStackRecommendations.isNotEmpty()) {
                            Text(
                                text = aiSynthesisResult!!.habitStackRecommendations.first(),
                                style = FormaTheme.typography.bodySmall,
                                color = FormaTheme.colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3 Keystone Intentions
            Text(
                text = "Keystone Intentions (1–3)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = FormaTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            KeystoneInput(
                number = "1",
                value = keystone1,
                placeholder = "Primary intention (e.g. Finish core design)",
                onValueChange = { viewModel.setKeystone(0, it) },
                onDone = { focusManager.clearFocus() }
            )
            Spacer(modifier = Modifier.height(8.dp))
            KeystoneInput(
                number = "2",
                value = keystone2,
                placeholder = "Secondary intention (e.g. 30m mindful walk)",
                onValueChange = { viewModel.setKeystone(1, it) },
                onDone = { focusManager.clearFocus() }
            )
            Spacer(modifier = Modifier.height(8.dp))
            KeystoneInput(
                number = "3",
                value = keystone3,
                placeholder = "Mindful micro-habit (e.g. Drink 2L water)",
                onValueChange = { viewModel.setKeystone(2, it) },
                onDone = { focusManager.clearFocus() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.saveMorningAlignment()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FormaTheme.colors.accent,
                    contentColor = FormaTheme.colors.onAccent
                )
            ) {
                Text(
                    text = "Begin Day with Alignment",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun EnergyOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) FormaTheme.colors.accentSoft else FormaTheme.colors.surfaceVariant.copy(alpha = 0.4f)
    val border = if (selected) FormaTheme.colors.accent else FormaTheme.colors.border
    val textCol = if (selected) FormaTheme.colors.accent else FormaTheme.colors.textSecondary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = textCol
        )
    }
}

@Composable
private fun KeystoneInput(
    number: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(text = placeholder, fontSize = 13.sp, color = FormaTheme.colors.textSecondary.copy(alpha = 0.6f))
        },
        leadingIcon = {
            Text(
                text = number,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = FormaTheme.colors.accent,
                modifier = Modifier.padding(start = 12.dp)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
            autoCorrectEnabled = true,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FormaTheme.colors.accent,
            unfocusedBorderColor = FormaTheme.colors.border,
            focusedContainerColor = FormaTheme.colors.surfaceVariant.copy(alpha = 0.3f),
            unfocusedContainerColor = FormaTheme.colors.surfaceVariant.copy(alpha = 0.3f),
            focusedTextColor = FormaTheme.colors.textPrimary,
            unfocusedTextColor = FormaTheme.colors.textPrimary,
            cursorColor = FormaTheme.colors.accent
        )
    )
}
