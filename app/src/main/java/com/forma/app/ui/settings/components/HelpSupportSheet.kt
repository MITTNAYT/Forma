package com.forma.app.ui.settings.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.component.FormaButton
import com.forma.app.core.designsystem.component.FormaButtonStyle
import com.forma.app.core.designsystem.motion.formaPressEffect

data class FaqItem(
    val question: String,
    val answer: String,
    val icon: ImageVector = Icons.Rounded.QuestionAnswer
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportSheet(
    onDismiss: () -> Unit,
    onReplayTour: () -> Unit
) {
    val colors = FormaTheme.colors
    val context = LocalContext.current
    var expandedFaqIndex by remember { mutableIntStateOf(-1) }
    var feedbackText by remember { mutableStateOf("") }
    var isFeedbackSent by remember { mutableStateOf(false) }

    val faqs = remember {
        listOf(
            FaqItem(
                question = "How is my data kept private & offline?",
                answer = "Forma stores all your habits, subtasks, notes, and timers locally in an encrypted SQLite database (Room) on your device. We use zero third-party tracking cookies, zero ad telemetry, and do not track or profile your behavior.",
                icon = Icons.Rounded.Shield
            ),
            FaqItem(
                question = "How do habit micro-steps work?",
                answer = "When creating or editing a habit, add micro-steps under 'HABIT MICRO-STEPS & CHECKLIST'. On the Today screen, tap any habit card to view and check off individual steps (e.g. 'Fill water bottle', 'Wear running shoes').",
                icon = Icons.Rounded.TouchApp
            ),
            FaqItem(
                question = "How do I backup or transfer to a new device?",
                answer = "Go to Settings > Security & Data Vault. Choose 'Export Encrypted Vault (.habitvault)' and set a master passphrase. You can restore this file on any Android device running Forma, or connect optional Google/Email Cloud Sync.",
                icon = Icons.Rounded.Lock
            ),
            FaqItem(
                question = "How does the refund policy work?",
                answer = "All purchases are processed securely through Google Play. Google Play provides a 48-hour self-service refund window. Beyond 48 hours, reach out to developer support with your Google Play Order ID for assistance.",
                icon = Icons.AutoMirrored.Rounded.HelpOutline
            ),
            FaqItem(
                question = "Why aren't my reminder notifications ringing?",
                answer = "Ensure Android Notification permissions are granted and battery optimization is set to 'Unrestricted' for Forma in Android System Settings, allowing our exact alarms to wake your device at the configured time.",
                icon = Icons.Rounded.QuestionAnswer
            )
        )
    }

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
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "SANCTUARY SUPPORT",
                        fontWeight = FontWeight.Bold,
                        color = colors.textTertiary,
                        letterSpacing = 1.6.sp,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Help & Guidance",
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 22.sp,
                        letterSpacing = (-0.5).sp
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ── Section 1: Replay App Tour Banner ────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.accentSoft)
                    .border(1.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.accent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = colors.onAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Sanctuary Tour",
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Replay the interactive feature tour",
                                color = colors.textSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.accent)
                            .formaPressEffect(targetScale = 0.95f) {
                                onReplayTour()
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = colors.onAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Start Tour",
                                fontWeight = FontWeight.Bold,
                                color = colors.onAccent,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }

            // ── Section 2: Interactive FAQ Accordion ─────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "FREQUENTLY ASKED QUESTIONS",
                    fontWeight = FontWeight.Bold,
                    color = colors.textTertiary,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp
                )

                faqs.forEachIndexed { index, faq ->
                    val isExpanded = expandedFaqIndex == index
                    val rotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrow_rot")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surfaceVariant.copy(alpha = 0.45f))
                            .border(1.dp, colors.border.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                            .clickable {
                                expandedFaqIndex = if (isExpanded) -1 else index
                            }
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = faq.icon,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = faq.question,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary,
                                        fontSize = 13.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.ExpandMore,
                                    contentDescription = null,
                                    tint = colors.textTertiary,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .rotate(rotation)
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = faq.answer,
                                        style = FormaTheme.typography.bodySmall,
                                        color = colors.textSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Section 3: Contact & Direct Feedback ────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "CONTACT & FEEDBACK",
                    fontWeight = FontWeight.Bold,
                    color = colors.textTertiary,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.35f))
                        .border(1.dp, colors.border.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Email,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Send Message to Developer Support",
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary,
                                fontSize = 13.sp
                            )
                        }

                        if (isFeedbackSent) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.accentSoft)
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Thank you! Your feedback has been received.",
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            OutlinedTextField(
                                value = feedbackText,
                                onValueChange = { feedbackText = it },
                                placeholder = {
                                    Text("Have a suggestion, bug report, or feature thought?", color = colors.textTertiary, fontSize = 12.sp)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.accent,
                                    unfocusedBorderColor = colors.border.copy(alpha = 0.4f),
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Email client button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.surfaceVariant)
                                        .formaPressEffect(targetScale = 0.96f) {
                                            launchEmailSupport(context)
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Open Mail Client",
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textSecondary,
                                        fontSize = 11.5.sp
                                    )
                                }

                                // Quick in-app submit
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (feedbackText.isNotBlank()) colors.accent else colors.accent.copy(alpha = 0.4f))
                                        .formaPressEffect(targetScale = 0.96f) {
                                            if (feedbackText.isNotBlank()) {
                                                isFeedbackSent = true
                                                Toast.makeText(context, "Feedback noted. Thank you for shaping Forma!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Send,
                                            contentDescription = null,
                                            tint = colors.onAccent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Send Message",
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
            }

            // ── Section 4: Gesture Guide ────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "SANCTUARY GESTURES",
                    fontWeight = FontWeight.Bold,
                    color = colors.textTertiary,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp
                )

                listOf(
                    "Tap circle icon" to "Mark habit complete with tactile haptic spring",
                    "Tap habit card body" to "Inspect details & check off micro-steps",
                    "Long press habit" to "Reorder or quick edit ritual",
                    "Filter chips" to "Filter between All, Morning, Afternoon & Evening",
                    "Evening Sanctuary" to "Reflect and rollover unfinished tasks gracefully"
                ).forEach { (gesture, description) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "•",
                            color = colors.accent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "$gesture: ",
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = description,
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

private fun launchEmailSupport(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@forma.app")
            putExtra(Intent.EXTRA_SUBJECT, "Forma Support Request [Android ${Build.VERSION.RELEASE}]")
            putExtra(
                Intent.EXTRA_TEXT,
                "\n\n---\nApp Version: Forma v2.0\nDevice: ${Build.MANUFACTURER} ${Build.MODEL}\nAndroid OS: ${Build.VERSION.RELEASE}\n"
            )
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Contact developer team at: support@forma.app", Toast.LENGTH_LONG).show()
    }
}
