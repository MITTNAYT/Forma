package com.habitflow.app.ui.stats.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.FormaTheme
import com.habitflow.app.core.designsystem.component.FormaButton
import com.habitflow.app.core.designsystem.component.FormaButtonStyle
import com.habitflow.app.domain.usecase.WeeklyZenRetro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyZenRetroSheet(
    retro: WeeklyZenRetro,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val colors = FormaTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Spa,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "WEEKLY ZEN RETRO",
                            style = FormaTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.sp
                        )
                        Text(
                            text = retro.weekDateRange,
                            style = FormaTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Score Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.surface)
                    .border(1.5.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Consistency Index",
                        style = FormaTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textTertiary,
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${retro.consistencyScore}%",
                        style = FormaTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        fontSize = 44.sp
                    )
                    Text(
                        text = "\"${retro.zenAffirmation}\"",
                        style = FormaTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3 Pillar Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Rituals",
                    value = "${retro.totalHabitsCompleted}",
                    sub = "completed",
                    icon = Icons.Rounded.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Deep Work",
                    value = String.format("%.1fh", retro.totalFocusMinutes / 60f),
                    sub = "focused",
                    icon = Icons.Rounded.Timer,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Peace",
                    value = String.format("%.1f/5", retro.averagePeaceRating),
                    sub = "serenity",
                    icon = Icons.Rounded.Favorite,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Top Habit Card
            retro.topPerformingHabit?.let { habit ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.accentSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "TOP RITUAL ANCHOR",
                                style = FormaTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent,
                                fontSize = 10.5.sp
                            )
                            Text(
                                text = habit.name,
                                style = FormaTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Friction Diagnostics Warning
            retro.frictionRecommendation?.let { recommendation ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFF8E7))
                        .border(1.dp, Color(0xFFE5C07B), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = Color(0xFFB58900),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "HABIT FRICTION DIAGNOSTIC",
                                style = FormaTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB58900),
                                fontSize = 10.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = recommendation,
                                style = FormaTheme.typography.bodySmall,
                                color = Color(0xFF584A2B),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Share Button
            FormaButton(
                text = "Share Weekly Zen Summary",
                onClick = {
                    val shareText = """
                        Forma Weekly Focus & Mindful Retro (${retro.weekDateRange})
                        • Consistency Index: ${retro.consistencyScore}%
                        • Rituals Completed: ${retro.totalHabitsCompleted}
                        • Deep Focus Output: ${String.format("%.1fh", retro.totalFocusMinutes / 60f)}
                        • Average Serenity: ${String.format("%.1f", retro.averagePeaceRating)}/5.0
                        
                        "${retro.zenAffirmation}"
                    """.trimIndent()

                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Weekly Zen Retro"))
                },
                style = FormaButtonStyle.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    val colors = FormaTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = FormaTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                fontSize = 15.sp
            )
            Text(
                text = title,
                style = FormaTheme.typography.labelSmall,
                color = colors.textTertiary,
                fontSize = 10.sp
            )
        }
    }
}
