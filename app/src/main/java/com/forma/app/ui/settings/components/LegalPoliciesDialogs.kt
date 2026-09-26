package com.forma.app.ui.settings.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.component.FormaButton
import com.forma.app.core.designsystem.component.FormaButtonStyle
import com.forma.app.core.designsystem.motion.formaPressEffect

enum class LegalPolicyType(val title: String, val subtitle: String, val icon: ImageVector) {
    PRIVACY_POLICY(
        title = "Privacy Policy",
        subtitle = "Data minimization & local-first storage",
        icon = Icons.Rounded.Shield
    ),
    TERMS_AND_CONDITIONS(
        title = "Terms & Conditions",
        subtitle = "Mindful productivity & fair use",
        icon = Icons.Rounded.Description
    ),
    REFUND_POLICY(
        title = "Refund Policy",
        subtitle = "Google Play subscriptions & Founder pass",
        icon = Icons.Rounded.Payments
    ),
    COOKIE_AND_CONSENT_POLICY(
        title = "Cookie & Necessary Data Policy",
        subtitle = "Zero tracking cookies, essential storage only",
        icon = Icons.Rounded.Cookie
    )
}

@Composable
fun LegalPolicyDialog(
    policyType: LegalPolicyType,
    onDismiss: () -> Unit
) {
    val colors = FormaTheme.colors

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            FormaButton(
                text = "Understood",
                onClick = onDismiss,
                style = FormaButtonStyle.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        },
        title = {
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = policyType.icon,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = policyType.title,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 17.sp
                        )
                        Text(
                            text = policyType.subtitle,
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = colors.textTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (policyType) {
                    LegalPolicyType.PRIVACY_POLICY -> PrivacyPolicyContent()
                    LegalPolicyType.TERMS_AND_CONDITIONS -> TermsContent()
                    LegalPolicyType.REFUND_POLICY -> RefundPolicyContent()
                    LegalPolicyType.COOKIE_AND_CONSENT_POLICY -> CookieConsentContent()
                }
            }
        },
        containerColor = colors.surface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun PrivacyPolicyContent() {
    val colors = FormaTheme.colors

    PolicySectionCard(
        title = "1. Strict Data Minimization",
        body = "Forma only collects and processes data strictly necessary for providing habit tracking, scheduling, and personal reflection. We do not require an email address or real name to use Forma locally. We collect zero location data, zero browsing telemetry, and zero device fingerprints."
    )

    PolicySectionCard(
        title = "2. Offline-First & Encrypted SQLite Storage",
        body = "All your habits, subtasks, micro-steps, streaks, daily reflections, and focus session records are stored directly on your physical device in an encrypted Room SQLite database. Your private thoughts and progress remain in your hands."
    )

    PolicySectionCard(
        title = "3. Zero Advertising or Tracking SDKs",
        body = "We do not integrate any third-party advertising SDKs, behavioral analytics, or tracking beacons. We will never sell, monetize, or broker your personal routine data to data brokers or advertising exchanges."
    )

    PolicySectionCard(
        title = "4. Optional Cloud Sync",
        body = "If you explicitly choose to connect an account for multi-device sync, only your encrypted habit entities and focus logs are synchronized to our secure, user-isolated cloud vault. You may disconnect or delete your cloud data at any time."
    )

    PolicySectionCard(
        title = "5. Complete Data Sovereignty",
        body = "You can export all your data anytime in human-readable Markdown or an AES-256 encrypted .habitvault file. Uninstalling Forma or clearing app data instantly deletes all local database tables."
    )
}

