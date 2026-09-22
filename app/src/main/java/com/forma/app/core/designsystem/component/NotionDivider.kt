package com.forma.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.forma.app.core.designsystem.FormaTheme

@Composable
fun FormaDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    verticalPadding: Dp = 0.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = verticalPadding)
            .height(thickness)
            .background(FormaTheme.colors.border)
    )
}

@Composable
fun NotionDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    verticalPadding: Dp = 0.dp
) {
    FormaDivider(
        modifier = modifier,
        thickness = thickness,
        verticalPadding = verticalPadding
    )
}
