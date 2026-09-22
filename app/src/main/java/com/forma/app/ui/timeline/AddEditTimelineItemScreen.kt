package com.forma.app.ui.timeline

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.forma.app.ui.timeline.components.AddEditTimelineSheet

/**
 * Decommissioned standalone typing page.
 * Delegates seamlessly to the fluid contextual bottom sheet [AddEditTimelineSheet],
 * ensuring zero screen jumps, preserved background context, and responsive keyboard avoidance.
 */
@Composable
fun AddEditTimelineItemScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditTimelineViewModel = hiltViewModel()
) {
    AddEditTimelineSheet(
        onDismiss = onNavigateBack,
        viewModel = viewModel
    )
}
