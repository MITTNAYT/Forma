package com.forma.app.domain.model

import androidx.compose.runtime.Immutable
import java.util.UUID

enum class HabitTemplateCategory(val displayName: String, val icon: String) {
    ESSENTIALS("Daily Essentials", "sun"),
    HEALTH_BODY("Health & Body", "gym"),
    FOCUS_WORK("Focus & Work", "target"),
    MIND_REST("Mind & Rest", "spa")
}

@Immutable
data class HabitTemplate(
    val id: String,
    val title: String,
    val description: String,
    val category: HabitTemplateCategory,
    val icon: String = "target",
    val colorTag: String = "#C58A24", // Warm Terracotta / Sand Gold
    val timeOfDay: TimeOfDay = TimeOfDay.ANYTIME,
    val energyLevel: EnergyLevel = EnergyLevel.MEDIUM,
    val durationMinutes: Int = 15,
    val reminderTimeMinutes: Int? = null,
    val simpleBenefit: String,
    val stackedCueText: String? = null,
    val subtasks: List<Subtask> = emptyList()
) {
    fun toHabit(): Habit = Habit(
        id = UUID.randomUUID().toString(),
        name = title,
        icon = icon,
        colorTag = colorTag,
        timeOfDay = timeOfDay,
        energyLevel = energyLevel,
        repeatDays = setOf(1, 2, 3, 4, 5, 6, 7),
        reminderTimeMinutes = reminderTimeMinutes ?: when (timeOfDay) {
            TimeOfDay.MORNING -> 8 * 60 // 8:00 AM
            TimeOfDay.AFTERNOON -> 14 * 60 // 2:00 PM
            TimeOfDay.EVENING -> 21 * 60 // 9:00 PM
            TimeOfDay.ANYTIME -> null
        },
        stackedCueText = stackedCueText,
        isIndefinite = true,
        subtasks = subtasks
    )

    companion object {
        val CURATED_TEMPLATES: List<HabitTemplate> = listOf(
            // ── 1. Daily Essentials ──────────────────────────────────────
            HabitTemplate(
                id = "essentials_morning_water",
                title = "Morning Hydration (500ml)",
                description = "Drink a large glass of clean water right after getting out of bed.",
                category = HabitTemplateCategory.ESSENTIALS,
                icon = "water",
                colorTag = "#4A7C59", // Sage Green
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 2,
                reminderTimeMinutes = 7 * 60 + 30,
                simpleBenefit = "Rehydrates brain and organs after 8 hours of sleep.",
                stackedCueText = "Right after stepping out of bed",
                subtasks = listOf(
                    Subtask(title = "Pour 500ml fresh water", completed = false),
                    Subtask(title = "Drink fully before coffee or breakfast", completed = false)
                )
            ),
            HabitTemplate(
                id = "essentials_daily_walk",
                title = "Daily 20-Minute Walk",
                description = "Get outside, move your legs, and clear mental fog.",
                category = HabitTemplateCategory.ESSENTIALS,
                icon = "walk",
                colorTag = "#C58A24", // Terracotta
                timeOfDay = TimeOfDay.AFTERNOON,
                energyLevel = EnergyLevel.MEDIUM,
                durationMinutes = 20,
                reminderTimeMinutes = 12 * 60 + 30,
                simpleBenefit = "Boosts cardiovascular flow and lifts daily mood.",
                stackedCueText = "After finishing lunch",
                subtasks = listOf(
                    Subtask(title = "Put on comfortable walking shoes", completed = false),
                    Subtask(title = "Walk without scrolling on phone", completed = false)
                )
            ),
            HabitTemplate(
                id = "essentials_read_15m",
                title = "Daily Reading (15 Pages)",
                description = "Read a book for 15 quiet, undistracted minutes.",
                category = HabitTemplateCategory.ESSENTIALS,
                icon = "book",
                colorTag = "#8D5B4C", // Cedar Wood
                timeOfDay = TimeOfDay.EVENING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 15,
                reminderTimeMinutes = 21 * 60,
                simpleBenefit = "Expands knowledge and calms the nervous system.",
                stackedCueText = "Before going to sleep",
                subtasks = listOf(
                    Subtask(title = "Open current book", completed = false),
                    Subtask(title = "Read 15 pages in peace", completed = false)
                )
            ),
            HabitTemplate(
                id = "essentials_plan_tomorrow",
                title = "Plan Tomorrow Tonight",
                description = "Write down your top 3 priorities for tomorrow before bed.",
                category = HabitTemplateCategory.ESSENTIALS,
                icon = "journal",
                colorTag = "#D4AF37", // Warm Gold
                timeOfDay = TimeOfDay.EVENING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 5,
                reminderTimeMinutes = 21 * 60 + 45,
                simpleBenefit = "Wake up with instant clarity instead of morning anxiety.",
                stackedCueText = "Right before evening brush",
                subtasks = listOf(
                    Subtask(title = "Identify top 3 tasks for tomorrow", completed = false),
                    Subtask(title = "Close laptop and set morning alarms", completed = false)
                )
            ),

            // ── 2. Health & Body ─────────────────────────────────────────
            HabitTemplate(
                id = "health_morning_sunlight",
                title = "Morning Sunlight (10m)",
                description = "Step outside for 10 minutes of natural outdoor light.",
                category = HabitTemplateCategory.HEALTH_BODY,
                icon = "sun",
                colorTag = "#C58A24", // Warm Terracotta
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 10,
                reminderTimeMinutes = 8 * 60,
                simpleBenefit = "Calibrates circadian rhythm for all-day energy.",
                stackedCueText = "Within 30 minutes of waking up",
                subtasks = listOf(
                    Subtask(title = "Step out into natural light", completed = false),
                    Subtask(title = "Breathe deeply for 10 minutes", completed = false)
                )
            ),
            HabitTemplate(
                id = "health_daily_stretch",
                title = "Quick Mobility Stretch",
                description = "5 to 10 minutes of gentle spinal, hip, and neck movement.",
                category = HabitTemplateCategory.HEALTH_BODY,
                icon = "zen",
                colorTag = "#4E6542", // Forest Matcha
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.MEDIUM,
                durationMinutes = 10,
                reminderTimeMinutes = 8 * 60 + 15,
                simpleBenefit = "Prevents back stiffness and improves posture.",
                stackedCueText = "After morning water",
                subtasks = listOf(
                    Subtask(title = "Cat-cow spinal stretches", completed = false),
                    Subtask(title = "Hip flexor and hamstring release", completed = false)
                )
            ),
            HabitTemplate(
                id = "health_workout_session",
                title = "Daily Workout or Gym",
                description = "30 to 45 minutes of strength training, running, or movement.",
                category = HabitTemplateCategory.HEALTH_BODY,
                icon = "gym",
                colorTag = "#E65100", // Dynamic Flame
                timeOfDay = TimeOfDay.AFTERNOON,
                energyLevel = EnergyLevel.HIGH,
                durationMinutes = 40,
                reminderTimeMinutes = 17 * 60,
                simpleBenefit = "Builds physical longevity and mental resilience.",
                stackedCueText = "At the end of workday",
                subtasks = listOf(
                    Subtask(title = "Change into athletic gear", completed = false),
                    Subtask(title = "Complete 35m training session", completed = false),
                    Subtask(title = "Cool down & hydrate", completed = false)
                )
            ),
            HabitTemplate(
                id = "health_caffeine_cutoff",
                title = "No Caffeine After 2 PM",
                description = "Switch to water or herbal tea in the afternoon.",
                category = HabitTemplateCategory.HEALTH_BODY,
                icon = "spa",
                colorTag = "#5E548E", // Slate Violet
                timeOfDay = TimeOfDay.AFTERNOON,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 1,
                reminderTimeMinutes = 14 * 60,
                simpleBenefit = "Protects restorative deep sleep architecture.",
                stackedCueText = "When 2:00 PM arrives",
                subtasks = listOf(
                    Subtask(title = "Switch from coffee to water or herbal tea", completed = false)
                )
            ),

            // ── 3. Focus & Work ──────────────────────────────────────────
            HabitTemplate(
                id = "focus_deep_work_block",
                title = "90-Minute Deep Work Block",
                description = "Single-task focus on your most important project with phone silenced.",
                category = HabitTemplateCategory.FOCUS_WORK,
                icon = "target",
                colorTag = "#2B5329", // Deep Pine
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.HIGH,
                durationMinutes = 90,
                reminderTimeMinutes = 9 * 60 + 30,
                simpleBenefit = "Accomplishes more in 90m than 6 hours of distracted multitasking.",
                stackedCueText = "When sitting down at work desk",
                subtasks = listOf(
                    Subtask(title = "Place phone on silent out of sight", completed = false),
                    Subtask(title = "Close email and chat tabs", completed = false),
                    Subtask(title = "Complete 90m uninterrupted focus block", completed = false)
                )
            ),
            HabitTemplate(
                id = "focus_clean_desk",
                title = "Desk & Workspace Reset",
                description = "Clear coffee mugs, paper clutter, and desktop windows.",
                category = HabitTemplateCategory.FOCUS_WORK,
                icon = "work",
                colorTag = "#7D7463", // Sandstone
                timeOfDay = TimeOfDay.EVENING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 5,
                reminderTimeMinutes = 18 * 60,
                simpleBenefit = "A clean physical space creates instant mental calm.",
                stackedCueText = "At the end of your workday",
                subtasks = listOf(
                    Subtask(title = "Clear mugs and trash from desk", completed = false),
                    Subtask(title = "Close all browser tabs from today", completed = false)
                )
            ),

            // ── 4. Mind & Rest ───────────────────────────────────────────
            HabitTemplate(
                id = "mind_5m_breath",
                title = "5-Minute Quiet Meditation",
                description = "Sit quietly, close your eyes, and focus purely on your breath.",
                category = HabitTemplateCategory.MIND_REST,
                icon = "zen",
                colorTag = "#3D5A80", // Slate Indigo
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 5,
                reminderTimeMinutes = 8 * 60,
                simpleBenefit = "Lowers cortisol and trains intentional attention.",
                stackedCueText = "Before opening morning notifications",
                subtasks = listOf(
                    Subtask(title = "Sit in comfortable posture", completed = false),
                    Subtask(title = "5 minutes of steady box breathing", completed = false)
                )
            ),
            HabitTemplate(
                id = "mind_gratitude_journal",
                title = "Evening Gratitude (3 Items)",
                description = "Write down 3 small things that went well or you appreciated today.",
                category = HabitTemplateCategory.MIND_REST,
                icon = "journal",
                colorTag = "#C58A24", // Warm Terracotta
                timeOfDay = TimeOfDay.EVENING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 5,
                reminderTimeMinutes = 21 * 60 + 30,
                simpleBenefit = "Rewires attention toward abundance and contentment.",
                stackedCueText = "When winding down in bed",
                subtasks = listOf(
                    Subtask(title = "Jot down 3 specific things you appreciated today", completed = false)
                )
            ),
            HabitTemplate(
                id = "mind_screen_sunset",
                title = "Digital Sunset at 10 PM",
                description = "Put phones and screens away 1 hour before sleeping.",
                category = HabitTemplateCategory.MIND_REST,
                icon = "moon",
                colorTag = "#4A4E69", // Twilight Navy
                timeOfDay = TimeOfDay.EVENING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 1,
                reminderTimeMinutes = 22 * 60,
                simpleBenefit = "Allows natural melatonin to release for rapid sleep onset.",
                stackedCueText = "1 hour before sleep target",
                subtasks = listOf(
                    Subtask(title = "Place phone on charger across the room", completed = false),
                    Subtask(title = "Dim bedroom lights", completed = false)
                )
            )
        )
    }
}
