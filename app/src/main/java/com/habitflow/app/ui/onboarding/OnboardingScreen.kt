package com.habitflow.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.habitflow.app.core.designsystem.component.FormaEmblem
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.core.designsystem.NotionTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val colors = NotionTheme.colors
    val step by viewModel.step.collectAsState()
    val name by viewModel.name.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        containerColor = colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    (slideInHorizontally { width -> width } + fadeIn()) with
                            (slideOutHorizontally { width -> -width } + fadeOut())
                },
                label = "onboarding_steps"
            ) { currentStep ->
                when (currentStep) {
                    OnboardingStep.WELCOME -> {
                        WelcomeStep(
                            onStart = { viewModel.goToNameStep() }
                        )
                    }
                    OnboardingStep.ENTER_NAME -> {
                        EnterNameStep(
                            name = name,
                            onNameChange = { viewModel.onNameChange(it) },
                            onContinue = {
                                keyboardController?.hide()
                                viewModel.submitName()
                            }
                        )
                    }
                    OnboardingStep.GREETING -> {
                        GreetingStep(
                            name = name,
                            onEnterApp = {
                                viewModel.completeOnboarding(onOnboardingFinished)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(
    onStart: () -> Unit
) {
    val colors = NotionTheme.colors

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Forma Architectural Emblem
        FormaEmblem(
            size = 104.dp,
            animated = true
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Brand Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "WELCOME TO FORMA",
                style = NotionTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.accent,
                letterSpacing = 1.4.sp,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Give Form to Your Days.\nCultivate lasting rhythm.",
            style = NotionTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 30.sp,
            lineHeight = 38.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Forma is Latin for form and shape. An architectural routine and focus sanctuary crafted for intentional living. Completely offline, zero tracking, and pure focus.",
            style = NotionTheme.typography.bodyMedium,
            color = colors.textSecondary,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accent,
                contentColor = colors.onAccent
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Begin Clean Slate",
                style = NotionTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun EnterNameStep(
    name: String,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    val colors = NotionTheme.colors

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        // Step indicator
        Text(
            text = "STEP 1 OF 2 • PERSONAL SANCTUARY",
            style = NotionTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colors.accent,
            letterSpacing = 1.2.sp,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "What should we call you?",
            style = NotionTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 28.sp,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your name helps personalize your daily greetings and focus sanctuary.",
            style = NotionTheme.typography.bodyMedium,
            color = colors.textSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = {
                Text(
                    text = "e.g. Maya or David",
                    color = colors.textTertiary,
                    style = NotionTheme.typography.bodyLarge
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = colors.accent
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (name.isNotBlank()) onContinue()
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.border,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinue,
            enabled = name.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accent,
                contentColor = colors.onAccent,
                disabledContainerColor = colors.surfaceVariant,
                disabledContentColor = colors.textTertiary
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Continue",
                style = NotionTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun GreetingStep(
    name: String,
    onEnterApp: () -> Unit
) {
    val colors = NotionTheme.colors
    val displayName = name.trim().ifBlank { "Friend" }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Welcome Sprout Badge
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(colors.accentSoft)
                .border(2.dp, colors.accent.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to Forma, $displayName! ✨",
            style = NotionTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 30.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Your personal sanctuary is ready. We've prepared a 100% clean slate so you can give form to habits and focus that truly matter to you.",
            style = NotionTheme.typography.bodyMedium,
            color = colors.textSecondary,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 3 Key Mindful Features
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BulletRow(text = "Pure Clean Slate — 0 pre-populated clutter")
                BulletRow(text = "Private Offline Sanctuary — no ads, no trackers")
                BulletRow(text = "Forma Rhythm Matrix & Tactile Focus Timer")
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onEnterApp,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accent,
                contentColor = colors.onAccent
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Enter HabitFlow",
                style = NotionTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun BulletRow(text: String) {
    val colors = NotionTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(colors.accent)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = NotionTheme.typography.bodySmall,
            color = colors.textPrimary,
            fontSize = 13.sp
        )
    }
}
