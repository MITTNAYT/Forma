package com.habitflow.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.NotionTheme

@Composable
fun StreakBadge(
    streakCount: Int,
    modifier: Modifier = Modifier,
    showFireIcon: Boolean = true
) {
    val colors = NotionTheme.colors

    Row(
        modifier = modifier
            .clip(NotionTheme.shapes.extraSmall)
            .background(if (streakCount > 0) colors.textPrimary else colors.surfaceVariant)
            .border(
                1.dp,
                if (streakCount > 0) colors.textPrimary else colors.border,
                NotionTheme.shapes.extraSmall
            )
            .padding(horizontal = 7.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showFireIcon && streakCount > 0) {
            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = null,
                tint = if (streakCount > 0) com.habitflow.app.core.designsystem.ObsidianBlackBg else colors.textPrimary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
        }
        Text(
            text = "$streakCount",
            style = NotionTheme.typography.labelSmall,
            color = if (streakCount > 0) com.habitflow.app.core.designsystem.ObsidianBlackBg else colors.textSecondary
        )
    }
}
