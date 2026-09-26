package com.forma.app.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.forma.app.domain.repository.HabitRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class HabitNotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var toggleHabitCompletionUseCase: com.forma.app.domain.usecase.ToggleHabitCompletionUseCase

    @Inject
    lateinit var notificationHelper: NotificationHelper

    companion object {
        const val ACTION_COMPLETE = "com.forma.app.ACTION_COMPLETE_HABIT"
        const val ACTION_SNOOZE = "com.forma.app.ACTION_SNOOZE_HABIT"
        const val EXTRA_HABIT_ID = "EXTRA_HABIT_ID"
        const val EXTRA_HABIT_NAME = "EXTRA_HABIT_NAME"
        const val EXTRA_HABIT_ICON = "EXTRA_HABIT_ICON"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: return
        val habitName = intent.getStringExtra(EXTRA_HABIT_NAME) ?: "Ritual"
        val habitIcon = intent.getStringExtra(EXTRA_HABIT_ICON) ?: "spa"

        NotificationManagerCompat.from(context).cancel(habitId.hashCode())

        when (intent.action) {
            ACTION_COMPLETE -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val dateIso = com.forma.app.core.util.DateUtils.formatDateIso(com.forma.app.core.util.DateUtils.today())
                    toggleHabitCompletionUseCase(habitId, dateIso)
                    com.forma.app.core.widget.WidgetActionReceiver.refreshAllWidgets(context)
                }
            }
            ACTION_SNOOZE -> {
                val now = LocalTime.now()
                val minutesFromMidnight = (now.hour * 60 + now.minute + 15) % (24 * 60)
                notificationHelper.scheduleHabitAlarm(habitId, habitName, habitIcon, minutesFromMidnight)
            }
        }
    }
}
