package com.habitflow.app.domain.usecase

import com.habitflow.app.domain.model.AiGenerationResult
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.TimelineItem
import com.habitflow.app.domain.repository.AiPlanPreset
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
        data class Success(val generatedItems: List<TimelineItem>, val generationResult: AiGenerationResult? = null) : Result
        object RequiresPro : Result
        data class Error(val message: String) : Result
    }

    suspend operator fun invoke(
        date: String,
        preset: AiPlanPreset = AiPlanPreset.DEEP_WORK,
        customPrompt: String = ""
    ): Result {
        return try {
            val habits = habitRepository.getAllHabits(includeArchived = false).first()
            val existingItems = timelineRepository.getTimelineItemsForDate(date).first()

            val aiResult = aiPlannerRepository.generateComprehensivePlan(
                prompt = customPrompt,
                date = date,
                habits = habits,
                existingItems = existingItems,
                preset = preset
            )

            if (aiResult.isSuccess) {
                val fullResult = aiResult.getOrThrow()
                // Save newly proposed items
                fullResult.tasks.forEach { item ->
                    timelineRepository.insertTimelineItem(item)
                }
                Result.Success(fullResult.tasks, fullResult)
            } else {
                Result.Error(aiResult.exceptionOrNull()?.message ?: "AI day planning failed.")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Unknown error planning day.")
        }
    }

    suspend fun generatePreview(
        date: String,
        prompt: String = "",
        preset: AiPlanPreset? = null
    ): com.habitflow.app.domain.model.AiGenerationResult {
        val habits = habitRepository.getAllHabits(includeArchived = false).first()
        val existingItems = timelineRepository.getTimelineItemsForDate(date).first()
        return aiPlannerRepository.generateComprehensivePlan(
            prompt = prompt,
            date = date,
            habits = habits,
            existingItems = existingItems,
            preset = preset
        ).getOrThrow()
    }

    suspend fun decomposeGoal(
        goal: String,
        date: String
    ): com.habitflow.app.domain.model.AiGenerationResult {
        val habits = habitRepository.getAllHabits(includeArchived = false).first()
        return aiPlannerRepository.decomposeGoal(goal, date, habits).getOrThrow()
    }

    suspend fun suggestHabitPacks(intent: String): List<Habit> {
        val habits = habitRepository.getAllHabits(includeArchived = false).first()
        return aiPlannerRepository.suggestHabitPacks(intent, habits).getOrThrow()
    }
}
