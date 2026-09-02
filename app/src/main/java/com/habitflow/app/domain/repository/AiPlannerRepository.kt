package com.habitflow.app.domain.repository

import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.TimelineItem

interface AiPlannerRepository {
    /**
     * Generates an intelligent, energy-balanced time-blocked schedule for a given day.
     * Behind an interface so real LLM API (OpenAI / Gemini / Anthropic) can be easily plugged in.
     */
    suspend fun suggestDayPlan(
        date: String,
        habits: List<Habit>,
        existingItems: List<TimelineItem>
    ): Result<List<TimelineItem>>
}
