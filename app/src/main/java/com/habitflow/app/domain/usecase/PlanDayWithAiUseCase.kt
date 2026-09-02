package com.habitflow.app.domain.usecase

import com.habitflow.app.domain.model.TimelineItem
import com.habitflow.app.domain.repository.AiPlannerRepository
import com.habitflow.app.domain.repository.BillingRepository
import com.habitflow.app.domain.repository.HabitRepository
import com.habitflow.app.domain.repository.TimelineRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PlanDayWithAiUseCase @Inject constructor(
    private val billingRepository: BillingRepository,
    private val habitRepository: HabitRepository,
    private val timelineRepository: TimelineRepository,
    private val aiPlannerRepository: AiPlannerRepository
) {
    sealed interface Result {
        data class Success(val generatedItems: List<TimelineItem>) : Result
        object RequiresPro : Result
        data class Error(val message: String) : Result
    }

    suspend operator fun invoke(date: String): Result {
        val isPro = billingRepository.isPro.first()
        if (!isPro) {
            return Result.RequiresPro
        }

        return try {
            val habits = habitRepository.getAllHabits(includeArchived = false).first()
            val existingItems = timelineRepository.getTimelineItemsForDate(date).first()

            val aiResult = aiPlannerRepository.suggestDayPlan(date, habits, existingItems)
            if (aiResult.isSuccess) {
                val proposedItems = aiResult.getOrThrow()
                // Save newly proposed items
                proposedItems.forEach { item ->
                    timelineRepository.insertTimelineItem(item)
                }
                Result.Success(proposedItems)
            } else {
                Result.Error(aiResult.exceptionOrNull()?.message ?: "AI day planning failed.")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Unknown error planning day.")
        }
    }
}
