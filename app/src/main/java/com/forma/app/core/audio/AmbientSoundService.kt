package com.forma.app.core.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.forma.app.MainActivity
import com.forma.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AmbientSoundService : Service() {

    @Inject
    lateinit var ambientSoundManager: AmbientSoundManager

    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var sleepTimerJob: Job? = null
    private var activeSound: AmbientSound = AmbientSound.OFF

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_PLAY
        when (action) {
            ACTION_STOP -> {
                stopAmbient()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_PLAY -> {
                val soundName = intent?.getStringExtra(EXTRA_SOUND) ?: AmbientSound.RAIN.name
                val sound = try { AmbientSound.valueOf(soundName) } catch (_: Exception) { AmbientSound.RAIN }
                val timerMinutes = intent?.getIntExtra(EXTRA_TIMER_MINUTES, 0) ?: 0

                activeSound = sound
                ambientSoundManager.play(sound)
                val notification = buildNotification(sound, timerMinutes)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }

                sleepTimerJob?.cancel()
                if (timerMinutes > 0) {
                    sleepTimerJob = serviceScope.launch {
                        delay(timerMinutes * 60 * 1000L)
                        stopAmbient()
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun stopAmbient() {
        sleepTimerJob?.cancel()
        ambientSoundManager.stop()
        activeSound = AmbientSound.OFF
    }

    override fun onDestroy() {
        stopAmbient()
        super.onDestroy()
    }

    private fun buildNotification(sound: AmbientSound, timerMinutes: Int): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AmbientSoundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timerSubtext = if (timerMinutes > 0) " • Timer: ${timerMinutes}m" else ""

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(sound.displayName)
            .setContentText("Forma Ambient Soundscape${timerSubtext}")
            .setSubText("Mindful Audio")
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopPendingIntent
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Forma Ambient Soundscape",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls background mindful ambient soundscapes."
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "forma_ambient_sound"
        private const val NOTIFICATION_ID = 3001

        const val ACTION_PLAY = "com.forma.app.action.PLAY_AMBIENT"
        const val ACTION_STOP = "com.forma.app.action.STOP_AMBIENT"
        const val EXTRA_SOUND = "extra_sound"
        const val EXTRA_TIMER_MINUTES = "extra_timer_minutes"

        fun start(context: Context, sound: AmbientSound, timerMinutes: Int = 0) {
            val intent = Intent(context, AmbientSoundService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_SOUND, sound.name)
                putExtra(EXTRA_TIMER_MINUTES, timerMinutes)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AmbientSoundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
