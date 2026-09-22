package com.habitflow.app.core.designsystem.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Universal Keyboard-Avoiding & Outside-Dismiss Container.
 *
 * Automatically keeps active focused inputs visible above the keyboard without layout jumps
 * and dismisses the keyboard gracefully on outside taps.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FormaKeyboardAvoidingContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(bringIntoViewRequester: BringIntoViewRequester, onFieldFocused: () -> Unit) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    val onFieldFocused: () -> Unit = {
        coroutineScope.launch {
            delay(180)
            bringIntoViewRequester.bringIntoView()
        }
    }

    Box(
        modifier = modifier
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .bringIntoViewRequester(bringIntoViewRequester)
    ) {
        content(bringIntoViewRequester, onFieldFocused)
    }
}
