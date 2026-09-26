package com.forma.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.forma.app.core.designsystem.motion.formaPressEffect
import com.forma.app.domain.model.AuthState
import com.forma.app.domain.model.SubscriptionTier
import com.forma.app.domain.repository.PaletteFamily
import com.forma.app.ui.auth.AuthViewModel
import com.forma.app.ui.auth.components.AuthModalBottomSheet
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val colors = FormaTheme.colors
    val step by viewModel.step.collectAsState()
    val name by viewModel.name.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    var showAuthSheet by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        containerColor = colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    (slideInHorizontally { width -> width } + fadeIn()) togetherWith
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
                            onContinue = { viewModel.proceedToCalibration() }
                        )
                    }
                    OnboardingStep.CALIBRATION -> {
                        CalibrationStep(
                            name = name,
                            onProceed = { viewModel.proceedToPaywall() }
                        )
                    }
                    OnboardingStep.PRO_PAYWALL -> {
                        val currentTier by viewModel.currentTier.collectAsState()
                        OnboardingPaywallStep(
                            currentTier = currentTier,
                            authState = authState,
                            onOpenAuth = { showAuthSheet = true },
                            onPurchase = { tier ->
                                viewModel.purchaseTier(tier) {
                                    // Proceeds to greeting
                                }
                            },
                            onContinueFree = { viewModel.continueAsFreeExplorer() }
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

        if (showAuthSheet) {
            AuthModalBottomSheet(
                viewModel = authViewModel,
                onDismiss = { showAuthSheet = false }
            )
        }
    }
}

// ── Screen 1: Welcome Manifesto ──────────────────────────────────────────

