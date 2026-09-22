package com.forma.app.ui.circles

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.domain.model.Circle
import com.forma.app.domain.model.CircleReactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleDetailScreen(
    circle: Circle,
    viewModel: CirclesViewModel,
    onNavigateBack: () -> Unit
) {
    val colors = FormaTheme.colors
    val members by viewModel.selectedCircleMembers.collectAsState()
    val checkIns by viewModel.selectedCircleCheckIns.collectAsState()
    val context = LocalContext.current

    var showMenu by remember { mutableStateOf(false) }
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = circle.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${members.size} members • Code: ${circle.inviteCode}",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Menu", tint = colors.textPrimary)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Copy Invite Code") },
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Circle Invite Code", circle.inviteCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Code copied: ${circle.inviteCode}", Toast.LENGTH_SHORT).show()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Rounded.ContentCopy, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Leave Circle") },
                            onClick = {
                                viewModel.leaveCircle(circle.id)
                                showMenu = false
                                onNavigateBack()
                            }
                        )
                    }
                }
            }
        },
        containerColor = colors.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Invite Banner
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = colors.accentSoft,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.accent.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Invite Your Inner Circle",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = colors.accent
                            )
                            Text(
                                text = "Share code '${circle.inviteCode}' with friends to journey together.",
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        }
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Circle Code", circle.inviteCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Invite Code ${circle.inviteCode} copied!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy", tint = colors.accent)
                        }
                    }
                }
            }

            // Members & Momentum Leaderboard
            item {
                Text(
                    text = "SANCTUARY MEMBERS",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize = 10.sp,
                    color = colors.textTertiary
                )
            }

            items(members) { member ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(colors.accentSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.displayName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent
                                )
                            }
                            Column {
                                Text(
                                    text = member.displayName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = if (member.currentStreak > 0) "🔥 ${member.currentStreak} day streak" else "🌱 Fresh start",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        // Mindful Quick Reaction Trigger
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CircleReactionType.entries.take(3).forEach { reaction ->
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(colors.accentSoft)
                                        .clickable {
                                            viewModel.sendReaction(circle.id, member.userId, reaction)
                                        }
                                        .padding(6.dp)
                                ) {
                                    Text(text = reaction.emoji, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Recent Circle Activity Stream
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "SHARED PRESENCE FEED",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize = 10.sp,
                    color = colors.textTertiary
                )
            }

            if (checkIns.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No check-ins today yet. Complete a habit to share your presence.",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            } else {
                items(checkIns) { checkIn ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = colors.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${checkIn.userName} completed '${checkIn.habitName}'",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary
                                )
                                if (checkIn.note.isNotBlank()) {
                                    Text(
                                        text = "\"${checkIn.note}\"",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            Text(
                                text = timeFormatter.format(Date(checkIn.timestamp)),
                                fontSize = 10.sp,
                                color = colors.textTertiary
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