@Composable
private fun TermsContent() {
    val colors = FormaTheme.colors

    PolicySectionCard(
        title = "1. Acceptance of Terms",
        body = "By downloading, accessing, or using Forma, you agree to these Terms & Conditions. Forma is designed as a peaceful, intentional habit tracker and time-blocking tool."
    )

    PolicySectionCard(
        title = "2. User Digital Sovereignty",
        body = "You retain 100% intellectual property and ownership over all habits, journal reflections, and personal schedules you create in the app. Forma grants you a personal, non-transferable license to use the app for personal productivity."
    )

    PolicySectionCard(
        title = "3. Not Clinical or Medical Advice",
        body = "Forma provides self-reflection prompts, breathing exercises, and habit organization tools. It does not provide medical, psychological, psychiatric, or healthcare advice. Consult licensed medical professionals for health or clinical concerns."
    )

    PolicySectionCard(
        title = "4. Account & Passphrase Security",
        body = "If you utilize the Zero-Knowledge Encrypted Vault export feature, you are solely responsible for remembering your master passphrase. Because Forma uses client-side encryption, lost passphrases cannot be recovered by our team."
    )

    PolicySectionCard(
        title = "5. Service Updates & Changes",
        body = "We continually refine Forma to improve stability, aesthetics, and privacy. We reserve the right to modify features or policies with appropriate notice in-app or via our repository."
    )
}

@Composable
private fun RefundPolicyContent() {
    val colors = FormaTheme.colors

    PolicySectionCard(
        title = "1. Google Play In-App Billing",
        body = "All payments, recurring subscriptions (Monthly Pro), and one-time purchases (Lifetime Founder) are processed directly and securely through Google Play In-App Billing under Google Play Terms of Service."
    )

    PolicySectionCard(
        title = "2. Standard 48-Hour Google Play Window",
        body = "If you made an accidental purchase or are unsatisfied within 48 hours of transaction, you can request an instant refund directly through Google Play by visiting play.google.com/store/account/orderhistory."
    )

    PolicySectionCard(
        title = "3. Founder & Pro Satisfaction Guarantee",
        body = "If you encounter a billing error, technical defect, or unintended renewal beyond the 48-hour Google Play window, contact our developer support team directly at support@forma.app with your Google Play Order ID (e.g. GPA.xxxx-xxxx-xxxx-xxxxx). We review all legitimate refund requests within 2 business days."
    )

    PolicySectionCard(
        title = "4. Canceling Recurring Subscriptions",
        body = "You can cancel your Monthly Pro subscription at any time via the Google Play Subscriptions center. When canceled, your Pro access remains fully active until the conclusion of the paid billing period, with no subsequent charges."
    )
}

@Composable
private fun CookieConsentContent() {
    val colors = FormaTheme.colors

    PolicySectionCard(
        title = "1. Zero Web Advertising Cookies",
        body = "Forma is a native mobile application and does not use cookies for behavioral profiling, advertising targeting, or cross-site tracking. There are zero third-party marketing cookies present."
    )

    PolicySectionCard(
        title = "2. Strictly Necessary Functional Storage",
        body = "Forma only stores strictly necessary functional preferences locally on your device:\n• Android DataStore: Stores your selected color theme, dark mode preference, notification schedules, and consent status.\n• Local SQLite Room Database: Stores your habit rituals, subtasks, streaks, and focus records.\n• Cloud Auth Tokens: If you sign in, temporary authentication tokens are stored in Android Keystore solely to keep your private sync active."
    )

    PolicySectionCard(
        title = "3. Data Minimization Guarantee",
        body = "We believe in collecting only what our app needs to function smoothly. We do not collect unnecessary metadata or harvest telemetry."
    )
}

@Composable
private fun PolicySectionCard(title: String, body: String) {
    val colors = FormaTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, colors.border.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = body,
                style = FormaTheme.typography.bodySmall,
                color = colors.textSecondary,
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun NecessaryDataConsentBanner(
    hasConsented: Boolean,
    onAccept: () -> Unit,
    onReviewPolicies: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FormaTheme.colors

    if (!hasConsented) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.2.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DATA MINIMIZATION & PRIVACY",
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Strictly Necessary Data Only",
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Forma stores habits and timers locally on your device. We use zero advertising cookies and zero third-party trackers.",
                    color = colors.textSecondary,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceVariant)
                            .formaPressEffect(targetScale = 0.96f) { onReviewPolicies() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Review Policies",
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textSecondary,
                            fontSize = 11.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.accent)
                            .formaPressEffect(targetScale = 0.96f) { onAccept() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Accept Essential",
                            fontWeight = FontWeight.Bold,
                            color = colors.onAccent,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }
    }
}
