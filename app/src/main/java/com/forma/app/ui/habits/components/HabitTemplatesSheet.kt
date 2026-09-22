package com.forma.app.ui.habits.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.motion.formaPressEffect
import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.HabitTemplate
import com.forma.app.domain.model.HabitTemplateCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTemplatesSheet(
    onAddHabit: (Habit) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = FormaTheme.colors
    val haptics = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedCategory by remember { mutableStateOf<HabitTemplateCategory?>(null) }
    val addedTemplateIds = remember { mutableStateMapOf<String, Boolean>() }

    val filteredTemplates = remember(selectedCategory) {
        if (selectedCategory == null) {
            HabitTemplate.CURATED_TEMPLATES
        } else {
            HabitTemplate.CURATED_TEMPLATES.filter { it.category == selectedCategory }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        contentColor = colors.textPrimary,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 6.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(colors.border.copy(alpha = 0.6f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RITUAL INSPIRATION",
                        style = FormaTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textTertiary,
                        letterSpacing = 1.4.sp,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Habit Templates",
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 22.sp,
                        letterSpacing = (-0.5).sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                        .formaPressEffect(targetScale = 0.92f) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDismiss()
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

            // Category Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" chip
                val isAllSelected = selectedCategory == null
                val allBg by animateColorAsState(
                    targetValue = if (isAllSelected) colors.accent else colors.surface,
                    label = "all_chip_bg"
                )
                val allFg by animateColorAsState(
                    targetValue = if (isAllSelected) colors.onAccent else colors.textSecondary,
                    label = "all_chip_fg"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(allBg)
                        .border(
                            1.dp,
                            if (isAllSelected) colors.accent else colors.border.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .formaPressEffect(targetScale = 0.94f) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedCategory = null
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "All Rituals (${HabitTemplate.CURATED_TEMPLATES.size})",
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                        color = allFg,
                        fontSize = 12.sp
                    )
                }

                // Category chips
                HabitTemplateCategory.values().forEach { cat ->
                    val isSelected = selectedCategory == cat
                    val bg by animateColorAsState(
                        targetValue = if (isSelected) colors.accent else colors.surface,
                        label = "cat_chip_bg"
                    )
                    val fg by animateColorAsState(
                        targetValue = if (isSelected) colors.onAccent else colors.textSecondary,
                        label = "cat_chip_fg"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg)
                            .border(
                                1.dp,
                                if (isSelected) colors.accent else colors.border.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                            .formaPressEffect(targetScale = 0.94f) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedCategory = cat
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat.displayName,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = fg,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Templates List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = filteredTemplates,
                    key = { it.id }
                ) { template ->
                    val isAdded = addedTemplateIds[template.id] == true

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.surface)
                            .border(
                                1.dp,
                                if (isAdded) colors.accent.copy(alpha = 0.6f) else colors.border.copy(alpha = 0.4f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            // Top Row: Category badge & duration
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(colors.accentSoft)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = template.category.displayName.uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = colors.accent,
                                            fontSize = 9.5.sp,
                                            letterSpacing = 0.6.sp
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(colors.surfaceVariant)
                                            .padding(horizontal = 7.dp, vertical = 3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.Schedule,
                                                contentDescription = null,
                                                tint = colors.textSecondary,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${template.durationMinutes}m",
                                                fontWeight = FontWeight.Medium,
                                                color = colors.textSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }

                                // Add Button
                                val btnBg by animateColorAsState(
                                    targetValue = if (isAdded) colors.accentSoft else colors.accent,
                                    label = "add_btn_bg"
                                )
                                val btnFg by animateColorAsState(
                                    targetValue = if (isAdded) colors.accent else colors.onAccent,
                                    label = "add_btn_fg"
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(btnBg)
                                        .border(
                                            1.dp,
                                            if (isAdded) colors.accent.copy(alpha = 0.4f) else Color.Transparent,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .formaPressEffect(targetScale = 0.94f) {
                                            if (!isAdded) {
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                addedTemplateIds[template.id] = true
                                                onAddHabit(template.toHabit())
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isAdded) Icons.Rounded.Check else Icons.Rounded.Add,
                                            contentDescription = null,
                                            tint = btnFg,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isAdded) "Added" else "Add Ritual",
                                            fontWeight = FontWeight.Bold,
                                            color = btnFg,
                                            fontSize = 11.5.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Title
                            Text(
                                text = template.title,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 16.sp,
                                letterSpacing = (-0.3).sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Description
                            Text(
                                text = template.description,
                                color = colors.textSecondary,
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp
                            )

                            // Stacked cue if present
                            if (template.stackedCueText != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.surfaceVariant.copy(alpha = 0.6f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "HABIT STACK: ",
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textTertiary,
                                        fontSize = 9.5.sp,
                                        letterSpacing = 0.6.sp
                                    )
                                    Text(
                                        text = "\"${template.stackedCueText}\"",
                                        color = colors.textPrimary,
                                        fontSize = 10.5.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Science note box
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.accentSoft.copy(alpha = 0.5f))
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Psychology,
                                    contentDescription = "Science",
                                    tint = colors.accent,
                                    modifier = Modifier
                                        .size(15.dp)
                                        .padding(top = 1.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = template.scienceNote,
                                    color = colors.accent,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 10.5.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