@Composable
private fun WelcomeStep(onStart: () -> Unit) {
    val colors = FormaTheme.colors

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FormaEmblem(
            size = 84.dp,
            animated = true
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Less noise. More intention.",
            style = FormaTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.8).sp,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Forma is your private sanctuary to cultivate daily rhythm, focus depth, and calm without the guilt.",
            style = FormaTheme.typography.bodyLarge,
            fontWeight = FontWeight.Normal,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(44.dp))

        FormaButton(
            text = "Begin Sanctuary Journey",
            onClick = onStart,
            style = FormaButtonStyle.PRIMARY,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── Screen 2: Enter Name & Form Consent ───────────────────────────────────

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
        Text(
            text = "SANCTUARY IDENTITY",
            style = FormaTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colors.accent,
            letterSpacing = 1.3.sp,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "What should we call you?",
            style = FormaTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 28.sp,
            letterSpacing = (-0.6).sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your name personalizes your daily greetings and reflection records.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = {
                Text(
                    text = "e.g. Alex or Maya",
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

        Spacer(modifier = Modifier.height(12.dp))

        // Form Consent & Data Minimization note
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "Form consent: Stored locally on your device only. Zero telemetry transmitted.",
                color = colors.textTertiary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

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
                .height(54.dp)
        ) {
            Text(
                text = "Continue",
                style = FormaTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

// ── Screen 3: Choose Atmosphere Palette ──────────────────────────────────

@Composable
private fun ChooseThemeStep(
    onSelectTheme: (PaletteFamily) -> Unit
) {
    val colors = FormaTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ATMOSPHERE & PALETTE",
            style = FormaTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colors.accent,
            letterSpacing = 1.3.sp,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose your color tone",
            style = FormaTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 26.sp,
            letterSpacing = (-0.6).sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Every palette is crafted to reduce eye strain and cultivate presence.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            fontSize = 13.5.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        val palettes = listOf(
            Triple(PaletteFamily.MATCHA_OAT, "Matcha & Oat (Signature)", listOf(Color(0xFF4E6542), Color(0xFFEDF3EB))),
            Triple(PaletteFamily.COFFEE_CREAM, "Espresso & Warm Cream", listOf(Color(0xFF2C221E), Color(0xFFEFE8DE))),
            Triple(PaletteFamily.TERRACOTTA_SAND, "Terracotta & Desert Sand", listOf(Color(0xFF8D5B4C), Color(0xFFF7EFE8))),
            Triple(PaletteFamily.LAVENDER_MILK, "Lavender & Chamomile", listOf(Color(0xFF5E548E), Color(0xFFEDE9F5))),
            Triple(PaletteFamily.MONOCHROME, "Zen Monochrome", listOf(Color(0xFF1E211E), Color(0xFFF0F2EE)))
        )

        palettes.forEach { (palette, name, swatches) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .formaPressEffect(targetScale = 0.98f) { onSelectTheme(palette) }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .border(1.dp, colors.border.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().background(swatches[0]))
                            Box(modifier = Modifier.size(14.dp).background(swatches[1]).align(Alignment.BottomEnd))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = name,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            fontSize = 14.sp
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
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ── Screen 4: Persona Starter Packs with Micro-Steps ─────────────────────

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
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "LIFESTYLE RHYTHM",
            style = FormaTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colors.accent,
            letterSpacing = 1.3.sp,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select your daily focus",
            style = FormaTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 26.sp,
            letterSpacing = (-0.6).sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Choose an archetype that matches your current lifestyle and goals.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        StarterPackType.values().forEach { pack ->
            val isSelected = pack == selectedPack
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSelected) colors.accentSoft else colors.surface)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) colors.accent else colors.border.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .formaPressEffect(targetScale = 0.98f) { onSelectPack(pack) }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) colors.accent else colors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (pack) {
                            StarterPackType.MINDFUL_LIVING -> Icons.Rounded.Spa
                            StarterPackType.DEEP_WORK -> Icons.Rounded.Terminal
                            StarterPackType.CREATIVE_STUDIO -> Icons.Rounded.AutoAwesome
                            StarterPackType.ACADEMIC_STUDY -> Icons.Rounded.Person
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) colors.onAccent else colors.textPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pack.title,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) colors.accent else colors.textPrimary,
                            fontSize = 14.5.sp
                        )
                        Text(
                            text = pack.subtitle,
                            color = colors.textSecondary,
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "INCLUDED RITUALS & MICRO-STEPS",
            style = FormaTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colors.textTertiary,
            letterSpacing = 1.2.sp,
            fontSize = 10.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        starterHabits.forEachIndexed { index, habit ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .formaPressEffect(targetScale = 0.98f) { onToggleHabit(index) }
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (habit.isSelected) colors.accent else colors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (habit.isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = colors.onAccent,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = habit.name,
                                fontWeight = FontWeight.SemiBold,
                                color = if (habit.isSelected) colors.textPrimary else colors.textTertiary,
                                fontSize = 13.5.sp
                            )
                            if (habit.subtasks.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${habit.subtasks.size} steps",
                                    color = colors.accent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        if (habit.cueText != null) {
                            Text(
                                text = "Cue: ${habit.cueText}",
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
                text = "Calibrate Sanctuary",
                style = FormaTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

// ── Screen 5: Kinetic Sanctuary Calibration ──────────────────────────────

@Composable
private fun CalibrationStep(
    name: String,
    onProceed: () -> Unit
) {
    val colors = FormaTheme.colors
    var completedStage by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        delay(600)
        completedStage = 1
        delay(700)
        completedStage = 2
        delay(700)
        completedStage = 3
        delay(600)
        completedStage = 4
        delay(800)
        onProceed()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { (completedStage / 4f).coerceIn(0.1f, 1f) },
                strokeWidth = 3.dp,
                color = colors.accent,
                trackColor = colors.accentSoft,
                modifier = Modifier.fillMaxSize()
            )
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Calibrating Your Sanctuary",
            style = FormaTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Personalizing daily curves and local vault for ${name.ifBlank { "you" }}...",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            fontSize = 13.5.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            listOf(
                "Analyzing circadian rhythm & chronotype...",
                "Initializing local SQLite zero-knowledge vault...",
                "Scheduling intentional morning & evening nudges...",
                "Sanctuary configuration complete."
            ).forEachIndexed { index, text ->
                val isDone = completedStage > index
                val isInProgress = completedStage == index

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDone) colors.accent
                                else if (isInProgress) colors.accentSoft
                                else colors.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = colors.onAccent,
                                modifier = Modifier.size(12.dp)
                            )
                        } else if (isInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(10.dp),
                                strokeWidth = 1.5.dp,
                                color = colors.accent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = text,
                        fontWeight = if (isInProgress || isDone) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isDone) colors.textPrimary else if (isInProgress) colors.accent else colors.textTertiary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onProceed,
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
                text = "View Sanctuary Pass",
                style = FormaTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

// ── Screen 6: Pro Paywall Sanctuary Funnel ────────────────────────────────

@Composable
private fun OnboardingPaywallStep(
    currentTier: SubscriptionTier,
    authState: AuthState,
    onOpenAuth: () -> Unit,
    onPurchase: (SubscriptionTier) -> Unit,
    onContinueFree: () -> Unit
) {
    val colors = FormaTheme.colors
    var selectedTier by remember { mutableStateOf(SubscriptionTier.LIFETIME_FOUNDER) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gold Founder Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFD4AF37).copy(alpha = 0.15f))
                .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.WorkspacePremium,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "FORMA PRO SANCTUARY",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37),
                    letterSpacing = 1.2.sp,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Give Form to Your Flow",
            style = FormaTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 26.sp,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.6).sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Full AI Day Synthesis, limitless rituals, and zero-knowledge encrypted cloud sync.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Cloud Sanctuary Account / Sign-Up Card (Option A)
        if (authState is AuthState.Authenticated) {
            val user = authState.user
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.accentSoft)
                    .border(1.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudDone,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Cloud Sanctuary Active",
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Linked to ${user.displayName ?: user.email ?: "your account"}",
                            color = colors.textSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .formaPressEffect(targetScale = 0.98f) { onOpenAuth() }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.accentSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudUpload,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Save Sanctuary to Cloud",
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Sign up with Google or Email (Optional)",
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.accentSoft)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Sign Up",
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tier Options
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Lifetime Founder Pass
            val isFounder = selectedTier == SubscriptionTier.LIFETIME_FOUNDER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isFounder) Color(0xFFFDF8EA) else colors.surface)
                    .border(
                        width = if (isFounder) 2.dp else 1.dp,
                        color = if (isFounder) Color(0xFFD4AF37) else colors.border.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .formaPressEffect(targetScale = 0.98f) { selectedTier = SubscriptionTier.LIFETIME_FOUNDER }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Master Architect Pass",
                                fontWeight = FontWeight.Bold,
                                color = if (isFounder) Color(0xFF2C2411) else colors.textPrimary,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFD4AF37))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "BEST VALUE",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2C2411),
                                    fontSize = 8.5.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Pay once, own forever • All future updates included",
                            color = if (isFounder) Color(0xFF6B5828) else colors.textSecondary,
                            fontSize = 11.5.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$19.99",
                            fontWeight = FontWeight.Bold,
                            color = if (isFounder) Color(0xFF2C2411) else colors.textPrimary,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "one-time",
                            color = colors.textTertiary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Monthly Pro Subscription
            val isMonthly = selectedTier == SubscriptionTier.MONTHLY_PRO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isMonthly) colors.accentSoft else colors.surface)
                    .border(
                        width = if (isMonthly) 2.dp else 1.dp,
                        color = if (isMonthly) colors.accent else colors.border.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .formaPressEffect(targetScale = 0.98f) { selectedTier = SubscriptionTier.MONTHLY_PRO }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Monthly Pro",
                                fontWeight = FontWeight.Bold,
                                color = if (isMonthly) colors.accent else colors.textPrimary,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.accentSoft)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "7-DAY TRIAL",
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    fontSize = 8.5.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Cancel anytime in Google Play • 7 days free",
                            color = colors.textSecondary,
                            fontSize = 11.5.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$3.99",
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "/ month",
                            color = colors.textTertiary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Features list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(
                "Full AI Day Synthesis & Weekly Zen Retrospective",
                "Unlimited Habits & Micro-Steps Checklists",
                "Zero-Knowledge AES-256 Encrypted Vault (.habitvault)",
                "All 5 Artisanal Color Palettes Unlocked",
                "Multi-Device Cloud Backup & Sync"
            ).forEach { perk ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(colors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = perk,
                        color = colors.textPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action CTA
        Button(
            onClick = { onPurchase(selectedTier) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedTier == SubscriptionTier.LIFETIME_FOUNDER) Color(0xFFD4AF37) else colors.accent,
                contentColor = if (selectedTier == SubscriptionTier.LIFETIME_FOUNDER) Color(0xFF2C2411) else colors.onAccent
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = if (selectedTier == SubscriptionTier.LIFETIME_FOUNDER) "Unlock Lifetime Founder" else "Start 7-Day Free Trial",
                style = FormaTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Free Explorer Fallback
        TextButton(
            onClick = onContinueFree,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Continue with Free Sanctuary",
                color = colors.textSecondary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "48-Hour Google Play instant refund guarantee • Cancel anytime",
            color = colors.textTertiary,
            fontSize = 10.5.sp
        )
    }
}

// ── Screen 7: Final Sanctuary Greeting ───────────────────────────────────

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
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(colors.accentSoft)
                .border(2.dp, colors.accent.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to Forma, $displayName",
            style = FormaTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 28.sp,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.6).sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Your daily sanctuary is initialized. Habits are seeded, timers are calibrated, and your privacy is preserved on-device.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BulletRow(text = "Private Offline Sanctuary — zero ads, zero trackers")
                BulletRow(text = "Tactile Spring Haptics & Intentional Micro-Steps")
                BulletRow(text = "Circadian Rhythm Alignment & Evening Peace")
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
                .height(54.dp)
        ) {
            Text(
                text = "Enter Sanctuary",
                style = FormaTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
private fun BulletRow(text: String) {
    val colors = FormaTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(colors.accent)
        )
        Text(
            text = text,
            style = FormaTheme.typography.bodyMedium,
            color = colors.textPrimary,
            fontSize = 13.sp
        )
    }
}
