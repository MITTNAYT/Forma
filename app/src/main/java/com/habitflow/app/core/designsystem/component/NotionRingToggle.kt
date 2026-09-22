package com.habitflow.app.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.habitflow.app.core.designsystem.FormaTheme

@Composable
fun FormaRingToggle(
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    accentColor: Color = FormaTheme.colors.accent
) {
    val bgColor by animateColorAsState(
        targetValue = if (checked) accentColor else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "ring_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = accentColor,
        animationSpec = tween(durationMillis = 200),
        label = "ring_border"
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor)
            .border(2.5.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onToggle()
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = checked,
            enter = fadeIn(tween(160)) + scaleIn(tween(160), initialScale = 0.5f),
            exit = fadeOut(tween(100)) + scaleOut(tween(100), targetScale = 0.5f)
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Done",
                tint = Color.White,
                modifier = Modifier.size(size * 0.65f)
            )
        }
    }
}

@Composable
fun NotionRingToggle(
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    accentColor: Color = FormaTheme.colors.accent
) {
    FormaRingToggle(
        checked = checked,
        onToggle = onToggle,
        modifier = modifier,
        size = size,
        accentColor = accentColor
    )
}
