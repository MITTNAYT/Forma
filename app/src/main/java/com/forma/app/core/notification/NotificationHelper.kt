package com.forma.app.core.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.forma.app.MainActivity
import com.forma.app.R
import com.forma.app.core.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val habitsChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_HABITS_ID,
                Constants.NOTIFICATION_CHANNEL_HABITS_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily habit schedule alerts"
            }

            val tasksChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_TASKS_ID,
                Constants.NOTIFICATION_CHANNEL_TASKS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Structured timeline task reminders"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(habitsChannel)
            notificationManager.createNotificationChannel(tasksChannel)
        }
    }

    fun showHabitNotification(habitId: String, habitName: String, habitIcon: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val completeIntent = Intent(context, HabitNotificationActionReceiver::class.java).apply {
            action = HabitNotificationActionReceiver.ACTION_COMPLETE
            putExtra(HabitNotificationActionReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(HabitNotificationActionReceiver.EXTRA_HABIT_NAME, habitName)
            putExtra(HabitNotificationActionReceiver.EXTRA_HABIT_ICON, habitIcon)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            (habitId + "_complete").hashCode(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, HabitNotificationActionReceiver::class.java).apply {
            action = HabitNotificationActionReceiver.ACTION_SNOOZE
            putExtra(HabitNotificationActionReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(HabitNotificationActionReceiver.EXTRA_HABIT_NAME, habitName)
            putExtra(HabitNotificationActionReceiver.EXTRA_HABIT_ICON, habitIcon)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (habitId + "_snooze").hashCode(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_HABITS_ID)
            .setSmallIcon(R.drawable.ic_stat_leaf)
            .setContentTitle(habitName)
            .setContentText("Time for your scheduled habit!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_stat_leaf, "✓ Done", completePendingIntent)
            .addAction(R.drawable.ic_stat_leaf, "+ 15m Snooze", snoozePendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(habitId.hashCode(), notification)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    fun showTaskNotification(taskId: String, taskTitle: String, timeText: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_TASKS_ID)
            .setSmallIcon(R.drawable.ic_stat_leaf)
            .setContentTitle("Upcoming: $taskTitle")
            .setContentText("Starts at $timeText")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(taskId.hashCode(), notification)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    fun scheduleHabitAlarm(habitId: String, habitName: String, habitIcon: String, minutesFromMidnight: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, HabitAlarmReceiver::class.java).apply {
            putExtra("HABIT_ID", habitId)
            putExtra("HABIT_NAME", habitName)
            putExtra("HABIT_ICON", habitIcon)
            putExtra("MINUTES_FROM_MIDNIGHT", minutesFromMidnight)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, minutesFromMidnight / 60)
            set(Calendar.MINUTE, minutesFromMidnight % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    fun cancelHabitAlarm(habitId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HabitAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
