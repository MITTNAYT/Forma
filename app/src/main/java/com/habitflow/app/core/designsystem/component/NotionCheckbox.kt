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
fun FormaCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    accentColor: Color = FormaTheme.colors.accent
) {
    val colors = FormaTheme.colors
    val bgColor by animateColorAsState(
        targetValue = if (checked) accentColor else Color.Transparent,
        animationSpec = tween(durationMillis = 180),
        label = "checkbox_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) accentColor else colors.border,
        animationSpec = tween(durationMillis = 180),
        label = "checkbox_border"
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(FormaTheme.shapes.extraSmall)
            .background(bgColor)
            .border(1.5.dp, borderColor, FormaTheme.shapes.extraSmall)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = checked,
            enter = fadeIn(tween(150)) + scaleIn(tween(150), initialScale = 0.6f),
            exit = fadeOut(tween(100)) + scaleOut(tween(100), targetScale = 0.6f)
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Completed",
                tint = Color.White,
                modifier = Modifier.size(size * 0.75f)
            )
        }
    }
}

@Composable
fun NotionCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    accentColor: Color = FormaTheme.colors.accent
) {
    FormaCheckbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        size = size,
        accentColor = accentColor
    )
}
