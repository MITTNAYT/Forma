package com.habitflow.app.domain.usecase

import com.habitflow.app.domain.model.HabitCompletion
import com.habitflow.app.domain.repository.HabitRepository
import java.util.UUID
import javax.inject.Inject

class ToggleHabitCompletionUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(habitId: String, date: String, currentlyCompleted: Boolean) {
        if (currentlyCompleted) {
            habitRepository.deleteCompletion(habitId, date)
        } else {
            habitRepository.recordCompletion(
                HabitCompletion(
                    id = UUID.randomUUID().toString(),
                    habitId = habitId,
                    date = date,
                    completedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
