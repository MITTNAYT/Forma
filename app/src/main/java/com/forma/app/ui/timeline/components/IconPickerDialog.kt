package com.forma.app.ui.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.forma.app.core.designsystem.motion.formaPressEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.icon.FormaIcon
import com.forma.app.core.designsystem.icon.FormaIcons

/**
 * Zen Color Palette for tasks, rituals, and icons.
 */
val ZenColorPalette = listOf(
    Pair("#4E6542", "Matcha Green"),
    Pair("#637852", "Tea Leaf"),
    Pair("#8A9A86", "Sage"),
    Pair("#3D5A40", "Forest"),
    Pair("#D4A373", "Amber"),
    Pair("#C27D60", "Terracotta"),
    Pair("#B5838D", "Dusty Rose"),
    Pair("#DDB892", "Warm Sand"),
    Pair("#4A6B82", "Slate Blue"),
    Pair("#8377D1", "Lavender"),
    Pair("#495057", "Charcoal")
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun IconPickerDialog(
    selectedIcon: String,
    selectedColor: String? = null,
    onIconSelected: (String) -> Unit,
    onColorSelected: ((String?) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val colors = FormaTheme.colors
    val haptic = LocalHapticFeedback.current
    var activeColorHex by remember { mutableStateOf(selectedColor) }
    val activeColor = if (!activeColorHex.isNullOrBlank()) {
        try {
            Color(android.graphics.Color.parseColor(activeColorHex))
        } catch (_: Exception) {
            colors.accent
        }
    } else {
        colors.accent
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ICON & AESTHETIC",
                        style = FormaTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textTertiary,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Personalize Ritual",
                        style = FormaTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 20.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Color Palette Selector Row
            Text(
                text = "CHOOSE COLOR TONE",
                style = FormaTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textTertiary,
                letterSpacing = 1.2.sp,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Theme Default Accent Swatch
                val isThemePicked = activeColorHex.isNullOrBlank()
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.accent)
                        .border(
                            width = if (isThemePicked) 2.5.dp else 1.dp,
                            color = if (isThemePicked) colors.textPrimary else colors.accent.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .formaPressEffect(targetScale = 0.88f) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            activeColorHex = null
                            onColorSelected?.invoke(null)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isThemePicked) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Theme Accent",
                            tint = colors.onAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                ZenColorPalette.forEach { (hex, name) ->
                    val c = try {
                        Color(android.graphics.Color.parseColor(hex))
                    } catch (_: Exception) {
                        colors.accent
                    }
                    val isPicked = hex.equals(activeColorHex, ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(
                                width = if (isPicked) 2.5.dp else 1.dp,
                                color = if (isPicked) colors.textPrimary else c.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .formaPressEffect(targetScale = 0.88f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                activeColorHex = hex
                                onColorSelected?.invoke(hex)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPicked) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = name,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 2. Icon Grid with Live Color Tint
            Text(
                text = "CHOOSE EMBLEM",
                style = FormaTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textTertiary,
                letterSpacing = 1.2.sp,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FormaIcons.allIcons.forEach { (iconKey, label, category) ->
                    val isSelected = selectedIcon.equals(iconKey, ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) activeColor.copy(alpha = 0.22f)
                                else colors.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                width = if (isSelected) 1.8.dp else 1.dp,
                                color = if (isSelected) activeColor else colors.border.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .formaPressEffect(targetScale = 0.90f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onIconSelected(iconKey)
                                onColorSelected?.invoke(activeColorHex)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        FormaIcon(
                            iconKey = iconKey,
                            contentDescription = label,
                            tint = if (isSelected) activeColor else colors.textPrimary.copy(alpha = 0.85f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
