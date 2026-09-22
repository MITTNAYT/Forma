package com.forma.app.ui.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.motion.formaPressEffect
import com.forma.app.domain.model.SubscriptionTier
import com.forma.app.domain.model.TierFeatureComparison
import com.forma.app.ui.settings.SettingsViewModel
import kotlinx.coroutines.launch

private val FounderGold = Color(0xFFD4AF37)
private val FounderGoldSoft = Color(0xFFFDF8EA)

/**
 * ProPaywallBottomSheet – Forma's Comprehensive 3-Tier Experience:
 * 1. Free Tier (Sanctuary Explorer)
 * 2. Monthly Pro ($4.99/mo with 7-Day Free Trial)
 * 3. Lifetime Founder ($49.99 one-time payment)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProPaywallBottomSheet(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val colors = FormaTheme.colors
    val currentTier by viewModel.currentTier.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var selectedTier by remember(currentTier) {
        mutableStateOf(if (currentTier == SubscriptionTier.FREE) SubscriptionTier.MONTHLY_PRO else currentTier)
    }
    var isPurchasing by remember { mutableStateOf(false) }
    var showComparisonMatrix by remember { mutableStateOf(false) }

    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by shimmerTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(colors.border)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // ── 1. Top Bar & Close ──────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accentSoft)
                        .border(1.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SANCTUARY MEMBERSHIP",
                            style = FormaTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            letterSpacing = 1.4.sp,
                            fontSize = 10.5.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── 2. Editorial Headline & Subtitle ─────────────────────
            Text(
                text = if (isPro) "Your Sanctuary Is Unlocked" else "Elevate Your\nDaily Rhythm",
                style = FormaTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                letterSpacing = (-0.6).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isPro)
                    "You have active access as ${currentTier.title}. Every ritual, AI schedule, and soundscape is ready."
                else
                    "Your daily habits and timeline are free forever. Upgrade to Pro or Lifetime for AI synthesis, biometric locks, and acoustic soundscapes.",
                style = FormaTheme.typography.bodyMedium,
                color = colors.textSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── 3. 3-Tier Selector Cards ─────────────────────────────
            Text(
                text = "CHOOSE YOUR PLAN",
                style = FormaTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textTertiary,
                letterSpacing = 1.3.sp,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Free Tier Card
                TierSelectionCard(
                    tier = SubscriptionTier.FREE,
                    isSelected = selectedTier == SubscriptionTier.FREE,
                    isCurrent = currentTier == SubscriptionTier.FREE,
                    headline = "Free Forever",
                    priceTag = "$0",
                    period = "",
                    subtitle = "5 active habits · Basic timeline · Offline SQLite",
                    onSelect = { selectedTier = SubscriptionTier.FREE }
                )

                // Monthly Pro Card
                TierSelectionCard(
                    tier = SubscriptionTier.MONTHLY_PRO,
                    isSelected = selectedTier == SubscriptionTier.MONTHLY_PRO,
                    isCurrent = currentTier == SubscriptionTier.MONTHLY_PRO,
                    headline = "Forma Pro",
                    priceTag = "$4.99",
                    period = "/ month",
                    subtitle = "7-Day Free Trial · AI Studio · Soundscapes · Cloud Sync",
                    badge = "MOST POPULAR",
                    onSelect = { selectedTier = SubscriptionTier.MONTHLY_PRO }
                )

                // Lifetime Founder Card
                TierSelectionCard(
                    tier = SubscriptionTier.LIFETIME_FOUNDER,
                    isSelected = selectedTier == SubscriptionTier.LIFETIME_FOUNDER,
                    isCurrent = currentTier == SubscriptionTier.LIFETIME_FOUNDER,
                    headline = "Forma Founder",
                    priceTag = "$49.99",
                    period = "one-time",
                    subtitle = "Pay once · Lifetime access · Founder Gold badge",
                    badge = "SAVE 70%",
                    accentColor = FounderGold,
                    onSelect = { selectedTier = SubscriptionTier.LIFETIME_FOUNDER }
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ── 4. Key Highlights for Selected Tier ──────────────────
            Text(
                text = "INCLUDED IN ${selectedTier.title.uppercase()}",
                style = FormaTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textTertiary,
                letterSpacing = 1.3.sp,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (selectedTier) {
                    SubscriptionTier.FREE -> {
                        ProFeatureBullet("Up to 5 active daily rituals with streak tracking")
                        ProFeatureBullet("Clean chronological timeline & day planning")
                        ProFeatureBullet("Signature Matcha & Oat serene visual theme")
                        ProFeatureBullet("Local encrypted database with offline JSON export")
                    }
                    SubscriptionTier.MONTHLY_PRO -> {
                        ProFeatureBullet("Everything in Free, plus unlimited daily rituals")
                        ProFeatureBullet("Gemini 1.5 Flash AI Day Studio schedule synthesis")
                        ProFeatureBullet("Circadian Energy Wave chronotype alignment")
                        ProFeatureBullet("Biometric Sanctuary Lock & App Switcher privacy masking")
                        ProFeatureBullet("Procedural acoustic soundscapes (432Hz bowl, rain, alpha waves)")
                        ProFeatureBullet("Cross-device sync powered by Clerk authentication")
                    }
                    SubscriptionTier.LIFETIME_FOUNDER -> {
                        ProFeatureBullet("All Forma Pro capabilities permanently (no recurring bills)")
                        ProFeatureBullet("Exclusive Founder Gold emblem on profile & Intentional Circles")
                        ProFeatureBullet("VIP priority access to future AI models and acoustic stems")
                        ProFeatureBullet("Lifetime updates & highest priority cloud sync")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 5. Expandable Feature Matrix ────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceVariant.copy(alpha = 0.4f))
                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable { showComparisonMatrix = !showComparisonMatrix }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showComparisonMatrix) "Hide Plan Comparison" else "Compare All 3 Plans",
                        style = FormaTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        fontSize = 13.sp
                    )
                    Icon(
                        imageVector = if (showComparisonMatrix) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = showComparisonMatrix) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.background)
                        .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ComparisonRow("Active Habits", "5 max", "Unlimited", "Unlimited")
                    ComparisonRow("Gemini AI Studio", "—", "✓ Included", "✓ Priority")
                    ComparisonRow("Circadian Wave", "—", "✓ Included", "✓ Included")
                    ComparisonRow("Biometric Lock", "—", "✓ Included", "✓ Included")
                    ComparisonRow("Acoustic Audio", "Basic", "All 5+ stems", "All 5+ stems")
                    ComparisonRow("Clerk Cloud Sync", "—", "✓ Included", "✓ Included")
                    ComparisonRow("Founder Gold Badge", "—", "—", "✓ Exclusive")
                    ComparisonRow("Billing Mode", "Free", "$4.99 / mo", "$49.99 once")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 6. Dynamic CTA Action Button ────────────────────────
            val isCurrentSelection = currentTier == selectedTier

            val ctaButtonText = when {
                isPurchasing -> "Connecting to Google Play..."
                isCurrentSelection -> "Current Active Plan"
                selectedTier == SubscriptionTier.FREE -> "Continue with Free Sanctuary"
                selectedTier == SubscriptionTier.MONTHLY_PRO -> "Start 7-Day Free Trial — $4.99/mo"
                selectedTier == SubscriptionTier.LIFETIME_FOUNDER -> "Unlock Lifetime Founder — $49.99"
                else -> "Upgrade Sanctuary"
            }

            val isCtaEnabled = !isPurchasing && !isCurrentSelection

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(
                        elevation = if (isCtaEnabled) 8.dp else 0.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = if (selectedTier == SubscriptionTier.LIFETIME_FOUNDER) FounderGold.copy(alpha = 0.25f) else colors.accent.copy(alpha = 0.25f),
                        spotColor = if (selectedTier == SubscriptionTier.LIFETIME_FOUNDER) FounderGold.copy(alpha = 0.4f) else colors.accent.copy(alpha = 0.4f)
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        when {
                            !isCtaEnabled -> colors.surfaceVariant
                            selectedTier == SubscriptionTier.LIFETIME_FOUNDER -> FounderGold
                            selectedTier == SubscriptionTier.MONTHLY_PRO -> colors.accent
                            else -> colors.surfaceVariant
                        }
                    )
                    .clickable(enabled = isCtaEnabled) {
                        coroutineScope.launch {
                            isPurchasing = true
                            when (selectedTier) {
                                SubscriptionTier.MONTHLY_PRO -> viewModel.purchaseMonthlyPro()
                                SubscriptionTier.LIFETIME_FOUNDER -> viewModel.purchaseLifetimeFounder()
                                SubscriptionTier.FREE -> viewModel.setSubscriptionTier(SubscriptionTier.FREE)
                            }
                            isPurchasing = false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Shimmer overlay on active purchase buttons
                if (isCtaEnabled && selectedTier != SubscriptionTier.FREE) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.22f),
                                        Color.Transparent
                                    ),
                                    start = Offset(shimmerOffset * 500f - 200f, 0f),
                                    end = Offset(shimmerOffset * 500f + 200f, 54f)
                                )
                            )
                    )
                }

                if (isPurchasing) {
                    CircularProgressIndicator(
                        color = colors.onAccent,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = ctaButtonText,
                        style = FormaTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isCtaEnabled) {
                            if (selectedTier == SubscriptionTier.LIFETIME_FOUNDER) Color(0xFF2C2411) else colors.onAccent
                        } else {
                            colors.textSecondary
                        },
                        fontSize = 15.sp,
                        letterSpacing = 0.3.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── 7. Trust Row & Restore Action ─────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Google Play verified checkout  ·  Clerk account sync",
                    style = FormaTheme.typography.bodySmall,
                    color = colors.textTertiary,
                    fontSize = 11.sp
                )
            }

            TextButton(
                onClick = { viewModel.restorePurchases() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Restore Purchases",
                    style = FormaTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun TierSelectionCard(
    tier: SubscriptionTier,
    isSelected: Boolean,
    isCurrent: Boolean,
    headline: String,
    priceTag: String,
    period: String,
    subtitle: String,
    badge: String? = null,
    accentColor: Color? = null,
    onSelect: () -> Unit
) {
    val colors = FormaTheme.colors
    val activeColor = accentColor ?: colors.accent

    val cardBorderColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else colors.border.copy(alpha = 0.6f),
        label = "tierBorder"
    )

    val cardBgColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (accentColor != null) FounderGoldSoft.copy(alpha = 0.35f) else colors.accentSoft.copy(alpha = 0.5f)
        } else {
            colors.surfaceVariant.copy(alpha = 0.35f)
        },
        label = "tierBg"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBgColor)
            .border(if (isSelected) 1.8.dp else 1.dp, cardBorderColor, RoundedCornerShape(18.dp))
            .formaPressEffect(targetScale = 0.985f, onClick = onSelect)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, if (isSelected) activeColor else colors.textTertiary, CircleShape)
                            .background(if (isSelected) activeColor else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (accentColor != null) Color(0xFF2C2411) else colors.onAccent)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = headline,
                        style = FormaTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 15.sp
                    )

                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.accent.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                fontWeight = FontWeight.Bold,
                                color = colors.accent,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(activeColor)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = badge,
                            fontWeight = FontWeight.Bold,
                            color = if (accentColor != null) Color(0xFF2C2411) else colors.onAccent,
                            fontSize = 9.5.sp,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = priceTag,
                    style = FormaTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontSize = 24.sp
                )
                if (period.isNotBlank()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = period,
                        style = FormaTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = FormaTheme.typography.bodySmall,
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun ProFeatureBullet(text: String) {
    val colors = FormaTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(16.dp)
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

@Composable
private fun ComparisonRow(
    feature: String,
    free: String,
    pro: String,
    founder: String
) {
    val colors = FormaTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = feature,
            style = FormaTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary,
            fontSize = 12.sp,
            modifier = Modifier.weight(1.3f)
        )
        Text(
            text = free,
            style = FormaTheme.typography.bodySmall,
            color = colors.textTertiary,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.9f)
        )
        Text(
            text = pro,
            style = FormaTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.accent,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = founder,
            style = FormaTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = FounderGold,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
    }
}
