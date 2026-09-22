package com.habitflow.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.habitflow.app.MainActivity
import com.habitflow.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val FOCUS_NOTIFICATION_CHANNEL_ID = "forma_focus_live_channel"
        const val FOCUS_NOTIFICATION_CHANNEL_NAME = "Forma Focus Flow Live"
        const val FOCUS_NOTIFICATION_ID = 4004

        const val ACTION_PAUSE = "com.habitflow.app.ACTION_FOCUS_PAUSE"
        const val ACTION_RESUME = "com.habitflow.app.ACTION_FOCUS_RESUME"
        const val ACTION_COMPLETE = "com.habitflow.app.ACTION_FOCUS_COMPLETE"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createFocusChannel()
    }

    private fun createFocusChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FOCUS_NOTIFICATION_CHANNEL_ID,
                FOCUS_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing live mindful focus timer"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun updateFocusNotification(
        taskTitle: String,
        remainingSeconds: Int,
        totalSeconds: Int,
        isRunning: Boolean
    ) {
        val mins = remainingSeconds / 60
        val secs = remainingSeconds % 60
        val timeFormatted = String.format("%02d:%02d", mins, secs)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "pomodoro")
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Pause / Resume
        val toggleActionIntent = Intent(context, FocusActionReceiver::class.java).apply {
            action = if (isRunning) ACTION_PAUSE else ACTION_RESUME
        }
        val togglePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            toggleActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val toggleActionTitle = if (isRunning) "Pause" else "Resume"

        // Action: Complete
        val completeActionIntent = Intent(context, FocusActionReceiver::class.java).apply {
            action = ACTION_COMPLETE
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            completeActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val progress = if (totalSeconds > 0) {
            ((totalSeconds - remainingSeconds).toFloat() / totalSeconds * 100).toInt()
        } else 0

        val builder = NotificationCompat.Builder(context, FOCUS_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_leaf)
            .setContentTitle(if (taskTitle.isBlank()) "Mindful Focus • $timeFormatted" else "$taskTitle • $timeFormatted")
            .setContentText(if (isRunning) "Flow in progress. Breathe gently." else "Session paused.")
            .setContentIntent(mainPendingIntent)
            .setOngoing(isRunning)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setProgress(100, progress, false)
            .addAction(0, toggleActionTitle, togglePendingIntent)
            .addAction(0, "Complete", completePendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)

        try {
            notificationManager.notify(FOCUS_NOTIFICATION_ID, builder.build())
        } catch (_: SecurityException) {
            // Notification permission might not be granted
        }
    }

    fun dismissFocusNotification() {
        notificationManager.cancel(FOCUS_NOTIFICATION_ID)
    }
}
