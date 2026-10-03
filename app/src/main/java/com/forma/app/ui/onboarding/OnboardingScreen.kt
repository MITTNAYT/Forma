package com.forma.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.component.FormaButton
import com.forma.app.core.designsystem.component.FormaButtonStyle
import com.forma.app.core.designsystem.component.FormaEmblem
import com.forma.app.core.designsystem.icon.FormaIcon
import com.forma.app.core.designsystem.motion.formaPressEffect

@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val colors = FormaTheme.colors
    val step by viewModel.step.collectAsState()
    val name by viewModel.name.collectAsState()
    val starterHabits by viewModel.starterHabits.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        containerColor = colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                label = "onboarding_screen_step"
            ) { currentStep ->
                when (currentStep) {
                    OnboardingStep.WELCOME_NAME -> {
                        WelcomeNameStep(
                            name = name,
                            onNameChange = { viewModel.onNameChange(it) },
                            onContinue = {
                                keyboardController?.hide()
                                viewModel.submitName()
                            }
                        )
                    }

                    OnboardingStep.STARTER_HABITS -> {
                        StarterHabitsStep(
                            habits = starterHabits,
                            onToggleHabit = { viewModel.toggleStarterHabit(it) },
                            onBack = { viewModel.goToPreviousStep() },
                            onContinue = { viewModel.submitHabits() },
                            onSkip = { viewModel.skipStarterHabits() }
                        )
                    }

                    OnboardingStep.READY -> {
                        ReadyStep(
                            name = name,
                            selectedCount = starterHabits.count { it.isSelected },
                            onBack = { viewModel.goToPreviousStep() },
                            onFinish = {
                                viewModel.completeOnboarding(onOnboardingFinished)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ── Step 1: Welcome & Name ───────────────────────────────────────────────────

@Composable
private fun WelcomeNameStep(
    name: String,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    val colors = FormaTheme.colors
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.6f))

        FormaEmblem(
            size = 80.dp,
            animated = true
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Forma",
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 32.sp,
            letterSpacing = (-0.8).sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "One job: see what to do today,\nand check it off with zero noise.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(36.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "WHAT SHOULD WE CALL YOU?",
                style = FormaTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textTertiary,
                letterSpacing = 1.2.sp,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = {
                    Text(
                        text = "Your name or alias",
                        color = colors.textTertiary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onContinue()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.border,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    cursorColor = colors.accent,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        FormaButton(
            text = "Continue",
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onContinue()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = colors.onAccent,
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

// ── Step 2: Choose Starter Rituals ──────────────────────────────────────────

@Composable
private fun StarterHabitsStep(
    habits: List<StarterHabitItem>,
    onToggleHabit: (Int) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    val colors = FormaTheme.colors
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Top Back Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant.copy(alpha = 0.85f))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "STARTER RITUALS",
            style = FormaTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colors.textTertiary,
            letterSpacing = 1.4.sp,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Pick 1 to 3 to start today",
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 26.sp,
            letterSpacing = (-0.6).sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "You can customize or add your own anytime from the Today canvas.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(habits) { index, item ->
                StarterHabitRow(
                    item = item,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleHabit(index)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FormaButton(
            text = "Continue",
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onContinue()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = colors.onAccent,
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSkip()
                }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Skip and start completely from zero",
                style = FormaTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.textTertiary,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun StarterHabitRow(
    item: StarterHabitItem,
    onClick: () -> Unit
) {
    val colors = FormaTheme.colors
    val tagColor = try {
        Color(android.graphics.Color.parseColor(item.colorTag))
    } catch (_: Exception) {
        colors.accent
    }

    val borderColor = if (item.isSelected) colors.accent else colors.border.copy(alpha = 0.6f)
    val bgColor = if (item.isSelected) colors.surfaceVariant else colors.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(18.dp))
            .formaPressEffect()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(tagColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            FormaIcon(
                iconKey = item.icon,
                contentDescription = item.name,
                tint = tagColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = FormaTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.subtitle,
                style = FormaTheme.typography.bodySmall,
                color = colors.textTertiary,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (item.isSelected) colors.accent else Color.Transparent)
                .border(
                    width = 1.5.dp,
                    color = if (item.isSelected) colors.accent else colors.border,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (item.isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected",
                    tint = colors.onAccent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ── Step 3: Ready ───────────────────────────────────────────────────────────

@Composable
private fun ReadyStep(
    name: String,
    selectedCount: Int,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val colors = FormaTheme.colors
    val haptic = LocalHapticFeedback.current
    val displayName = name.trim().ifBlank { "there" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant.copy(alpha = 0.85f))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.7f))

        FormaEmblem(
            size = 84.dp,
            animated = true
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "You're all set, $displayName.",
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 28.sp,
            letterSpacing = (-0.6).sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        val countText = if (selectedCount > 0) {
            "$selectedCount starter ritual${if (selectedCount > 1) "s are" else " is"} primed on your canvas."
        } else {
            "Your clean canvas is ready for your first ritual."
        }

        Text(
            text = "$countText Show up, tap to complete, and let momentum build day by day.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        FormaButton(
            text = "Open Forma",
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onFinish()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = colors.onAccent,
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}
