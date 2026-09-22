package com.forma.app.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed interface FocusActionEvent {
    object Pause : FocusActionEvent
    object Resume : FocusActionEvent
    object Complete : FocusActionEvent
}

@Singleton
class FocusActionBus @Inject constructor() {
    private val _events = MutableSharedFlow<FocusActionEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<FocusActionEvent> = _events.asSharedFlow()

    fun emit(event: FocusActionEvent) {
        _events.tryEmit(event)
    }
}

@AndroidEntryPoint
class FocusActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var focusActionBus: FocusActionBus

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            FocusNotificationManager.ACTION_PAUSE -> focusActionBus.emit(FocusActionEvent.Pause)
            FocusNotificationManager.ACTION_RESUME -> focusActionBus.emit(FocusActionEvent.Resume)
            FocusNotificationManager.ACTION_COMPLETE -> focusActionBus.emit(FocusActionEvent.Complete)
        }
    }
}
