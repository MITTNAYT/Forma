package com.forma.app.core.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.forma.app.domain.repository.HabitRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class HabitReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val habitRepository: HabitRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val habitId = inputData.getString("HABIT_ID") ?: return Result.failure()
        val habit = habitRepository.getHabitById(habitId).first() ?: return Result.failure()

        if (!habit.archived) {
            notificationHelper.showHabitNotification(habit.id, habit.name, habit.icon)
        }
        return Result.success()
    }
}
