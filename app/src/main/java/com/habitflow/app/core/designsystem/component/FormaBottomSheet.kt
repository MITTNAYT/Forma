package com.habitflow.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.NotionTheme
import com.habitflow.app.core.designsystem.motion.formaPressEffect

/**
 * Premium Framer-style Floating Bottom Sheet Container.
 *
 * Features:
 * - Fluid spring reveal & hide physics.
 * - Dimmed backdrop preserving background screen context.
 * - Auto-adapting keyboard avoidance (imePadding + navigationBarsPadding).
 * - Outside tap gesture to blur active input and dismiss soft keyboard.
 * - Standardized drag handle and contextual title bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormaBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    title: String? = null,
    subtitle: String? = null,
    showCloseButton: Boolean = true,
    trailingAction: (@Composable () -> Unit)? = null,
    containerColor: Color = NotionTheme.colors.background,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = NotionTheme.colors
    val focusManager = LocalFocusManager.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = containerColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 38.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(colors.border.copy(alpha = 0.8f))
                )
            }
        },
        scrimColor = Color.Black.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Optional Header Row
            if (title != null || showCloseButton || trailingAction != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        if (title != null) {
                            Text(
                                text = title,
                                style = NotionTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 20.sp,
                                letterSpacing = (-0.3).sp
                            )
                        }
                        if (subtitle != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = subtitle,
                                style = NotionTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                fontSize = 12.5.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        trailingAction?.invoke()

                        if (showCloseButton) {
                            if (trailingAction != null) Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceVariant)
                                    .formaPressEffect(targetScale = 0.90f) {
                                        onDismissRequest()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Body Content
            content()
        }
    }
}
