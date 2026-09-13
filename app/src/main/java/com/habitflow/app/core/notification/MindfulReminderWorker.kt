package com.habitflow.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.habitflow.app.MainActivity
import com.habitflow.app.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MindfulReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val reminderType = inputData.getString(KEY_REMINDER_TYPE) ?: TYPE_MORNING
        showReminderNotification(reminderType)
        return Result.success()
    }

    private fun showReminderNotification(type: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel(notificationManager)

        val isMorning = type == TYPE_MORNING
        val title = if (isMorning) "🌅 Morning Alignment" else "🌙 Evening Sanctuary"
        val message = if (isMorning) {
            "Take a slow breath. Set your keystone intentions for a peaceful day."
        } else {
            "The day is drawing to a close. Reflect gently and release unfinished tasks to tomorrow."
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DESTINATION, if (isMorning) "morning" else "evening")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            if (isMorning) 401 else 402,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(if (isMorning) NOTIFICATION_ID_MORNING else NOTIFICATION_ID_EVENING, notification)
    }

    private fun createChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Forma Mindful Prompts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gentle morning alignment and evening sanctuary reflections."
                setShowBadge(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "forma_mindful_prompts"
        const val KEY_REMINDER_TYPE = "key_reminder_type"
        const val TYPE_MORNING = "type_morning"
        const val TYPE_EVENING = "type_evening"
        const val EXTRA_DESTINATION = "extra_destination"

        const val NOTIFICATION_ID_MORNING = 4001
        const val NOTIFICATION_ID_EVENING = 4002
    }
}
