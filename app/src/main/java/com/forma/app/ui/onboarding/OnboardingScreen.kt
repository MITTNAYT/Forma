package com.forma.app.ui.onboarding

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Terminal
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forma.app.R
import com.forma.app.core.designsystem.CoffeeLightAccent
import com.forma.app.core.designsystem.CoffeeLightBg
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.LavenderLightAccent
import com.forma.app.core.designsystem.LavenderLightBg
import com.forma.app.core.designsystem.MatchaLightAccent
import com.forma.app.core.designsystem.MatchaLightBg
import com.forma.app.core.designsystem.MonoLightAccent
import com.forma.app.core.designsystem.MonoLightBg
import com.forma.app.core.designsystem.TerracottaLightAccent
import com.forma.app.core.designsystem.TerracottaLightBg
import com.forma.app.core.designsystem.component.FormaButton
import com.forma.app.core.designsystem.component.FormaButtonStyle
import com.forma.app.core.designsystem.component.FormaEmblem
import com.forma.app.core.designsystem.motion.formaPressEffect
import com.forma.app.domain.repository.PaletteFamily

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val colors = FormaTheme.colors
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
                    OnboardingStep.CHOOSE_THEME -> {
                        ChooseThemeStep(
                            onSelectTheme = { palette -> viewModel.selectTheme(palette) }
                        )
                    }
                    OnboardingStep.CHOOSE_STARTER_PACK -> {
                        val selectedPack by viewModel.selectedPack.collectAsState()
                        val starterHabits by viewModel.starterHabits.collectAsState()
                        ChooseStarterPackStep(
                            selectedPack = selectedPack,
                            starterHabits = starterHabits,
                            onSelectPack = { viewModel.selectStarterPack(it) },
                            onToggleHabit = { viewModel.toggleStarterHabit(it) },
                            onContinue = { viewModel.proceedToGreeting() }
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
    val colors = FormaTheme.colors

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Forma Architectural Emblem centered 80x80dp
        FormaEmblem(
            size = 80.dp,
            animated = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Headline: Less noise. More intention.
        Text(
            text = "Less noise. More intention.",
            style = FormaTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.6).sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Sub-headline: forma is your quiet space to build daily rhythm without the guilt.
        Text(
            text = "forma is your quiet space to build daily rhythm without the guilt.",
            style = FormaTheme.typography.bodyLarge,
            fontWeight = FontWeight.Normal,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 4. "Get Started →" button
        FormaButton(
            text = "Get Started",
            onClick = onStart,
            style = FormaButtonStyle.PRIMARY,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun EnterNameStep(
    name: String,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    val colors = FormaTheme.colors

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        // Step indicator
        Text(
            text = "STEP 1 OF 3 • PERSONAL SANCTUARY",
            style = FormaTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colors.accent,
            letterSpacing = 1.2.sp,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "What should we call you?",
            style = FormaTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 28.sp,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your name helps personalize your daily greetings and focus sanctuary.",
            style = FormaTheme.typography.bodyMedium,
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
                    style = FormaTheme.typography.bodyLarge
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
                style = FormaTheme.typography.labelLarge,
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
private fun ChooseThemeStep(
    onSelectTheme: (PaletteFamily) -> Unit
) {
    val colors = FormaTheme.colors

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "STEP 2 OF 3 • PHILOSOPHY & PALETTE",
            style = FormaTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colors.accent,
            letterSpacing = 1.2.sp,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "No streaks. Just presence.",
            style = FormaTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Focus on showing up, one small ritual at a time. Select your visual sanctuary:",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Theme palette cards
        val palettes = listOf(
            Triple(PaletteFamily.MATCHA_OAT, "Matcha & Oat", "Botanical sage — calm and grounded"),
            Triple(PaletteFamily.COFFEE_CREAM, "Espresso & Champagne", "Rich, warm, and sophisticated"),
            Triple(PaletteFamily.MONOCHROME, "Monochrome", "Pure black and white — OLED-sharp"),
            Triple(PaletteFamily.TERRACOTTA_SAND, "Terracotta & Sand", "Earthy clay warmth"),
            Triple(PaletteFamily.LAVENDER_MILK, "Lavender & Milk", "Serene, soft, and dreamy"),
        )

        palettes.forEach { (palette, name, desc) ->
            val previewAccent = when (palette) {
                PaletteFamily.MATCHA_OAT -> MatchaLightAccent
                PaletteFamily.COFFEE_CREAM, PaletteFamily.WALNUT_ESPRESSO -> CoffeeLightAccent
                PaletteFamily.MONOCHROME -> MonoLightAccent
                PaletteFamily.TERRACOTTA_SAND -> TerracottaLightAccent
                PaletteFamily.LAVENDER_MILK -> LavenderLightAccent
            }
            val previewBg = when (palette) {
                PaletteFamily.MATCHA_OAT -> MatchaLightBg
                PaletteFamily.COFFEE_CREAM, PaletteFamily.WALNUT_ESPRESSO -> CoffeeLightBg
                PaletteFamily.MONOCHROME -> MonoLightBg
                PaletteFamily.TERRACOTTA_SAND -> TerracottaLightBg
                PaletteFamily.LAVENDER_MILK -> LavenderLightBg
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                    .formaPressEffect(targetScale = 0.97f) { onSelectTheme(palette) }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Color preview swatch
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(previewBg)
                            .border(1.dp, previewAccent.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(previewAccent)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            style = FormaTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = desc,
                            style = FormaTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }

                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = colors.textTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ChooseStarterPackStep(
    selectedPack: StarterPackType,
    starterHabits: List<StarterHabitItem>,
    onSelectPack: (StarterPackType) -> Unit,
    onToggleHabit: (Int) -> Unit,
    onContinue: () -> Unit
) {
    val colors = FormaTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "STEP 3 OF 3 • COMMITMENT",
            style = FormaTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colors.accent,
            letterSpacing = 1.2.sp,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Where intention takes form.",
            style = FormaTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose your first ritual for today. You can shape or expand your cadence anytime.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3 Starter Pack Selector Cards
        StarterPackType.values().forEach { pack ->
            val isSelected = pack == selectedPack
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSelected) colors.accentSoft else colors.surface)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) colors.accent else colors.border.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .formaPressEffect(targetScale = 0.98f) { onSelectPack(pack) }
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) colors.accent else colors.surfaceVariant)
                            .border(1.dp, if (isSelected) colors.accent else colors.border, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val packIcon = when (pack) {
                            StarterPackType.MINDFUL_LIVING -> Icons.Rounded.Spa
                            StarterPackType.DEEP_WORK -> Icons.Rounded.Terminal
                            StarterPackType.HEALTH_VITALITY -> Icons.Rounded.FitnessCenter
                        }
                        Icon(
                            imageVector = packIcon,
                            contentDescription = null,
                            tint = if (isSelected) colors.onAccent else colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pack.title,
                            style = FormaTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) colors.accent else colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = pack.subtitle,
                            style = FormaTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Habit Checklist
        Text(
            text = "INCLUDED RITUALS (TAP TO TOGGLE)",
            style = FormaTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colors.textTertiary,
            letterSpacing = 1.2.sp,
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        starterHabits.forEachIndexed { index, habit ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .formaPressEffect(targetScale = 0.98f) { onToggleHabit(index) }
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (habit.isSelected) colors.accent else colors.surfaceVariant)
                            .border(1.dp, if (habit.isSelected) colors.accent else colors.border, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (habit.isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = colors.onAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habit.name,
                            style = FormaTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (habit.isSelected) colors.textPrimary else colors.textTertiary
                        )
                        if (habit.cueText != null) {
                            Text(
                                text = "Stacked cue: ${habit.cueText}",
                                style = FormaTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accent,
                contentColor = colors.onAccent
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "Continue",
                style = FormaTheme.typography.labelLarge,
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

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GreetingStep(
    name: String,
    onEnterApp: () -> Unit
) {
    val colors = FormaTheme.colors
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
            text = "Welcome to Forma, $displayName",
            style = FormaTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 30.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Your personal sanctuary is ready. We've prepared a 100% clean slate so you can give form to habits and focus that truly matter to you.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
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
                text = "Enter Forma",
                style = FormaTheme.typography.labelLarge,
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
    val colors = FormaTheme.colors
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
            style = FormaTheme.typography.bodySmall,
            color = colors.textPrimary,
            fontSize = 13.sp
        )
    }
}
