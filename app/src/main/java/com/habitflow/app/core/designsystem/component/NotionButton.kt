package com.habitflow.app.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.NotionTheme

enum class NotionButtonStyle {
    PRIMARY,
    OUTLINE,
    GHOST,
    SOFT_PILL
}

@Composable
fun NotionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: NotionButtonStyle = NotionButtonStyle.PRIMARY,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val colors = NotionTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "btn_press_scale"
    )

    when (style) {
        NotionButtonStyle.PRIMARY -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.onAccent,
                    disabledContainerColor = colors.accent.copy(alpha = 0.35f),
                    disabledContentColor = colors.onAccent.copy(alpha = 0.6f)
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                modifier = modifier
                    .scale(buttonScale)
                    .height(48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    leadingIcon?.let {
                        it()
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = text,
                        style = NotionTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
        NotionButtonStyle.SOFT_PILL -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentSoft,
                    contentColor = colors.accent,
                    disabledContainerColor = colors.surfaceVariant,
                    disabledContentColor = colors.textTertiary
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                modifier = modifier
                    .scale(buttonScale)
                    .height(44.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    leadingIcon?.let {
                        it()
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = text,
                        style = NotionTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
        NotionButtonStyle.OUTLINE -> {
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, if (enabled) colors.border else colors.border.copy(alpha = 0.4f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.surface,
                    contentColor = colors.textPrimary,
                    disabledContentColor = colors.textTertiary
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                modifier = modifier
                    .scale(buttonScale)
                    .height(44.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    leadingIcon?.let {
                        it()
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = text,
                        style = NotionTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
        NotionButtonStyle.GHOST -> {
            TextButton(
                onClick = onClick,
                enabled = enabled,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colors.accent,
                    disabledContentColor = colors.textTertiary
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = modifier
                    .scale(buttonScale)
                    .height(38.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    leadingIcon?.let {
                        it()
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = text,
                        style = NotionTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
