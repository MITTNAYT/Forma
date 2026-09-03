package com.habitflow.app.domain.repository

import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.TimelineItem

enum class AiPlanPreset(val title: String, val subtitle: String, val icon: String) {
    DEEP_WORK("Deep Focus Sprint", "Uninterrupted cognitive flow blocks", "computer"),
    HEALTH_BALANCE("Health & Movement", "Hydration, walking & mindful pauses", "spa"),
    EXAM_STUDY("Study & Retention", "Active recall & structured sprint intervals", "school")
}

interface AiPlannerRepository {
    /**
     * Generates an intelligent, energy-balanced time-blocked schedule for a given day.
     * Behind an interface so real LLM API (OpenAI / Gemini / Anthropic) can be easily plugged in.
     */
    suspend fun suggestDayPlan(
        date: String,
        habits: List<Habit>,
        existingItems: List<TimelineItem>,
        preset: AiPlanPreset = AiPlanPreset.DEEP_WORK
    ): Result<List<TimelineItem>>
}
