package com.habitflow.app.core.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object MindfulReminderScheduler {

    private const val WORK_MORNING = "forma_work_morning_alignment"
    private const val WORK_EVENING = "forma_work_evening_sanctuary"

    fun scheduleMorningReminder(context: Context, hour: Int = 8, minute: Int = 0) {
        val initialDelay = calculateInitialDelay(hour, minute)
        val request = PeriodicWorkRequestBuilder<MindfulReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(MindfulReminderWorker.KEY_REMINDER_TYPE to MindfulReminderWorker.TYPE_MORNING))
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_MORNING,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun scheduleEveningReminder(context: Context, hour: Int = 21, minute: Int = 30) {
        val initialDelay = calculateInitialDelay(hour, minute)
        val request = PeriodicWorkRequestBuilder<MindfulReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(MindfulReminderWorker.KEY_REMINDER_TYPE to MindfulReminderWorker.TYPE_EVENING))
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_EVENING,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelMorningReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_MORNING)
    }

    fun cancelEveningReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_EVENING)
    }

    private fun calculateInitialDelay(targetHour: Int, targetMinute: Int): Duration {
        val now = LocalDateTime.now()
        var target = now.with(LocalTime.of(targetHour, targetMinute, 0))
        if (now.isAfter(target)) {
            target = target.plusDays(1)
        }
        return Duration.between(now, target)
    }
}
