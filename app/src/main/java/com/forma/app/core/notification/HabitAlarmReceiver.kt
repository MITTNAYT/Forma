package com.forma.app.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HabitAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra("HABIT_ID") ?: return
        val habitName = intent.getStringExtra("HABIT_NAME") ?: "Habit"
        val habitIcon = intent.getStringExtra("HABIT_ICON") ?: "target"
        val minutesFromMidnight = intent.getIntExtra("MINUTES_FROM_MIDNIGHT", -1)

        notificationHelper.showHabitNotification(habitId, habitName, habitIcon)

        // Re-schedule for the next day so daily alarms recur automatically
        if (minutesFromMidnight >= 0) {
            notificationHelper.scheduleHabitAlarm(habitId, habitName, habitIcon, minutesFromMidnight)
        }
    }
}
