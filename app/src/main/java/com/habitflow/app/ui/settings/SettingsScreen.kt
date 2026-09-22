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
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.Vibration
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
import com.habitflow.app.core.designsystem.FormaTheme
import com.habitflow.app.domain.repository.DarkModeOption
import com.habitflow.app.domain.repository.PaletteFamily
import com.habitflow.app.ui.settings.components.EncryptedVaultDialog
import com.habitflow.app.ui.settings.components.ProPaywallBottomSheet
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material3.CircularProgressIndicator
import com.habitflow.app.domain.model.AuthState
import com.habitflow.app.ui.auth.AuthViewModel
import com.habitflow.app.ui.auth.components.AuthModalBottomSheet
import com.habitflow.app.ui.settings.components.VaultDialogMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToHabits: () -> Unit,
    onNavigateToStats: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val colors = FormaTheme.colors
    val context = LocalContext.current

    val paletteFamily by viewModel.paletteFamily.collectAsState()
    val darkModeOption by viewModel.darkModeOption.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    val userName by viewModel.userName.collectAsState()

    var showPaywall by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAuthBottomSheet by remember { mutableStateOf(false) }
    var vaultDialogMode by remember { mutableStateOf<VaultDialogMode?>(null) }
    var hapticsEnabled by remember { mutableStateOf(true) }
    var morningReminderEnabled by remember { mutableStateOf(true) }
    var eveningReminderEnabled by remember { mutableStateOf(true) }

    val userInitial = userName.trim().take(1).uppercase().ifBlank { "A" }
    val haptic = LocalHapticFeedback.current

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
                SettingsSectionHeader(label = "GENTLE NUDGES & PROMPTS", modifier = Modifier.padding(horizontal = 24.dp))
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
                            title = "Morning Alignment Nudge (8:00 AM)",
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
                            title = "Evening Sanctuary Nudge (9:30 PM)",
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

            // ── Section: Sync Sanctuary (Firebase Google & Email Sync) ──
            item {
                SettingsSectionHeader(label = "SYNC SANCTUARY", modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                val authState by authViewModel.authState.collectAsState()
                val isSyncing by authViewModel.isSyncing.collectAsState()
                val lastSyncedAt by authViewModel.lastSyncedAt.collectAsState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    when (val state = authState) {
                        is AuthState.Authenticated -> {
                            val user = state.user
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(colors.accentSoft),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.CloudDone,
                                                contentDescription = "Synced",
                                                tint = colors.accent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = user.displayName ?: "Sanctuary Member",
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.textPrimary,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = user.email ?: "Cloud Sync Active",
                                                color = colors.textSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    // Sync Now Button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(colors.accentSoft)
                                            .formaPressEffect(targetScale = 0.95f) {
                                                authViewModel.syncNow()
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isSyncing) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(11.dp),
                                                    strokeWidth = 1.5.dp,
                                                    color = colors.accent
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                            }
                                            Text(
                                                text = if (isSyncing) "Syncing..." else "Sync Now",
                                                fontWeight = FontWeight.Bold,
                                                color = colors.accent,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                SettingsDivider()
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (lastSyncedAt != null) "Last backed up to cloud" else "Offline-first local cache active",
                                        color = colors.textTertiary,
                                        fontSize = 11.sp
                                    )
                                    TextButton(
                                        onClick = { authViewModel.signOut() },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = "Sign Out",
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                        else -> {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(colors.accentSoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CloudSync,
                                            contentDescription = "Cloud Sync",
                                            tint = colors.accent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Cloud Backup & Multi-Device Sync",
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textPrimary,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Sign in with Google or Email to sync rituals across devices.",
                                            color = colors.textSecondary,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                com.habitflow.app.core.designsystem.component.FormaButton(
                                    text = "Connect Google / Email",
                                    onClick = { showAuthBottomSheet = true },
                                    style = com.habitflow.app.core.designsystem.component.FormaButtonStyle.PRIMARY,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Section: Data & Mindful Archives ───────────────────────
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
                            icon = Icons.Rounded.Article,
                            title = "Export Markdown Journal",
                            subtitle = "Notion, Obsidian & notes-app ready",
                            onClick = { viewModel.exportMarkdown(context) }
                        )
                        SettingsDivider(indent = 50.dp)
                        SettingsTapRow(
                            icon = Icons.Rounded.Bookmark,
                            title = "Export Encrypted Vault (.habitvault)",
                            subtitle = "Zero-Knowledge AES-256-GCM encrypted backup",
                            onClick = { vaultDialogMode = VaultDialogMode.ENCRYPT_EXPORT }
                        )
                        SettingsDivider(indent = 50.dp)
                        SettingsTapRow(
                            icon = Icons.Rounded.TrackChanges,
                            title = "Unlock & Restore Encrypted Vault",
                            subtitle = "Decrypt and restore with your master passphrase",
                            onClick = { vaultDialogMode = VaultDialogMode.DECRYPT_RESTORE }
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
                        text = "Forma  ·  v2.0",
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

    vaultDialogMode?.let { mode ->
        EncryptedVaultDialog(
            mode = mode,
            onDismiss = { vaultDialogMode = null },
            onExportWithPassword = { passphrase ->
                viewModel.exportEncryptedVault(context, passphrase) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    if (success) {
                        vaultDialogMode = null
                    }
                }
            },
            onRestoreWithPassword = { passphrase, payload ->
                viewModel.restoreEncryptedVault(passphrase, payload) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    if (success) {
                        vaultDialogMode = null
                    }
                }
            }
        )
    }

    val authUiMessage by authViewModel.uiMessage.collectAsState()
    androidx.compose.runtime.LaunchedEffect(authUiMessage) {
        authUiMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            authViewModel.clearMessage()
        }
    }

    if (showAuthBottomSheet) {
        AuthModalBottomSheet(
            viewModel = authViewModel,
            onDismiss = { showAuthBottomSheet = false }
        )
    }

    if (showPaywall) {
        ProPaywallBottomSheet(onDismiss = { showPaywall = false })
    }
}

// ── Shared small components ──────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(label: String, modifier: Modifier = Modifier) {
    val colors = FormaTheme.colors
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
    val colors = FormaTheme.colors
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
    val colors = FormaTheme.colors
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
    val colors = FormaTheme.colors
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
