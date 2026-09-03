package com.habitflow.app.data.repository

import com.habitflow.app.core.util.DateUtils
import com.habitflow.app.domain.model.EnergyLevel
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.Subtask
import com.habitflow.app.domain.model.TimelineItem
import com.habitflow.app.domain.repository.AiPlanPreset
import com.habitflow.app.domain.repository.AiPlannerRepository
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiPlannerRepositoryImpl @Inject constructor() : AiPlannerRepository {

    override suspend fun suggestDayPlan(
        date: String,
        habits: List<Habit>,
        existingItems: List<TimelineItem>,
        preset: AiPlanPreset
    ): Result<List<TimelineItem>> {
        // Simulate intelligent planning network latency
        delay(800)

        val generatedItems = mutableListOf<TimelineItem>()

        when (preset) {
            AiPlanPreset.DEEP_WORK -> {
                generatedItems.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Deep Focus: High-Priority Project",
                        date = date,
                        startTime = "09:30",
                        endTime = "11:30",
                        icon = "computer",
                        colorTag = "#637852",
                        notes = "AI Planned: Protect morning peak cognitive energy for uninterrupted output.",
                        subtasks = listOf(
                            Subtask(title = "Close distractions & email tabs", completed = false),
                            Subtask(title = "Execute core project milestone", completed = false)
                        ),
                        reminderMinutesBefore = 10
                    )
                )
                generatedItems.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Midday Mindful Reset & Walk",
                        date = date,
                        startTime = "13:00",
                        endTime = "13:45",
                        icon = "self_improvement",
                        colorTag = "#8F9E8B",
                        notes = "AI Planned: Step outside, disconnect from screens, replenish mental bandwidth.",
                        subtasks = listOf(
                            Subtask(title = "15 min outdoor walk", completed = false),
                            Subtask(title = "Hydrate & light stretch", completed = false)
                        )
                    )
                )
                generatedItems.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Afternoon Admin & Comms Sprint",
                        date = date,
                        startTime = "15:30",
                        endTime = "16:30",
                        icon = "mail",
                        colorTag = "#4A5568",
                        notes = "AI Planned: Batch replies, inbox zero, and task grooming.",
                        subtasks = listOf(
                            Subtask(title = "Process urgent messages", completed = false),
                            Subtask(title = "Review tomorrow's priorities", completed = false)
                        )
                    )
                )
            }
            AiPlanPreset.HEALTH_BALANCE -> {
                generatedItems.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Morning Sunlight & Hydration",
                        date = date,
                        startTime = "07:30",
                        endTime = "08:15",
                        icon = "wb_sunny",
                        colorTag = "#D4A373",
                        notes = "AI Planned: Get early morning photon exposure to calibrate circadian rhythm.",
                        subtasks = listOf(
                            Subtask(title = "500ml water with electrolytes", completed = false),
                            Subtask(title = "15 min outdoor light walk", completed = false)
                        )
                    )
                )
                generatedItems.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Mindful Mobility & Posture Break",
                        date = date,
                        startTime = "12:30",
                        endTime = "13:00",
                        icon = "spa",
                        colorTag = "#637852",
                        notes = "AI Planned: Relieve neck, spine, and hip flexor tension.",
                        subtasks = listOf(
                            Subtask(title = "Thoracic spine extensions", completed = false),
                            Subtask(title = "Deep diaphragmatic breathing", completed = false)
                        )
                    )
                )
                generatedItems.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Evening Digital Wind-Down",
                        date = date,
                        startTime = "21:00",
                        endTime = "21:45",
                        icon = "nights_stay",
                        colorTag = "#2D3748",
                        notes = "AI Planned: Dim artificial overhead lights; transition to restorative reading.",
                        subtasks = listOf(
                            Subtask(title = "No screens 45 min before sleep", completed = false),
                            Subtask(title = "Reflective daily journaling", completed = false)
                        )
                    )
                )
            }
            AiPlanPreset.EXAM_STUDY -> {
                generatedItems.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Active Recall & Core Concepts",
                        date = date,
                        startTime = "09:00",
                        endTime = "10:30",
                        icon = "school",
                        colorTag = "#E67E22",
                        notes = "AI Planned: Test yourself on high-yield exam chapters without looking at notes.",
                        subtasks = listOf(
                            Subtask(title = "Complete chapter 1-3 recall", completed = false),
                            Subtask(title = "Identify weak knowledge gaps", completed = false)
                        ),
                        reminderMinutesBefore = 10
                    )
                )
                generatedItems.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Flashcards & Spaced Repetition",
                        date = date,
                        startTime = "14:00",
                        endTime = "15:00",
                        icon = "menu_book",
                        colorTag = "#3498DB",
                        notes = "AI Planned: Review key definitions and formulas with active retention.",
                        subtasks = listOf(
                            Subtask(title = "Review 50 due flashcards", completed = false),
                            Subtask(title = "Star difficult problem sets", completed = false)
                        )
                    )
                )
                generatedItems.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Timed Mock Practice Session",
                        date = date,
                        startTime = "16:30",
                        endTime = "18:00",
                        icon = "timer",
                        colorTag = "#637852",
                        notes = "AI Planned: Simulate authentic exam conditions with strict timer.",
                        subtasks = listOf(
                            Subtask(title = "Full timed test section", completed = false),
                            Subtask(title = "Error log & answer review", completed = false)
                        )
                    )
                )
            }
        }

        return Result.success(generatedItems)
    }
}
