package com.forma.app.domain.model

import androidx.compose.runtime.Immutable
import java.util.UUID

enum class HabitTemplateCategory(val displayName: String, val icon: String) {
    MORNING_AWAKENING("Morning Awakening", "sun"),
    DEEP_FOCUS("Deep Focus & Flow", "zap"),
    MINDFULNESS("Stillness & Breath", "feather"),
    PHYSICAL_VITALITY("Physical Vitality", "heart"),
    EVENING_RESTORATION("Evening Restoration", "moon"),
    DIGITAL_WELLNESS("Digital Wellness", "sparkles")
}

@Immutable
data class HabitTemplate(
    val id: String,
    val title: String,
    val description: String,
    val category: HabitTemplateCategory,
    val icon: String = "target",
    val colorTag: String = "#4E6542",
    val timeOfDay: TimeOfDay = TimeOfDay.ANYTIME,
    val energyLevel: EnergyLevel = EnergyLevel.MEDIUM,
    val durationMinutes: Int = 15,
    val scienceNote: String,
    val stackedCueText: String? = null
) {
    fun toHabit(): Habit = Habit(
        id = UUID.randomUUID().toString(),
        name = title,
        icon = icon,
        colorTag = colorTag,
        timeOfDay = timeOfDay,
        energyLevel = energyLevel,
        repeatDays = setOf(1, 2, 3, 4, 5, 6, 7),
        reminderTimeMinutes = when (timeOfDay) {
            TimeOfDay.MORNING -> 8 * 60 // 8:00 AM
            TimeOfDay.AFTERNOON -> 14 * 60 // 2:00 PM
            TimeOfDay.EVENING -> 21 * 60 // 9:00 PM
            TimeOfDay.ANYTIME -> null
        },
        stackedCueText = stackedCueText,
        isIndefinite = true
    )

    companion object {
        val CURATED_TEMPLATES: List<HabitTemplate> = listOf(
            // ── Morning Awakening ───────────────────────────────────────
            HabitTemplate(
                id = "morning_sunlight",
                title = "Morning Sunlight & Hydration",
                description = "Drink 500ml water with sea salt and view natural daylight for 10-15 minutes.",
                category = HabitTemplateCategory.MORNING_AWAKENING,
                icon = "sun",
                colorTag = "#D4AF37", // Warm Gold
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 15,
                scienceNote = "Huberman Lab protocol: Triggers morning cortisol pulse and sets circadian rhythm for deep sleep 16 hours later.",
                stackedCueText = "After I get out of bed"
            ),
            HabitTemplate(
                id = "cold_splash_wim_hof",
                title = "Cold Water Reset",
                description = "Cold shower or ice water facial plunge to invigorate the nervous system.",
                category = HabitTemplateCategory.MORNING_AWAKENING,
                icon = "water",
                colorTag = "#4A7C9B", // Cool Blue
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.HIGH,
                durationMinutes = 5,
                scienceNote = "Increases norepinephrine by 250% for sustained focus and immune resilience.",
                stackedCueText = "After morning hydration"
            ),
            HabitTemplate(
                id = "morning_tea_stillness",
                title = "Mindful Matcha / Tea Ceremony",
                description = "Brew and sip your tea in deliberate silence, without checking notifications.",
                category = HabitTemplateCategory.MORNING_AWAKENING,
                icon = "coffee",
                colorTag = "#4E6542", // Matcha Green
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 10,
                scienceNote = "L-theanine combined with gentle caffeine stimulates calm alpha brainwaves.",
                stackedCueText = "After pouring morning brew"
            ),

            // ── Deep Focus & Flow ───────────────────────────────────────
            HabitTemplate(
                id = "deep_work_90",
                title = "90-Minute Ultradian Deep Work",
                description = "Unbroken single-task focus block on your highest-leverage cognitive priority.",
                category = HabitTemplateCategory.DEEP_FOCUS,
                icon = "zap",
                colorTag = "#8D5B4C", // Terracotta
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.HIGH,
                durationMinutes = 90,
                scienceNote = "Aligns with human 90-minute basic rest-activity cycles for maximum neuroplasticity.",
                stackedCueText = "After opening my workspace"
            ),
            HabitTemplate(
                id = "pomodoro_focus_sprint",
                title = "25-Minute Flow Sprint",
                description = "One single-task sprint in full airplane mode with zero interruptions.",
                category = HabitTemplateCategory.DEEP_FOCUS,
                icon = "target",
                colorTag = "#2C221E", // Espresso
                timeOfDay = TimeOfDay.AFTERNOON,
                energyLevel = EnergyLevel.MEDIUM,
                durationMinutes = 25,
                scienceNote = "Reduces task friction and cognitive overhead through time-boxing.",
                stackedCueText = "After setting my timer"
            ),
            HabitTemplate(
                id = "daily_priorities_3",
                title = "Rule of 3 Intentions",
                description = "Define exactly 3 non-negotiable milestones before touching communication apps.",
                category = HabitTemplateCategory.DEEP_FOCUS,
                icon = "feather",
                colorTag = "#4E6542",
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 5,
                scienceNote = "Prevents attention fragmentation and decision fatigue early in the day.",
                stackedCueText = "Before opening inbox"
            ),

            // ── Stillness & Breath ──────────────────────────────────────
            HabitTemplate(
                id = "box_breathing_4x4",
                title = "Box Breathing (4-4-4-4)",
                description = "Inhale 4s, Hold 4s, Exhale 4s, Hold 4s for 5 continuous calm cycles.",
                category = HabitTemplateCategory.MINDFULNESS,
                icon = "sparkles",
                colorTag = "#5E548E", // Lavender
                timeOfDay = TimeOfDay.AFTERNOON,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 5,
                scienceNote = "Downregulates autonomic nervous system, balancing heart rate variability.",
                stackedCueText = "When feeling midday tension"
            ),
            HabitTemplate(
                id = "gratitude_journal_3",
                title = "Three Graces Reflection",
                description = "Write down 3 specific, sensory moments that brought genuine gratitude today.",
                category = HabitTemplateCategory.MINDFULNESS,
                icon = "book",
                colorTag = "#D4AF37",
                timeOfDay = TimeOfDay.EVENING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 5,
                scienceNote = "Proven in positive psychology to reprogram negative cognitive bias.",
                stackedCueText = "After sitting in my reading chair"
            ),
            HabitTemplate(
                id = "body_scan_meditation",
                title = "10-Minute Zen Body Scan",
                description = "Progressively release tension from the crown of the head to the soles of the feet.",
                category = HabitTemplateCategory.MINDFULNESS,
                icon = "spa",
                colorTag = "#4E6542",
                timeOfDay = TimeOfDay.EVENING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 10,
                scienceNote = "Enhances interoceptive awareness and activates the parasympathetic tone.",
                stackedCueText = "Before evening rest"
            ),

            // ── Physical Vitality ───────────────────────────────────────
            HabitTemplate(
                id = "morning_mobility_stretch",
                title = "Gentle Spinal Mobility",
                description = "Cat-cow, hip openers, and thoracic spine rotations to lubricate joints.",
                category = HabitTemplateCategory.PHYSICAL_VITALITY,
                icon = "heart",
                colorTag = "#8D5B4C",
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.MEDIUM,
                durationMinutes = 10,
                scienceNote = "Restores synovial fluid flow and improves posture after hours of sleep.",
                stackedCueText = "After stepping out of bed"
            ),
            HabitTemplate(
                id = "brisk_nature_walk",
                title = "Post-Meal Nature Walk",
                description = "15-minute unhurried walk in green space or fresh air without podcasts.",
                category = HabitTemplateCategory.PHYSICAL_VITALITY,
                icon = "leaf",
                colorTag = "#4E6542",
                timeOfDay = TimeOfDay.AFTERNOON,
                energyLevel = EnergyLevel.MEDIUM,
                durationMinutes = 15,
                scienceNote = "Blunts postprandial glucose spikes by up to 30% and stimulates divergent thinking.",
                stackedCueText = "After finishing lunch"
            ),
            HabitTemplate(
                id = "hydration_electrolytes",
                title = "Electrolyte Recharge (2L)",
                description = "Maintain steady hydration with trace minerals throughout the workday.",
                category = HabitTemplateCategory.PHYSICAL_VITALITY,
                icon = "water",
                colorTag = "#4A7C9B",
                timeOfDay = TimeOfDay.ANYTIME,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 1,
                scienceNote = "A 2% drop in body water causes a measurable 10% drop in working memory.",
                stackedCueText = "Every time I fill my bottle"
            ),

            // ── Evening Restoration ─────────────────────────────────────
            HabitTemplate(
                id = "digital_sunset",
                title = "Digital Sunset & Blue Light Off",
                description = "Switch off screens 60 minutes before sleep; switch ambient lighting to warm amber.",
                category = HabitTemplateCategory.EVENING_RESTORATION,
                icon = "moon",
                colorTag = "#5E548E", // Lavender
                timeOfDay = TimeOfDay.EVENING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 60,
                scienceNote = "Preserves natural melatonin secretion and prevents REM sleep disruption.",
                stackedCueText = "When 9:00 PM alarm rings"
            ),
            HabitTemplate(
                id = "reading_fiction_paper",
                title = "Paper Book Reading (20 Pages)",
                description = "Immerse in literary fiction or philosophy printed on paper before sleep.",
                category = HabitTemplateCategory.EVENING_RESTORATION,
                icon = "book",
                colorTag = "#D4AF37",
                timeOfDay = TimeOfDay.EVENING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 20,
                scienceNote = "Reduces cortisol by 68%, outperforming tea, music, or walking for pre-sleep calming.",
                stackedCueText = "After getting into bed"
            ),
            HabitTemplate(
                id = "tomorrow_clarity_prep",
                title = "Tomorrow's Sanctuary Preview",
                description = "Review your calendar and prepare clothes and workspace for the morning.",
                category = HabitTemplateCategory.EVENING_RESTORATION,
                icon = "feather",
                colorTag = "#2C221E",
                timeOfDay = TimeOfDay.EVENING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 10,
                scienceNote = "Zeigarnik effect mitigation: frees cognitive loops so the brain can relax into sleep.",
                stackedCueText = "After dinner is cleared"
            ),

            // ── Digital Wellness ────────────────────────────────────────
            HabitTemplate(
                id = "no_phone_first_hour",
                title = "Phone-Free First Hour",
                description = "Keep phone in another room or airplane mode for the first 60 minutes of waking.",
                category = HabitTemplateCategory.DIGITAL_WELLNESS,
                icon = "sparkles",
                colorTag = "#8D5B4C",
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 60,
                scienceNote = "Guards against early dopamine spiking and reactive stress conditioning.",
                stackedCueText = "Upon opening eyes"
            ),
            HabitTemplate(
                id = "mindful_inbox_zero",
                title = "Batch Communication Windows",
                description = "Check email and messaging only at 11:00 AM and 4:30 PM instead of continuous checks.",
                category = HabitTemplateCategory.DIGITAL_WELLNESS,
                icon = "target",
                colorTag = "#4E6542",
                timeOfDay = TimeOfDay.AFTERNOON,
                energyLevel = EnergyLevel.MEDIUM,
                durationMinutes = 20,
                scienceNote = "Eliminates attention switching residue, saving ~2.1 hours of lost focus daily.",
                stackedCueText = "At designated batch intervals"
            )
        )
    }
}
