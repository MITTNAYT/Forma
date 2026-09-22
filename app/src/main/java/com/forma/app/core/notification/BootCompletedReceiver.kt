package com.forma.app.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.forma.app.domain.repository.HabitRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var habitRepository: HabitRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val habits = habitRepository.getAllHabits(includeArchived = false).first()
                habits.forEach { habit ->
                    habit.reminderTimeMinutes?.let { minutes ->
                        notificationHelper.scheduleHabitAlarm(
                            habitId = habit.id,
                            habitName = habit.name,
                            habitIcon = habit.icon,
                            minutesFromMidnight = minutes
                        )
                    }
                }
            }
        }
    }
}
