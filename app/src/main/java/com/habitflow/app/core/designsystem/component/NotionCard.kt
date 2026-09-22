package com.habitflow.app.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.habitflow.app.core.designsystem.FormaTheme
import com.habitflow.app.core.designsystem.motion.formaPressEffect

@Composable
fun FormaCard(
    modifier: Modifier = Modifier,
    shape: Shape = FormaTheme.shapes.medium,
    backgroundColor: Color = FormaTheme.colors.surface,
    borderColor: Color = FormaTheme.colors.border,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier.then(
            if (onClick != null) Modifier.formaPressEffect(targetScale = 0.98f, onClick = onClick) else Modifier
        ),
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.padding(14.dp),
            content = content
        )
    }
}

@Composable
fun NotionCard(
    modifier: Modifier = Modifier,
    shape: Shape = FormaTheme.shapes.medium,
    backgroundColor: Color = FormaTheme.colors.surface,
    borderColor: Color = FormaTheme.colors.border,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    FormaCard(
        modifier = modifier,
        shape = shape,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        borderWidth = borderWidth,
        onClick = onClick,
        content = content
    )
}
