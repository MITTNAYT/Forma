package com.forma.app.ui.today.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.motion.formaPressEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InlineQuickEntryBar(
    onQuickAdd: (title: String, isHabit: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Type a new ritual or intention..."
) {
    val colors = FormaTheme.colors
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var text by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    var isHabitMode by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) colors.accent.copy(alpha = 0.8f) else colors.border.copy(alpha = 0.55f),
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "inline_border_color"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 1.5.dp else 1.dp,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "inline_border_width"
    )
    val containerBg by animateColorAsState(
        targetValue = if (isFocused) colors.surface else colors.surfaceVariant.copy(alpha = 0.45f),
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "inline_bg_color"
    )

    fun submit() {
        if (text.isNotBlank()) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val submittedText = text.trim()
            text = ""
            onQuickAdd(submittedText, isHabitMode)
            focusManager.clearFocus()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .clip(RoundedCornerShape(22.dp))
            .background(containerBg)
            .border(borderWidth, borderColor, RoundedCornerShape(22.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusRequester.requestFocus()
            }
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mode Toggle Icon (Ritual vs Single Intention)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isHabitMode) colors.accentSoft else colors.surfaceVariant)
                        .border(
                            1.dp,
                            if (isHabitMode) colors.accent.copy(alpha = 0.4f) else colors.border.copy(alpha = 0.4f),
                            RoundedCornerShape(10.dp)
                        )
                        .formaPressEffect(targetScale = 0.90f) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isHabitMode = !isHabitMode
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isHabitMode) Icons.Rounded.Spa else Icons.Rounded.PushPin,
                        contentDescription = if (isHabitMode) "Ritual Mode" else "Intention Mode",
                        tint = if (isHabitMode) colors.accent else colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Inline In-Place BasicTextField
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                            if (focusState.isFocused) {
                                coroutineScope.launch {
                                    delay(200)
                                    bringIntoViewRequester.bringIntoView()
                                }
                            }
                        },
                    textStyle = FormaTheme.typography.bodyMedium.copy(
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        autoCorrectEnabled = true,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { submit() }
                    ),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (text.isEmpty()) {
                                Text(
                                    text = if (isHabitMode) "Add recurring daily ritual..." else placeholder,
                                    style = FormaTheme.typography.bodyMedium.copy(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        lineHeight = 20.sp
                                    ),
                                    color = colors.textTertiary,
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                // Submit Button
                AnimatedVisibility(
                    visible = text.isNotBlank(),
                    enter = fadeIn(tween(140)) + scaleIn(tween(140)),
                    exit = fadeOut(tween(100)) + scaleOut(tween(100))
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                            .formaPressEffect(targetScale = 0.90f) {
                                submit()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = "Save",
                            tint = colors.onAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Expanded Mode Pill Indicator when focused
            AnimatedVisibility(
                visible = isFocused,
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(120))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Type Switcher Chips
                        listOf(
                            Pair(false, "Single Intention"),
                            Pair(true, "Daily Ritual")
                        ).forEach { (habitOption, label) ->
                            val active = isHabitMode == habitOption
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) colors.accentSoft else colors.surface)
                                    .border(
                                        1.dp,
                                        if (active) colors.accent.copy(alpha = 0.4f) else colors.border.copy(alpha = 0.4f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isHabitMode = habitOption
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = FormaTheme.typography.labelSmall,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                    color = if (active) colors.accent else colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = "↵ Enter to save",
                        style = FormaTheme.typography.labelSmall,
                        color = colors.textTertiary,
                        fontSize = 10.5.sp
                    )
                }
            }
        }
    }
}
