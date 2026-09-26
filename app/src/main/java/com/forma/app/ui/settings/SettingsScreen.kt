package com.forma.app.ui.settings

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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.motion.formaPressEffect
import com.forma.app.domain.model.AuthState
import com.forma.app.domain.model.SubscriptionTier
import com.forma.app.domain.repository.DarkModeOption
import com.forma.app.domain.repository.PaletteFamily
import com.forma.app.ui.auth.AuthViewModel
import com.forma.app.ui.auth.components.AuthModalBottomSheet
import com.forma.app.ui.settings.components.EncryptedVaultDialog
import com.forma.app.ui.settings.components.LegalPolicyDialog
import com.forma.app.ui.settings.components.LegalPolicyType
import com.forma.app.ui.settings.components.NecessaryDataConsentBanner
import com.forma.app.ui.settings.components.ProPaywallBottomSheet
import com.forma.app.ui.settings.components.VaultDialogMode

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
    val currentTier by viewModel.currentTier.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val isBiometricLockEnabled by viewModel.isBiometricLockEnabled.collectAsState()
    val isPrivacyMaskingEnabled by viewModel.isPrivacyMaskingEnabled.collectAsState()
    val hasConsentedToDataAndCookies by viewModel.hasConsentedToDataAndCookies.collectAsState()

    val authState by authViewModel.authState.collectAsState()
    val isSyncing by authViewModel.isSyncing.collectAsState()
    val lastSyncedAt by authViewModel.lastSyncedAt.collectAsState()

    var showPaywall by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAuthBottomSheet by remember { mutableStateOf(false) }
    var vaultDialogMode by remember { mutableStateOf<VaultDialogMode?>(null) }
    var selectedPolicyType by remember { mutableStateOf<LegalPolicyType?>(null) }
    var showHelpSupportSheet by remember { mutableStateOf(false) }
    var showSanctuaryTour by remember { mutableStateOf(false) }

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
                        .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 28.sp,
                        letterSpacing = (-0.8).sp
                    )
                }
            }


            // ═══════════════════════════════════════════════════════════
            // ── GROUP 1: PROFILE & SYNC ───────────────────────────────
            // ═══════════════════════════════════════════════════════════
            item {
                SettingsSectionHeader(
                    label = "PROFILE",
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
                )
            }
            item {
                SettingsCard {
                    // Profile Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .formaPressEffect(targetScale = 0.98f) { showEditProfileDialog = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(modifier = Modifier.size(48.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(colors.accent, colors.accentMuted)
                                        )
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .align(Alignment.Center)
                                    .clip(CircleShape)
                                    .background(colors.accentSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userInitial,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userName.ifBlank { "Tap to set name" },
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 16.sp,
                                letterSpacing = (-0.2).sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(
                                            when (currentTier) {
                                                SubscriptionTier.LIFETIME_FOUNDER -> Color(0xFFD4AF37)
                                                SubscriptionTier.MONTHLY_PRO -> colors.accent
                                                SubscriptionTier.FREE -> colors.surfaceVariant
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when (currentTier) {
                                            SubscriptionTier.LIFETIME_FOUNDER -> "FOUNDER"
                                            SubscriptionTier.MONTHLY_PRO -> "PRO"
                                            SubscriptionTier.FREE -> "FREE"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = when (currentTier) {
                                            SubscriptionTier.LIFETIME_FOUNDER -> Color(0xFF2C2411)
                                            SubscriptionTier.MONTHLY_PRO -> colors.onAccent
                                            SubscriptionTier.FREE -> colors.textSecondary
                                        },
                                        fontSize = 9.sp,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                            }
                        }

                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Profile",
                            tint = colors.textTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    SettingsDivider()

                    // Membership Row
                    SettingsTapRow(
                        icon = Icons.Rounded.Spa,
                        title = when (currentTier) {
                            SubscriptionTier.LIFETIME_FOUNDER -> "Lifetime Founder Pass"
                            SubscriptionTier.MONTHLY_PRO -> "Pro Active"
                            SubscriptionTier.FREE -> "Upgrade to Pro"
                        },
                        subtitle = when (currentTier) {
                            SubscriptionTier.LIFETIME_FOUNDER -> "All features unlocked"
                            SubscriptionTier.MONTHLY_PRO -> "Subscription active"
                            SubscriptionTier.FREE -> "AI synthesis, deep metrics, unlimited rituals"
                        },
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showPaywall = true
                        }
                    )

                    SettingsDivider()

                    // Cloud Sync Row
                    when (val state = authState) {
                        is AuthState.Authenticated -> {
                            val user = state.user
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    SettingsIconBox(icon = Icons.Rounded.CloudDone)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Cloud Sync",
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textPrimary,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = user.email ?: "Connected",
                                            color = colors.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.accentSoft)
                                            .formaPressEffect(targetScale = 0.95f) { authViewModel.syncNow() }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isSyncing) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(10.dp),
                                                    strokeWidth = 1.5.dp,
                                                    color = colors.accent
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Text(
                                                text = if (isSyncing) "Syncing" else "Sync",
                                                fontWeight = FontWeight.Bold,
                                                color = colors.accent,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(
                                        onClick = { authViewModel.signOut() },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                    ) {
                                        Text(
                                            text = "Sign Out",
                                            color = colors.textTertiary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                        else -> {
                            SettingsTapRow(
                                icon = Icons.Rounded.CloudSync,
                                title = "Cloud Sync",
                                subtitle = "Sign in to back up across devices",
                                onClick = { showAuthBottomSheet = true }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ═══════════════════════════════════════════════════════════
            // ── GROUP 2: PREFERENCES ──────────────────────────────────
            // ═══════════════════════════════════════════════════════════
            item {
                SettingsSectionHeader(
                    label = "PREFERENCES",
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
                )
            }
            item {
                SettingsCard {
                    // Display Mode
                    Text(
                        text = "Display Mode",
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        fontSize = 14.sp
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
                                    .height(36.dp)
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
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = fg,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Colour Palette
                    Text(
                        text = "Colour Palette",
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val paletteMeta = listOf(
                        Triple(PaletteFamily.MATCHA_OAT, "Matcha & Oat",
                            listOf(Color(0xFF4E6542), Color(0xFFEDF3EB))),
                        Triple(PaletteFamily.COFFEE_CREAM, "Espresso & Cream",
                            listOf(Color(0xFF2C221E), Color(0xFFEFE8DE))),
                        Triple(PaletteFamily.TERRACOTTA_SAND, "Terracotta & Sand",
                            listOf(Color(0xFFC58A24), Color(0xFFFBF3E0))),
                        Triple(PaletteFamily.LAVENDER_MILK, "Lavender & Chamomile",
                            listOf(Color(0xFF5E548E), Color(0xFFEDE9F5))),
                        Triple(PaletteFamily.MONOCHROME, "Monochrome",
                            listOf(Color(0xFF1E211E), Color(0xFFF0F2EE)))
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        paletteMeta.forEach { (palette, name, swatches) ->
                            val isChosen = paletteFamily == palette
                            val cardBg by animateColorAsState(
                                targetValue = if (isChosen) colors.accentSoft else colors.surfaceVariant.copy(alpha = 0.5f),
                                label = "pal_card_bg"
                            )
                            val borderColor = if (isChosen) colors.accent else colors.border.copy(alpha = 0.5f)

                            Box(
                                modifier = Modifier
                                    .width(135.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(cardBg)
                                    .border(if (isChosen) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
                                    .formaPressEffect(targetScale = 0.94f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setPaletteFamily(palette)
                                    }
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Dual Swatch Circles
                                        Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(swatches[0])
                                                    .border(1.5.dp, colors.surface, CircleShape)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(swatches[1])
                                                    .border(1.5.dp, colors.surface, CircleShape)
                                            )
                                        }

                                        if (isChosen) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .background(colors.accent),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Check,
                                                    contentDescription = null,
                                                    tint = colors.onAccent,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = name,
                                        style = FormaTheme.typography.bodySmall,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isChosen) colors.textPrimary else colors.textSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    SettingsDivider(indent = 0.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Notifications & Haptics
                    SettingsToggleRow(
                        icon = Icons.Rounded.WbSunny,
                        title = "Morning Nudge",
                        subtitle = "8:00 AM intention reminder",
                        checked = morningReminderEnabled,
                        onCheckedChange = {
                            morningReminderEnabled = it
                            viewModel.toggleMorningReminder(context, it)
                        }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.Bedtime,
                        title = "Evening Nudge",
                        subtitle = "9:30 PM reflection reminder",
                        checked = eveningReminderEnabled,
                        onCheckedChange = {
                            eveningReminderEnabled = it
                            viewModel.toggleEveningReminder(context, it)
                        }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.Vibration,
                        title = "Haptics",
                        subtitle = "Micro-vibrations on interactions",
                        checked = hapticsEnabled,
                        onCheckedChange = { hapticsEnabled = it }
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ═══════════════════════════════════════════════════════════
            // ── GROUP 3: PRIVACY & ABOUT ──────────────────────────────
            // ═══════════════════════════════════════════════════════════
            item {
                SettingsSectionHeader(
                    label = "PRIVACY & DATA",
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
                )
            }
            item {
                SettingsCard {
                    SettingsToggleRow(
                        icon = Icons.Rounded.Spa,
                        title = "Biometric Lock",
                        subtitle = "Require fingerprint or face unlock",
                        checked = isBiometricLockEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.setBiometricLockEnabled(enabled)
                        }
                    )
                    SettingsDivider()
                    SettingsTapRow(
                        icon = Icons.AutoMirrored.Rounded.Article,
                        title = "Export Journal",
                        subtitle = "Markdown format for Notion & Obsidian",
                        onClick = { viewModel.exportMarkdown(context) }
                    )
                    SettingsDivider()
                    SettingsTapRow(
                        icon = Icons.Rounded.Bookmark,
                        title = "Export Encrypted Vault",
                        subtitle = "AES-256 encrypted archive",
                        onClick = { vaultDialogMode = VaultDialogMode.ENCRYPT_EXPORT }
                    )
                    SettingsDivider()
                    SettingsTapRow(
                        icon = Icons.Rounded.TrackChanges,
                        title = "Restore from Vault",
                        subtitle = "Import encrypted backup",
                        onClick = { vaultDialogMode = VaultDialogMode.DECRYPT_RESTORE }
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Legal & Help ─────────────────────────────────────────
            item {
                SettingsSectionHeader(
                    label = "ABOUT",
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
                )
            }
            item {
                SettingsCard {
                    SettingsTapRow(
                        icon = Icons.Rounded.Shield,
                        title = "Privacy Policy",
                        subtitle = "How your data is handled",
                        onClick = { selectedPolicyType = LegalPolicyType.PRIVACY_POLICY }
                    )
                    SettingsDivider()
                    SettingsTapRow(
                        icon = Icons.Rounded.Description,
                        title = "Terms & Conditions",
                        subtitle = "Usage agreement",
                        onClick = { selectedPolicyType = LegalPolicyType.TERMS_AND_CONDITIONS }
                    )
                    SettingsDivider()
                    SettingsTapRow(
                        icon = Icons.Rounded.Payments,
                        title = "Refund Policy",
                        subtitle = "Subscription & founder pass",
                        onClick = { selectedPolicyType = LegalPolicyType.REFUND_POLICY }
                    )
                    SettingsDivider()
                    SettingsTapRow(
                        icon = Icons.AutoMirrored.Rounded.HelpOutline,
                        title = "Help & Support",
                        subtitle = "FAQ, tour replay, community",
                        onClick = { showHelpSupportSheet = true }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Version Footer ───────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Forma v2.0",
                        color = colors.textTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Private by design. Offline-first.",
                        color = colors.textTertiary.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }

    // ── Edit Profile Dialog ──────────────────────────────────────
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Name", color = colors.textSecondary) },
                        singleLine = true,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            cursorColor = colors.accent
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Stored locally on your device only.",
                            color = colors.textTertiary,
                            fontSize = 10.5.sp
                        )
                    }
                }
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

    // ── Legal Policy Dialog ──────────────────────────────────────
    selectedPolicyType?.let { policyType ->
        LegalPolicyDialog(
            policyType = policyType,
            onDismiss = { selectedPolicyType = null }
        )
    }

    // ── Encrypted Vault Dialog ──────────────────────────────────
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

    if (showHelpSupportSheet) {
        com.forma.app.ui.settings.components.HelpSupportSheet(
            onDismiss = { showHelpSupportSheet = false },
            onReplayTour = {
                showHelpSupportSheet = false
                showSanctuaryTour = true
            }
        )
    }

    if (showSanctuaryTour) {
        com.forma.app.ui.settings.components.SanctuaryWalkthroughSheet(
            onDismiss = { showSanctuaryTour = false }
        )
    }
}

// ── Shared Components ──────────────────────────────────────────────

/** Consistent card wrapper for all settings groups */
@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    val colors = FormaTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsSectionHeader(label: String, modifier: Modifier = Modifier) {
    val colors = FormaTheme.colors
    Text(
        text = label,
        fontWeight = FontWeight.Bold,
        color = colors.accent,
        fontSize = 11.sp,
        letterSpacing = 1.2.sp,
        modifier = modifier
    )
}

@Composable
private fun SettingsDivider(indent: Dp = 48.dp) {
    val colors = FormaTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent)
            .height(0.5.dp)
            .background(colors.border.copy(alpha = 0.3f))
    )
}

@Composable
private fun SettingsIconBox(
    icon: ImageVector,
    tint: Color = FormaTheme.colors.accent
) {
    val colors = FormaTheme.colors
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
            tint = tint,
            modifier = Modifier.size(17.dp)
        )
    }
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
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            SettingsIconBox(icon = icon)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    letterSpacing = (-0.15).sp
                )
                Text(
                    text = subtitle,
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCheckedChange(it)
            },
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
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .formaPressEffect(targetScale = 0.98f) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            SettingsIconBox(icon = icon)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    letterSpacing = (-0.15).sp
                )
                Text(
                    text = subtitle,
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
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
