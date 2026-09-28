package com.forma.app.ui.settings.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.component.FormaEmblem
import com.forma.app.core.designsystem.icon.FormaIcon
import com.forma.app.core.designsystem.motion.formaPressEffect

data class WalkthroughChapterMeta(
    val id: Int,
    val shortName: String,
    val badge: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanctuaryWalkthroughSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = FormaTheme.colors
    val haptic = LocalHapticFeedback.current

    var currentChapterIndex by remember { mutableIntStateOf(0) }

    val chapters = remember {
        listOf(
            WalkthroughChapterMeta(
                id = 0,
                shortName = "Welcome",
                badge = "CHAPTER 01",
                title = "Welcome & Testimonials",
                subtitle = "Where intention takes form. A calm sanctuary loved by mindful creators.",
                icon = Icons.Rounded.Spa,
                accentColor = Color(0xFF4A7C59)
            ),
            WalkthroughChapterMeta(
                id = 1,
                shortName = "Sign In",
                badge = "CHAPTER 02",
                title = "Sign Up / Sign In",
                subtitle = "Sovereign privacy. Sync with Clerk & Google or remain 100% offline.",
                icon = Icons.Rounded.Lock,
                accentColor = Color(0xFF5A3926)
            ),
            WalkthroughChapterMeta(
                id = 2,
                shortName = "Starters",
                badge = "CHAPTER 03",
                title = "Starter Habit Selection",
                subtitle = "Archetypes curated with atomic micro-steps and environmental cues.",
                icon = Icons.Rounded.AutoAwesome,
                accentColor = Color(0xFFC58A24)
            ),
            WalkthroughChapterMeta(
                id = 3,
                shortName = "Paywall",
                badge = "CHAPTER 04",
                title = "Sanctuary Membership",
                subtitle = "Transparent tiers. Free 3 habits forever, Pro, and Forma Founder lifetime.",
                icon = Icons.Rounded.Star,
                accentColor = Color(0xFFD4AF37)
            ),
            WalkthroughChapterMeta(
                id = 4,
                shortName = "Notifications",
                badge = "CHAPTER 05",
                title = "Notification Permission",
                subtitle = "Gentle circadian cues powered by exact alarms. Zero spam or dark patterns.",
                icon = Icons.Rounded.NotificationsActive,
                accentColor = Color(0xFF8D5B4C)
            ),
            WalkthroughChapterMeta(
                id = 5,
                shortName = "Mood Log",
                badge = "CHAPTER 06",
                title = "Logging a Mood",
                subtitle = "Morning alignment and evening reflections to track internal weather.",
                icon = Icons.Rounded.WbSunny,
                accentColor = Color(0xFFE29578)
            ),
            WalkthroughChapterMeta(
                id = 6,
                shortName = "Create Habit",
                badge = "CHAPTER 07",
                title = "Creating a Habit",
                subtitle = "Tie habits to cues and break big ambitions into 2-minute steps.",
                icon = Icons.Rounded.Add,
                accentColor = Color(0xFF2C6E49)
            ),
            WalkthroughChapterMeta(
                id = 7,
                shortName = "Complete Habit",
                badge = "CHAPTER 08",
                title = "Completing a Habit",
                subtitle = "Tactile haptic springs, spark bursts, and dynamic streak islands.",
                icon = Icons.Rounded.CheckCircle,
                accentColor = Color(0xFF4A7C59)
            ),
            WalkthroughChapterMeta(
                id = 8,
                shortName = "Habit Details",
                badge = "CHAPTER 09",
                title = "Exploring Habit Details",
                subtitle = "Streak curves, micro-step progress, focus sprints, and wintering mode.",
                icon = Icons.Rounded.Schedule,
                accentColor = Color(0xFF5E548E)
            ),
            WalkthroughChapterMeta(
                id = 9,
                shortName = "Join Circle",
                badge = "CHAPTER 10",
                title = "Joining a Challenge",
                subtitle = "Community Circles for shared momentum without toxic social media noise.",
                icon = Icons.Rounded.Group,
                accentColor = Color(0xFF3D5A80)
            ),
            WalkthroughChapterMeta(
                id = 10,
                shortName = "Create Circle",
                badge = "CHAPTER 11",
                title = "Creating a Challenge",
                subtitle = "Rally teams, friends, or study groups around positive daily rituals.",
                icon = Icons.Rounded.GroupAdd,
                accentColor = Color(0xFF6B4E71)
            )
        )
    }

    val currentChapter = chapters[currentChapterIndex]
    val isLastChapter = currentChapterIndex == chapters.size - 1

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.border)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header Row: Badge, counter, and close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(currentChapter.accentColor.copy(alpha = 0.14f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = currentChapter.badge,
                            style = FormaTheme.typography.labelSmall.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                letterSpacing = 1.2.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = currentChapter.accentColor,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${String.format("%02d", currentChapterIndex + 1)} / ${String.format("%02d", chapters.size)}",
                        style = FormaTheme.typography.labelSmall.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            fontFeatureSettings = "tnum"
                        ),
                        color = colors.textTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDismiss()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal Scrollable Chapter Selector Chips (Jump to any of the 11 chapters)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                chapters.forEachIndexed { index, chapter ->
                    val isSelected = index == currentChapterIndex
                    val chipBg by animateColorAsState(
                        targetValue = if (isSelected) chapter.accentColor else colors.surfaceVariant.copy(alpha = 0.6f),
                        label = "ch_chip_bg"
                    )
                    val chipFg by animateColorAsState(
                        targetValue = if (isSelected) colors.onAccent else colors.textSecondary,
                        label = "ch_chip_fg"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(chipBg)
                            .border(
                                1.dp,
                                if (isSelected) chapter.accentColor else colors.border.copy(alpha = 0.4f),
                                RoundedCornerShape(10.dp)
                            )
                            .formaPressEffect(targetScale = 0.94f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentChapterIndex = index
                            }
                            .padding(horizontal = 9.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}. ${chapter.shortName}",
                            style = FormaTheme.typography.labelSmall.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = chipFg,
                            fontSize = 10.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step Progress Track (11 segments)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                chapters.indices.forEach { index ->
                    val isActive = index <= currentChapterIndex
                    val segmentColor by animateColorAsState(
                        targetValue = if (isActive) currentChapter.accentColor else colors.surfaceVariant,
                        label = "ch_progress_segment"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.5.dp)
                            .clip(CircleShape)
                            .background(segmentColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Animated Slide Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .weight(1f, fill = false)
            ) {
                AnimatedContent(
                    targetState = currentChapterIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width / 3 } + fadeIn(tween(220)))
                                .togetherWith(slideOutHorizontally { width -> -width / 3 } + fadeOut(tween(180)))
                        } else {
                            (slideInHorizontally { width -> -width / 3 } + fadeIn(tween(220)))
                                .togetherWith(slideOutHorizontally { width -> width / 3 } + fadeOut(tween(180)))
                        }
                    },
                    label = "chapter_slide_content"
                ) { chapterIdx ->
                    when (chapterIdx) {
                        0 -> WelcomeWithTestimonialsChapter(currentChapter.accentColor)
                        1 -> SignUpSignInChapter(currentChapter.accentColor)
                        2 -> StarterHabitSelectionChapter(currentChapter.accentColor)
                        3 -> PaywallChapter(currentChapter.accentColor)
                        4 -> NotificationPermissionPromptChapter(currentChapter.accentColor)
                        5 -> LoggingMoodChapter(currentChapter.accentColor)
                        6 -> CreatingHabitChapter(currentChapter.accentColor)
                        7 -> CompletingHabitChapter(currentChapter.accentColor)
                        8 -> ExploringHabitDetailsChapter(currentChapter.accentColor)
                        9 -> JoiningChallengeChapter(currentChapter.accentColor)
                        10 -> CreatingChallengeChapter(currentChapter.accentColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentChapterIndex > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surfaceVariant)
                            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .formaPressEffect(targetScale = 0.94f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentChapterIndex--
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Previous",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Previous",
                                style = FormaTheme.typography.labelSmall.copy(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                ),
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(currentChapter.accentColor)
                        .formaPressEffect(targetScale = 0.94f) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (isLastChapter) {
                                onDismiss()
                            } else {
                                currentChapterIndex++
                            }
                        }
                        .padding(horizontal = 22.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isLastChapter) "Enter Sanctuary" else "Continue Chapter →",
                            style = FormaTheme.typography.labelSmall.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.5.sp
                        )
                        if (isLastChapter) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHAPTER 01: WELCOME WITH USER TESTIMONIALS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WelcomeWithTestimonialsChapter(accent: Color) {
    val colors = FormaTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FormaEmblem(size = 68.dp, animated = true)

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Where Intention Takes Form",
            style = FormaTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Forma replaces endless to-do lists with a calm circadian rhythm, zero-guilt streaks, and local-first privacy.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "LOVED BY MINDFUL HUMANS",
            style = FormaTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accent,
            letterSpacing = 1.3.sp,
            fontSize = 10.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Testimonial Cards
        listOf(
            Triple(
                "Maya R. • Mindful Architect",
                "Forma eliminated the anxiety of traditional streak apps. Wintering mode saved my sanity during travel.",
                "★★★★★"
            ),
            Triple(
                "Julian K. • Systems Engineer",
                "Local-first, encrypted offline vault with zero trackers. The best Android app I have installed in years.",
                "★★★★★"
            ),
            Triple(
                "Elena S. • Author & Meditator",
                "Habit stacking cues and micro-steps helped me write 50,000 words without a single day of burnout.",
                "★★★★★"
            )
        ).forEach { (author, quote, stars) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = author,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = stars,
                            color = Color(0xFFD4AF37),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"$quote\"",
                        style = FormaTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHAPTER 02: SIGN UP / SIGN IN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SignUpSignInChapter(accent: Color) {
    val colors = FormaTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(accent.copy(alpha = 0.12f))
                .border(1.5.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Sovereign Auth & Sync",
            style = FormaTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Authenticate securely with Clerk or remain 100% offline. No third-party data tracking.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Mock Auth Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surfaceVariant.copy(alpha = 0.5f))
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue with Google Account",
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        fontSize = 12.5.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sign In with Email Magic Link",
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    fontSize = 12.5.sp
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.12f))
                    .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "100% Guest Mode (Local Encrypted SQLite)",
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        fontSize = 11.5.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHAPTER 03: STARTER HABIT SELECTION
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StarterHabitSelectionChapter(accent: Color) {
    val colors = FormaTheme.colors
    var selectedArchetype by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Persona Starter Packs",
            style = FormaTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Pick an archetype tailored to your lifestyle. Each habit comes with proven cues and micro-steps.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        val archetypes = listOf(
            Triple("Mindful Living", "Hydration, 10m Meditation, Nature Walk", Icons.Rounded.Spa),
            Triple("Deep Work", "Morning Focus Sprint, Zero Inbox, Evening Shutdown", Icons.Rounded.Schedule),
            Triple("Creative Studio", "Sketching, Daily Reading, Journaling", Icons.Rounded.AutoAwesome)
        )

        archetypes.forEachIndexed { index, (name, habits, icon) ->
            val isSelected = index == selectedArchetype
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) accent.copy(alpha = 0.12f) else colors.surfaceVariant.copy(alpha = 0.45f))
                    .border(
                        1.dp,
                        if (isSelected) accent else colors.border.copy(alpha = 0.4f),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { selectedArchetype = index }
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) accent else colors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) colors.onAccent else colors.textPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = habits,
                            style = FormaTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHAPTER 04: PAYWALL
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PaywallChapter(accent: Color) {
    val colors = FormaTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Transparent Sanctuary Tiers",
            style = FormaTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Free users cultivate up to 3 rituals forever. Sanctuary Pro and Lifetime Founder unlock limitless rhythm.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Free Tier
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(text = "FREE", fontWeight = FontWeight.Bold, color = colors.textTertiary, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Explorer", fontWeight = FontWeight.Bold, color = colors.textPrimary, fontSize = 14.sp)
                    Text(text = "$0", fontWeight = FontWeight.ExtraBold, color = colors.textPrimary, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "• 3 Active Habits\n• Offline Database\n• Basic Streaks", color = colors.textSecondary, fontSize = 10.5.sp, lineHeight = 15.sp)
                }
            }

            // Founder Lifetime Tier
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accent.copy(alpha = 0.12f))
                    .border(1.5.dp, accent, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(accent)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(text = "LIFETIME", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 9.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Forma Founder", fontWeight = FontWeight.Bold, color = colors.textPrimary, fontSize = 14.sp)
                    Text(text = "$49.99 once", fontWeight = FontWeight.ExtraBold, color = accent, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "• Unlimited Habits\n• Ambient Soundscapes\n• Wintering Rest Mode\n• Vault Export", color = colors.textPrimary, fontSize = 10.5.sp, lineHeight = 15.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHAPTER 05: NOTIFICATION PERMISSION PROMPT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NotificationPermissionPromptChapter(accent: Color) {
    val colors = FormaTheme.colors
    var morningEnabled by remember { mutableStateOf(true) }
    var eveningEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Gentle Circadian Cues",
            style = FormaTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Forma uses exact alarms only for rituals you designate. Never marketing spam or guilt pings.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surfaceVariant.copy(alpha = 0.45f))
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Morning Anchor (08:00 AM)", fontWeight = FontWeight.Bold, color = colors.textPrimary, fontSize = 13.sp)
                    Text(text = "Wake up to your primary keystone habit", color = colors.textSecondary, fontSize = 11.sp)
                }
                Switch(
                    checked = morningEnabled,
                    onCheckedChange = { morningEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = colors.surface, checkedTrackColor = accent)
                )
            }

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.4f)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Evening Sanctuary (09:30 PM)", fontWeight = FontWeight.Bold, color = colors.textPrimary, fontSize = 13.sp)
                    Text(text = "Peaceful reflection & rollover unfinished items", color = colors.textSecondary, fontSize = 11.sp)
                }
                Switch(
                    checked = eveningEnabled,
                    onCheckedChange = { eveningEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = colors.surface, checkedTrackColor = accent)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHAPTER 06: LOGGING A MOOD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LoggingMoodChapter(accent: Color) {
    val colors = FormaTheme.colors
    var selectedMood by remember { mutableStateOf("Calm") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mindful Mood & Reflection",
            style = FormaTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Track your internal energy and weather. Pair daily feelings with short reflections.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                Triple("Radiant", "☀️", Color(0xFFE9C46A)),
                Triple("Calm", "🌿", Color(0xFF4A7C59)),
                Triple("Grounded", "⛰️", Color(0xFF8D5B4C)),
                Triple("Heavy", "🌧️", Color(0xFF3D5A80)),
                Triple("Foggy", "🌫️", Color(0xFF7F7F7F))
            ).forEach { (mood, emoji, moodColor) ->
                val isSelected = selectedMood == mood
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { selectedMood = mood }
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) moodColor.copy(alpha = 0.25f) else colors.surfaceVariant)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) moodColor else colors.border.copy(alpha = 0.4f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mood,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) colors.textPrimary else colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHAPTER 07: CREATING A HABIT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CreatingHabitChapter(accent: Color) {
    val colors = FormaTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Atomic Habit Architecture",
            style = FormaTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Anchor new intentions onto existing cues and specify tactile micro-steps.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "RITUAL TITLE", style = FormaTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = accent, fontSize = 10.sp)
                Text(text = "Morning Tea & Stillness", fontWeight = FontWeight.Bold, color = colors.textPrimary, fontSize = 15.sp)

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.4f)))

                Text(text = "HABIT STACKING CUE", style = FormaTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = accent, fontSize = 10.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Rounded.Link, contentDescription = null, tint = accent, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "After I boil water for tea, I will sit for 5 minutes.", color = colors.textSecondary, fontSize = 12.sp)
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.4f)))

                Text(text = "MICRO-STEPS CHECKLIST", style = FormaTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = accent, fontSize = 10.sp)
                Text(text = "✓ Pour hot water into cup\n✓ Put phone in another room\n✓ Take 10 mindful breaths", color = colors.textSecondary, fontSize = 11.5.sp, lineHeight = 16.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHAPTER 08: COMPLETING A HABIT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CompletingHabitChapter(accent: Color) {
    val colors = FormaTheme.colors
    var isDone by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tactile Spring Completion",
            style = FormaTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Tap the circular checkmark below to test the spring haptic feedback & streak celebration.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(if (isDone) accent.copy(alpha = 0.12f) else colors.surface)
                .border(
                    width = if (isDone) 1.5.dp else 1.dp,
                    color = if (isDone) accent else colors.border,
                    shape = RoundedCornerShape(18.dp)
                )
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
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Rounded.Spa, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Deep Focus Sprint", fontWeight = FontWeight.Bold, color = colors.textPrimary, fontSize = 14.5.sp)
                        Text(text = if (isDone) "Completed! Streak: 14 days" else "Morning • 25 min", color = colors.textSecondary, fontSize = 11.5.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDone) accent else Color.Transparent)
                        .border(1.5.dp, if (isDone) accent else colors.border, CircleShape)
                        .clickable { isDone = !isDone },
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(imageVector = Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHAPTER 09: EXPLORING HABIT DETAILS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExploringHabitDetailsChapter(accent: Color) {
    val colors = FormaTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Habit Analytics & Wintering",
            style = FormaTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Tap any habit on Today to view micro-steps, streak curves, and toggle Wintering rest days.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Column {
                    Text(text = "CURRENT STREAK", fontWeight = FontWeight.Bold, color = colors.textTertiary, fontSize = 9.sp)
                    Text(text = "18 Days", fontWeight = FontWeight.ExtraBold, color = colors.textPrimary, fontSize = 16.sp)
                    Text(text = "Top 5% consistency", color = colors.textSecondary, fontSize = 10.sp)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Column {
                    Text(text = "WINTERING REST", fontWeight = FontWeight.Bold, color = accent, fontSize = 9.sp)
                    Text(text = "Safe & Frozen", fontWeight = FontWeight.ExtraBold, color = colors.textPrimary, fontSize = 16.sp)
                    Text(text = "Zero guilt or break", color = colors.textSecondary, fontSize = 10.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHAPTER 10: JOINING A CHALLENGE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun JoiningChallengeChapter(accent: Color) {
    val colors = FormaTheme.colors
    var isJoined by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Circles: Shared Accountability",
            style = FormaTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Join community challenges without toxic social comparison. Share daily consistency together.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(1.dp, if (isJoined) accent else colors.border, RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accent.copy(alpha = 0.15f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(text = "30-DAY CHALLENGE", fontWeight = FontWeight.Bold, color = accent, fontSize = 9.5.sp)
                    }
                    Text(text = "248 Members", color = colors.textTertiary, fontSize = 10.5.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Dawn Awakening & Stillness", fontWeight = FontWeight.Bold, color = colors.textPrimary, fontSize = 15.sp)
                Text(text = "Wake up before 7:00 AM and sit in mindful reflection for 10 minutes.", color = colors.textSecondary, fontSize = 11.5.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isJoined) colors.accentSoft else accent)
                        .clickable { isJoined = !isJoined },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isJoined) "Joined ✓" else "Join Challenge",
                        fontWeight = FontWeight.Bold,
                        color = if (isJoined) accent else Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHAPTER 11: CREATING A CHALLENGE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CreatingChallengeChapter(accent: Color) {
    val colors = FormaTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Your Own Circle",
            style = FormaTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Rally your close circle, team, or study group. Set duration, select a target ritual, and invite peers.",
            style = FormaTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceVariant.copy(alpha = 0.5f))
                .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "CHALLENGE NAME", style = FormaTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = accent, fontSize = 10.sp)
                Text(text = "Productivity Deep Work Sprint", fontWeight = FontWeight.Bold, color = colors.textPrimary, fontSize = 14.sp)

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.4f)))

                Text(text = "CHALLENGE DURATION", style = FormaTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = accent, fontSize = 10.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("7 Days", "14 Days", "21 Days", "30 Days").forEachIndexed { i, d ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (i == 2) accent else colors.surface)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = d,
                                color = if (i == 2) Color.White else colors.textPrimary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.4f)))

                Text(text = "PRIVACY & ACCESS", style = FormaTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = accent, fontSize = 10.sp)
                Text(text = "Private Invite Passcode: #FORMA-SANCTUARY", color = colors.textSecondary, fontSize = 11.5.sp)
            }
        }
    }
}
