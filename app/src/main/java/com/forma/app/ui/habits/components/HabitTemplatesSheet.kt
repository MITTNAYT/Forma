package com.forma.app.ui.habits.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forma.app.core.designsystem.FormaTheme
import com.forma.app.core.designsystem.icon.FormaIcon
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
        containerColor = colors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.border)
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
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "READY-TO-USE ROUTINES",
                        style = FormaTheme.typography.labelSmall.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            letterSpacing = 1.4.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Curated Habit Templates",
                        style = FormaTheme.typography.titleMedium.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ),
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 20.sp
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
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" chip
                val isAllSelected = selectedCategory == null
                val allBg by animateColorAsState(
                    targetValue = if (isAllSelected) colors.accent else colors.surfaceVariant,
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
                            if (isAllSelected) colors.accent else colors.border.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                        .formaPressEffect(targetScale = 0.94f) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedCategory = null
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "All (${HabitTemplate.CURATED_TEMPLATES.size})",
                        style = FormaTheme.typography.labelSmall.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ),
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                        color = allFg,
                        fontSize = 11.5.sp
                    )
                }

                // Category chips
                HabitTemplateCategory.values().forEach { cat ->
                    val isSelected = selectedCategory == cat
                    val bg by animateColorAsState(
                        targetValue = if (isSelected) colors.accent else colors.surfaceVariant,
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
                                if (isSelected) colors.accent else colors.border.copy(alpha = 0.4f),
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
                            style = FormaTheme.typography.labelSmall.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = fg,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Templates List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = filteredTemplates,
                    key = { it.id }
                ) { template ->
                    val isAdded = addedTemplateIds[template.id] == true
                    val parsedAccent = remember(template.colorTag) {
                        try {
                            Color(android.graphics.Color.parseColor(template.colorTag))
                        } catch (_: Exception) {
                            colors.accent
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors.surface)
                            .border(
                                width = if (isAdded) 1.5.dp else 1.dp,
                                color = if (isAdded) parsedAccent.copy(alpha = 0.65f) else colors.border.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            // Top Row: Icon, Title & 1-Tap Add Action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Custom Squircle Icon
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(parsedAccent.copy(alpha = 0.12f))
                                            .border(1.dp, parsedAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        FormaIcon(
                                            iconKey = template.icon,
                                            contentDescription = template.title,
                                            tint = parsedAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = template.title,
                                            style = FormaTheme.typography.titleMedium.copy(
                                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary,
                                            fontSize = 15.sp,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(parsedAccent.copy(alpha = 0.14f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = template.timeOfDay.displayName.uppercase(),
                                                    style = FormaTheme.typography.labelSmall.copy(
                                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                    ),
                                                    fontWeight = FontWeight.Bold,
                                                    color = parsedAccent,
                                                    fontSize = 9.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${template.durationMinutes} min",
                                                style = FormaTheme.typography.labelSmall.copy(
                                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                ),
                                                color = colors.textTertiary,
                                                fontSize = 10.5.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Prominent 1-Tap Add Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(if (isAdded) parsedAccent else parsedAccent.copy(alpha = 0.14f))
                                        .border(1.dp, parsedAccent, RoundedCornerShape(11.dp))
                                        .formaPressEffect(targetScale = 0.92f) {
                                            if (!isAdded) {
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                addedTemplateIds[template.id] = true
                                                onAddHabit(template.toHabit())
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isAdded) Icons.Rounded.Check else Icons.Rounded.Add,
                                            contentDescription = null,
                                            tint = if (isAdded) colors.onAccent else parsedAccent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isAdded) "Added" else "Add Habit",
                                            style = FormaTheme.typography.labelSmall.copy(
                                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAdded) colors.onAccent else parsedAccent,
                                            fontSize = 11.5.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Benefit description
                            Text(
                                text = template.description,
                                style = FormaTheme.typography.bodySmall.copy(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeight = 16.sp
                                ),
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )

                            // Clean micro-steps preview
                            if (template.subtasks.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    template.subtasks.forEach { sub ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(parsedAccent)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = sub.title,
                                                style = FormaTheme.typography.bodySmall.copy(
                                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                                ),
                                                fontSize = 11.5.sp,
                                                color = colors.textSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            // Simple benefit pill
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Why it works: ${template.simpleBenefit}",
                                style = FormaTheme.typography.labelSmall.copy(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                ),
                                color = colors.textTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
