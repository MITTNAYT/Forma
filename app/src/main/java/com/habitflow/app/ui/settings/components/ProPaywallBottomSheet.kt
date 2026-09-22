package com.habitflow.app.ui.settings.components

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
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Widgets
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
import com.habitflow.app.core.designsystem.FormaTheme
import com.habitflow.app.ui.settings.SettingsViewModel
import kotlinx.coroutines.launch

private val ChampagneGold = Color(0xFFC8A84B)

/**
 * ProPaywallBottomSheet – The redesigned Forma Pro experience.
 *
 * Professional, organized, and matching the serene Matcha & Oat design system.
 * Transparent one-time pricing, clearly categorized value pillars, and calm trust signals.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProPaywallBottomSheet(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val colors = FormaTheme.colors
    val isPro by viewModel.isPro.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isPurchasing by remember { mutableStateOf(false) }

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
            // ── 1. Header & Close Button ──────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Forma Pro Zen Pill
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
                            text = "FORMA PRO",
                            style = FormaTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            letterSpacing = 1.6.sp,
                            fontSize = 11.sp
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

            Spacer(modifier = Modifier.height(20.dp))

            // ── 2. Editorial Headline & Description ───────────────────
            Text(
                text = if (isPro) "You are on Forma Pro" else "Elevate Your\nDaily Rhythm",
                style = FormaTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                letterSpacing = (-0.6).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isPro)
                    "All Pro capabilities are active on your device. Thank you for cultivating mindful flow."
                else
                    "Your core rituals and timeline are free forever. Pro unlocks AI schedule synthesis, deep analytics, and unlimited aesthetic freedom.",
                style = FormaTheme.typography.bodyMedium,
                color = colors.textSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── 3. Organized Feature Pillars ─────────────────────────
            Text(
                text = "WHAT'S INCLUDED",
                style = FormaTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textTertiary,
                letterSpacing = 1.4.sp,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ProFeatureCard(
                    icon = Icons.Rounded.Psychology,
                    title = "AI Day Synthesis",
                    subtitle = "Generates mindful, realistic daily timelines balancing habits, energy flow, and commitments automatically.",
                    badge = "INTELLIGENCE"
                )

                ProFeatureCard(
                    icon = Icons.Rounded.Insights,
                    title = "Executive Analytics",
                    subtitle = "Monthly consistency heatmap, correlation matrices, streak momentum, and detailed focus investment breakdowns.",
                    badge = "DEEP METRICS"
                )

                ProFeatureCard(
                    icon = Icons.Rounded.Palette,
                    title = "Unlimited Sanctuary",
                    subtitle = "Unlock all curated visual themes (Espresso, Monochrome, Forest), 1,000+ custom ritual icons, and live widgets.",
                    badge = "AESTHETICS"
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ── 4. Transparent Lifetime Pricing Card ─────────────────
            if (!isPro) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.2.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIFETIME MEMBERSHIP",
                                style = FormaTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent,
                                letterSpacing = 1.2.sp,
                                fontSize = 11.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.accent)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "PAY ONCE",
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onAccent,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$19",
                                style = FormaTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 38.sp,
                                letterSpacing = (-1).sp
                            )
                            Text(
                                text = ".99",
                                style = FormaTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textSecondary,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "one-time · yours forever",
                                style = FormaTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "No subscriptions. No recurring charges. Free future updates.",
                            style = FormaTheme.typography.bodySmall,
                            color = colors.textTertiary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── 5. Primary CTA Action Button ─────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = colors.accent.copy(alpha = 0.2f),
                            spotColor = colors.accent.copy(alpha = 0.35f)
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.accent)
                        .clickable(enabled = !isPurchasing) {
                            coroutineScope.launch {
                                isPurchasing = true
                                viewModel.purchasePro()
                                isPurchasing = false
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Shimmer overlay
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

                    if (isPurchasing) {
                        CircularProgressIndicator(
                            color = colors.onAccent,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Unlock Lifetime Access — $19.99",
                            style = FormaTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onAccent,
                            fontSize = 15.sp,
                            letterSpacing = 0.3.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── 6. Trust Row & Restore Action ─────────────────────
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
                        text = "Google Play verified checkout  ·  Instant restore",
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
            } else {
                // Active Pro State Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.accentSoft)
                        .border(1.dp, colors.accent.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Lifetime Membership Active",
                            style = FormaTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/** An organized, beautifully styled Pro feature pillar card. */
@Composable
private fun ProFeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String
) {
    val colors = FormaTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.background)
            .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = FormaTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 14.sp
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.surfaceVariant.copy(alpha = 0.7f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            fontWeight = FontWeight.Bold,
                            color = colors.textTertiary,
                            fontSize = 9.sp,
                            letterSpacing = 0.8.sp
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
}
