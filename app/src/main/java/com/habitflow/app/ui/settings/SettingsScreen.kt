package com.habitflow.app.ui.settings

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.habitflow.app.core.designsystem.motion.formaPressEffect
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.domain.repository.DarkModeOption
import com.habitflow.app.domain.repository.PaletteFamily
import com.habitflow.app.ui.settings.components.ProPaywallBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToHabits: () -> Unit,
    onNavigateToStats: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val colors = NotionTheme.colors
    val context = LocalContext.current

    val paletteFamily by viewModel.paletteFamily.collectAsState()
    val darkModeOption by viewModel.darkModeOption.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    val userName by viewModel.userName.collectAsState()

    var showPaywall by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showZenSummary by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var hapticsEnabled by remember { mutableStateOf(true) }
    var morningReminderEnabled by remember { mutableStateOf(true) }
    var eveningReminderEnabled by remember { mutableStateOf(true) }
    var selectedAmbientSound by remember { mutableStateOf(com.habitflow.app.core.audio.AmbientSound.RAIN) }
    var isAmbientPlaying by remember { mutableStateOf(false) }
    var ambientSleepTimerMinutes by remember { androidx.compose.runtime.mutableIntStateOf(30) }
    var zenSummaryData by remember { mutableStateOf<com.habitflow.app.ui.mindfulness.ZenSummaryData?>(null) }

    val userInitial = userName.trim().take(1).uppercase().ifBlank { "A" }
    val haptic = LocalHapticFeedback.current

    androidx.compose.runtime.LaunchedEffect(showZenSummary) {
        if (showZenSummary) {
            zenSummaryData = viewModel.getMonthlyZenSummary()
        }
    }

    Scaffold(containerColor = colors.background) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Page Header ─────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = "SETTINGS",
                        fontWeight = FontWeight.Bold,
                        color = colors.textTertiary,
                        letterSpacing = 1.5.sp,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Your Space",
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 28.sp,
                        letterSpacing = (-0.8).sp
                    )
                }
            }

            // ── Profile Card ────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.surface)
                        .formaPressEffect(targetScale = 0.98f) { showEditProfileDialog = true }
                        .padding(horizontal = 18.dp, vertical = 20.dp)
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
                            // Avatar with gradient ring
                            Box(modifier = Modifier.size(56.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    colors.accent,
                                                    colors.accentMuted
                                                )
                                            )
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .align(Alignment.Center)
                                        .clip(CircleShape)
                                        .background(colors.accentSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = userInitial,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.accent,
                                        fontSize = 22.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = userName.ifBlank { "Tap to set your name" },
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 17.sp,
                                    letterSpacing = (-0.2).sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isPro) colors.accent else colors.accentSoft
                                            )
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isPro) "PRO" else "FREE",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPro) colors.onAccent else colors.accent,
                                            fontSize = 9.sp,
                                            letterSpacing = 0.8.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Forma member",
                                        color = colors.textTertiary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(colors.accentSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit Name",
                                tint = colors.accent,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // ── Forma Pro Sanctuary Card (Organic placement below Profile) ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.surface)
                        .border(1.2.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                        .formaPressEffect(targetScale = 0.97f) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showPaywall = true
                        }
                        .padding(18.dp)
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
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.accentSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Spa,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Forma Pro",
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(colors.accent.copy(alpha = 0.14f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isPro) "LIFETIME ACTIVE" else "UPGRADE",
                                            fontWeight = FontWeight.Bold,
                                            color = colors.accent,
                                            fontSize = 9.5.sp,
                                            letterSpacing = 0.6.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (isPro) "Full sanctuary unlocked · AI synthesis & deep metrics"
                                    else "AI day synthesis · Deep analytics · Custom themes",
                                    color = colors.textSecondary,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Section: Appearance ──────────────────────────────────────
            item {
                SettingsSectionHeader(label = "APPEARANCE", modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        // Display mode
                        Text(
                            text = "Display Mode",
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceVariant)
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(
                                Triple(DarkModeOption.LIGHT, "Light", Icons.Rounded.LightMode),
                                Triple(DarkModeOption.DARK, "Dark", Icons.Rounded.DarkMode),
                                Triple(DarkModeOption.SYSTEM, "Auto", Icons.Rounded.PhoneAndroid)
                            ).forEach { (option, label, icon) ->
                                val isSelected = darkModeOption == option
                                val bg by animateColorAsState(
                                    targetValue = if (isSelected) colors.accent else Color.Transparent,
                                    label = "mode_bg"
                                )
                                val fg by animateColorAsState(
                                    targetValue = if (isSelected) colors.onAccent else colors.textSecondary,
                                    label = "mode_fg"
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(bg)
                                        .formaPressEffect(targetScale = 0.94f) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.setDarkModeOption(option)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = fg,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = fg,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Color palette
                        Text(
                            text = "Colour Palette",
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val paletteMeta = listOf(
                            Triple(PaletteFamily.MATCHA_OAT, "Matcha & Oat (Signature)",
                                listOf(Color(0xFF4E6542), Color(0xFFEDF3EB))),
                            Triple(PaletteFamily.COFFEE_CREAM, "Espresso & Warm Cream",
                                listOf(Color(0xFF2C221E), Color(0xFFEFE8DE))),
                            Triple(PaletteFamily.TERRACOTTA_SAND, "Terracotta & Desert Sand",
                                listOf(Color(0xFF8D5B4C), Color(0xFFF7EFE8))),
                            Triple(PaletteFamily.LAVENDER_MILK, "Lavender & Chamomile",
                                listOf(Color(0xFF5E548E), Color(0xFFEDE9F5))),
                            Triple(PaletteFamily.MONOCHROME, "Zen Monochrome",
                                listOf(Color(0xFF1E211E), Color(0xFFF0F2EE)))
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            paletteMeta.forEach { (palette, name, swatches) ->
                                val isChosen = paletteFamily == palette
                                val rowBg by animateColorAsState(
                                    targetValue = if (isChosen) colors.accentSoft else Color.Transparent,
                                    label = "palette_bg"
                                )
                                val borderColor = if (isChosen) colors.accent else colors.border.copy(alpha = 0.3f)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(rowBg)
                                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                        .formaPressEffect(targetScale = 0.97f) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.setPaletteFamily(palette)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Dual swatch (dark + light halves)
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, colors.border.copy(alpha = 0.4f), CircleShape)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(swatches[0])
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(11.dp)
                                                    .background(swatches[1])
                                                    .align(Alignment.BottomEnd)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = name,
                                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                            color = colors.textPrimary,
                                            fontSize = 13.sp
                                        )
                                    }
                                    if (isChosen) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = colors.accent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Section: Notifications & Feel ──────────────────────────
            item {
                SettingsSectionHeader(label = "NOTIFICATIONS & PROMPTS", modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SettingsToggleRow(
                            icon = Icons.Rounded.WbSunny,
                            title = "Morning Alignment (8:00 AM)",
                            subtitle = "Gentle intention setting to begin your day with focus",
                            checked = morningReminderEnabled,
                            onCheckedChange = {
                                morningReminderEnabled = it
                                viewModel.toggleMorningReminder(context, it)
                            }
                        )
                        SettingsDivider()
                        SettingsToggleRow(
                            icon = Icons.Rounded.Bedtime,
                            title = "Evening Sanctuary (9:30 PM)",
                            subtitle = "Reflect peacefully and rollover unfinished tasks",
                            checked = eveningReminderEnabled,
                            onCheckedChange = {
                                eveningReminderEnabled = it
                                viewModel.toggleEveningReminder(context, it)
                            }
                        )
                        SettingsDivider()
                        SettingsToggleRow(
                            icon = Icons.Rounded.Vibration,
                            title = "Spring Haptics",
                            subtitle = "Micro-vibrations on button taps and habit completions",
                            checked = hapticsEnabled,
                            onCheckedChange = { hapticsEnabled = it }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Section: Background Ambient Soundscapes ─────────────────
            item {
                SettingsSectionHeader(label = "BACKGROUND AMBIENT SOUNDSCAPES", modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isAmbientPlaying) colors.accentSoft else colors.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.GraphicEq,
                                        contentDescription = null,
                                        tint = if (isAmbientPlaying) colors.accent else colors.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = selectedAmbientSound.displayName,
                                        style = NotionTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = selectedAmbientSound.description,
                                        style = NotionTheme.typography.bodySmall,
                                        color = colors.textSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isAmbientPlaying) colors.accent else colors.accentSoft)
                                    .clickable {
                                        if (isAmbientPlaying) {
                                            isAmbientPlaying = false
                                            viewModel.stopBackgroundSound(context)
                                        } else {
                                            isAmbientPlaying = true
                                            viewModel.playBackgroundSound(context, selectedAmbientSound, ambientSleepTimerMinutes)
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isAmbientPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                        contentDescription = null,
                                        tint = if (isAmbientPlaying) Color.White else colors.accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isAmbientPlaying) "Stop" else "Play",
                                        style = NotionTheme.typography.labelSmall,
                                        color = if (isAmbientPlaying) Color.White else colors.accent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        SettingsDivider()
                        Spacer(modifier = Modifier.height(14.dp))

                        // Sound Selector Grid
                        Text(
                            text = "SOUNDSCAPE SELECTION",
                            style = NotionTheme.typography.labelSmall,
                            color = colors.textTertiary,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val availableSounds = listOf(
                            Triple(com.habitflow.app.core.audio.AmbientSound.RAIN, Icons.Rounded.WaterDrop, "Rain"),
                            Triple(com.habitflow.app.core.audio.AmbientSound.KYOTO_BELL, Icons.Rounded.Spa, "Bell"),
                            Triple(com.habitflow.app.core.audio.AmbientSound.ZEN_DRONE, Icons.Rounded.Waves, "Zen Drone"),
                            Triple(com.habitflow.app.core.audio.AmbientSound.FOREST_STREAM, Icons.Rounded.Forest, "Stream"),
                            Triple(com.habitflow.app.core.audio.AmbientSound.BROWN_NOISE, Icons.Rounded.Air, "Brown Flow"),
                            Triple(com.habitflow.app.core.audio.AmbientSound.WHITE_NOISE, Icons.Rounded.GraphicEq, "White Air")
                        )

                        // 3 rows of 2 or 2 rows of 3
                        val chunked = availableSounds.chunked(3)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            chunked.forEach { rowSounds ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowSounds.forEach { (sound, iconVector, label) ->
                                        val isSelected = selectedAmbientSound == sound
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) colors.accentSoft else colors.surfaceVariant.copy(alpha = 0.5f))
                                                .border(
                                                    1.dp,
                                                    if (isSelected) colors.accent else Color.Transparent,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    selectedAmbientSound = sound
                                                    if (isAmbientPlaying) {
                                                        viewModel.playBackgroundSound(context, sound, ambientSleepTimerMinutes)
                                                    }
                                                }
                                                .padding(vertical = 10.dp, horizontal = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = iconVector,
                                                    contentDescription = null,
                                                    tint = if (isSelected) colors.accent else colors.textSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = label,
                                                    style = NotionTheme.typography.labelSmall,
                                                    color = if (isSelected) colors.accent else colors.textPrimary,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sleep Timer Row
                        Text(
                            text = "SLEEP TIMER",
                            style = NotionTheme.typography.labelSmall,
                            color = colors.textTertiary,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(0 to "Continuous", 15 to "15m", 30 to "30m", 60 to "60m").forEach { (mins, label) ->
                                val isSelected = ambientSleepTimerMinutes == mins
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) colors.accentSoft else colors.surfaceVariant.copy(alpha = 0.5f))
                                        .border(
                                            1.dp,
                                            if (isSelected) colors.accent else Color.Transparent,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            ambientSleepTimerMinutes = mins
                                            if (isAmbientPlaying) {
                                                viewModel.playBackgroundSound(context, selectedAmbientSound, mins)
                                            }
                                        }
                                        .padding(vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = NotionTheme.typography.labelSmall,
                                        color = if (isSelected) colors.accent else colors.textPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Section: Data & Backup ──────────────────────────────────
            item {
                SettingsSectionHeader(label = "DATA & MINDFUL ARCHIVES", modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .padding(vertical = 4.dp)
                ) {
                    Column {
                        SettingsTapRow(
                            icon = Icons.Rounded.Spa,
                            title = "Monthly Zen Summary",
                            subtitle = "View your peace index, rituals & focus hours",
                            onClick = { showZenSummary = true }
                        )
                        SettingsDivider(indent = 50.dp)
                        SettingsTapRow(
                            icon = Icons.Rounded.Article,
                            title = "Export Markdown Journal",
                            subtitle = "Notion, Obsidian & notes-app ready",
                            onClick = { viewModel.exportMarkdown(context) }
                        )
                        SettingsDivider(indent = 50.dp)
                        SettingsTapRow(
                            icon = Icons.Rounded.Share,
                            title = "Export JSON Backup",
                            subtitle = "Full backup of rituals, reflections & timeline",
                            onClick = {
                                viewModel.exportFullBackup { json ->
                                    val sendIntent = android.content.Intent().apply {
                                        action = android.content.Intent.ACTION_SEND
                                        putExtra(android.content.Intent.EXTRA_TEXT, json)
                                        type = "application/json"
                                    }
                                    val shareIntent = android.content.Intent.createChooser(sendIntent, "Export HabitFlow Backup")
                                    context.startActivity(shareIntent)
                                }
                            }
                        )
                        SettingsDivider(indent = 50.dp)
                        SettingsTapRow(
                            icon = Icons.Rounded.AutoAwesome,
                            title = "Restore from JSON Backup",
                            subtitle = "Paste and restore existing HabitFlow database",
                            onClick = { showImportDialog = true }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Section: About ──────────────────────────────────────────
            item {
                SettingsSectionHeader(label = "ABOUT", modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .padding(vertical = 4.dp)
                ) {
                    Column {
                        SettingsTapRow(
                            icon = Icons.Rounded.HelpOutline,
                            title = "Help & Support",
                            subtitle = "FAQs, contact, and feedback",
                            onClick = {}
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Version label ───────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "HabitFlow  ·  v2.0",
                        color = colors.textTertiary,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }

    // ── Edit Profile Dialog ─────────────────────────────────────────
    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(userName) }
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "Your Name",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontSize = 18.sp
                )
            },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Name", color = colors.textSecondary) },
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempName.isNotBlank()) viewModel.setUserName(tempName)
                    showEditProfileDialog = false
                }) {
                    Text("Save", color = colors.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = {
                showImportDialog = false
                importJsonText = ""
            },
            title = {
                Text(
                    text = "Restore HabitFlow Database",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Paste your exported JSON backup text below to restore your habits, completions, and reflections.",
                        style = NotionTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        label = { Text("Backup JSON", color = colors.textSecondary) },
                        maxLines = 8,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (importJsonText.isNotBlank()) {
                        viewModel.restoreFullBackup(importJsonText) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) {
                                showImportDialog = false
                                importJsonText = ""
                            }
                        }
                    }
                }) {
                    Text("Restore", color = colors.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportDialog = false
                    importJsonText = ""
                }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showZenSummary) {
        zenSummaryData?.let { summary ->
            com.habitflow.app.ui.mindfulness.MonthlyZenSummarySheet(
                summaryData = summary,
                onDismiss = { showZenSummary = false }
            )
        }
    }

    if (showPaywall) {
        ProPaywallBottomSheet(onDismiss = { showPaywall = false })
    }
}

// ── Shared small components ──────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(label: String, modifier: Modifier = Modifier) {
    val colors = NotionTheme.colors
    Text(
        text = label,
        fontWeight = FontWeight.Bold,
        color = colors.textTertiary,
        fontSize = 10.sp,
        letterSpacing = 1.6.sp,
        modifier = modifier
    )
}

@Composable
private fun SettingsDivider(indent: Dp = 0.dp) {
    val colors = NotionTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent)
            .height(0.5.dp)
            .background(colors.border.copy(alpha = 0.35f))
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = NotionTheme.colors
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onAccent,
                checkedTrackColor = colors.accent,
                uncheckedThumbColor = colors.textTertiary,
                uncheckedTrackColor = colors.surfaceVariant
            )
        )
    }
}

@Composable
private fun SettingsTapRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = NotionTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .formaPressEffect(targetScale = 0.97f) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(16.dp)
        )
    }
}
