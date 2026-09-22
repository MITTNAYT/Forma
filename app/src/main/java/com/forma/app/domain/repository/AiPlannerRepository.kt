package com.forma.app.domain.repository

import com.forma.app.domain.model.AiGenerationResult
import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.TimelineItem

enum class AiPlanPreset(val title: String, val subtitle: String, val icon: String) {
    DEEP_WORK("Deep Focus Sprint", "Uninterrupted cognitive flow blocks with subtasks", "computer"),
    HEALTH_BALANCE("Health & Movement", "Circadian sunlight, hydration & posture resets", "spa"),
    EXAM_STUDY("Study & Retention", "Active recall, flashcards & timed mock tests", "school"),
    PRODUCTIVITY_SPRINT("Power Sprint", "High-leverage milestone execution & inbox zero", "bolt"),
    CREATIVE_FLOW("Creative Flow", "Unstructured ideation, deep drafting & synthesis", "brush"),
    MINDFUL_WEEKEND("Mindful Weekend", "Restorative leisure, reading & nature connection", "nature")
}

interface AiPlannerRepository {
    /**
     * Generates an intelligent, energy-balanced plan with tasks and suggested habits based on prompt and preset.
     */
    suspend fun generateComprehensivePlan(
        prompt: String,
        date: String,
        habits: List<Habit>,
        existingItems: List<TimelineItem>,
        preset: AiPlanPreset? = null
    ): Result<AiGenerationResult>

    /**
     * Decomposes a major goal into daily scheduled tasks and supporting lifelong habits.
     */
    suspend fun decomposeGoal(
        goal: String,
        date: String,
        currentHabits: List<Habit>
    ): Result<AiGenerationResult>

    /**
     * Suggests a tailored pack of habits matching a user's intent.
     */
    suspend fun suggestHabitPacks(
        intent: String,
        currentHabits: List<Habit>
    ): Result<List<Habit>>

    /**
     * Legacy day planner signature for backward compatibility.
     */
    suspend fun suggestDayPlan(
        date: String,
        habits: List<Habit>,
        existingItems: List<TimelineItem>,
        preset: AiPlanPreset = AiPlanPreset.DEEP_WORK
    ): Result<List<TimelineItem>>
}
