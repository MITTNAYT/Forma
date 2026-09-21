package com.habitflow.app.ui.analytics.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.motion.formaPressEffect
import com.habitflow.app.core.share.MilestoneCardExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareMilestoneDialog(
    habitTitle: String,
    streakDays: Int,
    userName: String = "Alex",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = NotionTheme.colors
    val scope = rememberCoroutineScope()

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(26.dp))
                .background(colors.surface)
                .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(26.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CELEBRATE MILESTONE",
                        style = NotionTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
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

                // Aesthetic Mini Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.background)
                        .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(vertical = 28.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Mini Ensō Ring
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(colors.accentSoft)
                                .border(3.dp, colors.accent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = streakDays.toString(),
                                style = NotionTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent,
                                fontSize = 34.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = habitTitle,
                            style = NotionTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "$streakDays Days of Consistent Flow",
                            style = NotionTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Share Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.accent)
                        .formaPressEffect(targetScale = 0.95f) {
                            scope.launch {
                                val result = MilestoneCardExporter.generateAndShareMilestone(
                                    context = context,
                                    habitTitle = habitTitle,
                                    streakDays = streakDays,
                                    userName = userName
                                )
                                result.onSuccess { intent ->
                                    context.startActivity(intent)
                                    onDismiss()
                                }.onFailure { error ->
                                    Toast.makeText(context, "Could not export card: ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share",
                            tint = colors.onAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Export & Share Story Card",
                            style = NotionTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onAccent,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
