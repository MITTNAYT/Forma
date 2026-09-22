package com.forma.app.ui.habits.components

import androidx.compose.runtime.Composable
import com.forma.app.domain.model.Habit
import com.forma.app.ui.timeline.CreationType
import com.forma.app.ui.timeline.components.AddEditTimelineSheet

/**
 * Compatibility wrapper delegating to the unified [AddEditTimelineSheet].
 */
@Composable
fun AddEditHabitDialog(
    initialHabit: Habit?,
    onDismiss: () -> Unit,
    onSave: (Habit) -> Unit = {},
    onDelete: ((Habit) -> Unit)? = null
) {
    AddEditTimelineSheet(
        itemId = initialHabit?.id,
        initialCreationType = CreationType.HABIT,
        onDismiss = onDismiss
    )
}
