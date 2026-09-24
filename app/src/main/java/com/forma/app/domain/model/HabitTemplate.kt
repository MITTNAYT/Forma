package com.forma.app.domain.model

import androidx.compose.runtime.Immutable
import java.util.UUID

enum class HabitTemplateCategory(val displayName: String, val icon: String) {
    STUDENTS_SCHOLARS("Students & Study", "book"),
    WRITERS_THINKERS("Writers & Readers", "feather"),
    CREATORS_DESIGNERS("Creators & Studio", "sparkles"),
    DEVELOPERS_MAKERS("Coders & Builders", "zap"),
    MINDFUL_LIVING("Mindful Living", "spa"),
    EVENING_UNWIND("Evening Wind-down", "moon")
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
    val reminderTimeMinutes: Int? = null,
    val scienceNote: String,
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
            // ── Students & Study ─────────────────────────────────────────
            HabitTemplate(
                id = "student_active_recall",
                title = "Active Recall & Flashcards",
                description = "Self-test on today's most demanding concepts without referencing notes.",
                category = HabitTemplateCategory.STUDENTS_SCHOLARS,
                icon = "book",
                colorTag = "#4E6542", // Matcha Green
                timeOfDay = TimeOfDay.AFTERNOON,
                energyLevel = EnergyLevel.HIGH,
                durationMinutes = 25,
                reminderTimeMinutes = 16 * 60 + 30, // 4:30 PM
                scienceNote = "Testing effect: Retrieval practice produces ~50% stronger synaptic memory traces than passive re-reading.",
                stackedCueText = "After arriving back from afternoon classes",
                subtasks = listOf(
                    Subtask(title = "Open deck for today's hardest lecture", completed = false),
                    Subtask(title = "Attempt recall without peeking at answers", completed = false),
                    Subtask(title = "Tag 3 concepts for professor office hours", completed = false)
                )
            ),
            HabitTemplate(
                id = "student_pomodoro_study",
                title = "Deep Study Sprint (45m)",
                description = "Single-subject uninterrupted focus block in full airplane mode.",
                category = HabitTemplateCategory.STUDENTS_SCHOLARS,
                icon = "target",
                colorTag = "#8D5B4C", // Terracotta
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.HIGH,
                durationMinutes = 45,
                reminderTimeMinutes = 10 * 60, // 10:00 AM
                scienceNote = "Ultradian rhythm research proves cognitive focus peaks in 45-minute continuous blocks.",
                stackedCueText = "After sitting down at library desk",
                subtasks = listOf(
                    Subtask(title = "Phone placed in bag on silent", completed = false),
                    Subtask(title = "Define 1 specific assignment milestone", completed = false),
                    Subtask(title = "45m unbroken practice problem solving", completed = false)
                )
            ),
            HabitTemplate(
                id = "student_lecture_preview",
                title = "Pre-Lecture Primer (15m)",
                description = "Skim syllabus outlines and key headings before class begins.",
                category = HabitTemplateCategory.STUDENTS_SCHOLARS,
                icon = "feather",
                colorTag = "#D4AF37", // Warm Gold
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 15,
                reminderTimeMinutes = 8 * 60 + 30, // 8:30 AM
                scienceNote = "Cognitive schema priming activates neural frameworks to encode lecture concepts faster.",
                stackedCueText = "15 minutes before lecture hall doors open",
                subtasks = listOf(
                    Subtask(title = "Skim lecture slide headings", completed = false),
                    Subtask(title = "Jot down 2 questions to listen for", completed = false)
                )
            ),
            HabitTemplate(
                id = "student_spaced_review",
                title = "Spaced Repetition Review (20m)",
                description = "Clear your daily flashcard queue before evening wind-down.",
                category = HabitTemplateCategory.STUDENTS_SCHOLARS,
                icon = "sparkles",
                colorTag = "#5E548E", // Lavender
                timeOfDay = TimeOfDay.EVENING,
                energyLevel = EnergyLevel.MEDIUM,
                durationMinutes = 20,
                reminderTimeMinutes = 20 * 60, // 8:00 PM
                scienceNote = "Ebbinghaus curve: Reviewing before sleep consolidates memories into long-term neocortex.",
                stackedCueText = "After finishing dinner",
                subtasks = listOf(
                    Subtask(title = "Complete pending review queue", completed = false),
                    Subtask(title = "Review difficult deck cards once more", completed = false)
                )
            ),

            // ── Writers & Readers ────────────────────────────────────────
            HabitTemplate(
                id = "writer_morning_pages",
                title = "Morning Pages (Stream of Consciousness)",
                description = "Write longhand or blank-screen thoughts to empty mental clutter before the day begins.",
                category = HabitTemplateCategory.WRITERS_THINKERS,
                icon = "feather",
                colorTag = "#8D5B4C", // Terracotta
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 25,
                reminderTimeMinutes = 7 * 60 + 30, // 7:30 AM
                scienceNote = "Silences the internal critic by writing before the analytical left hemisphere fully asserts control.",
                stackedCueText = "With morning coffee or tea",
                subtasks = listOf(
                    Subtask(title = "Open blank page without distractions", completed = false),
                    Subtask(title = "Write 3 unedited pages of thought", completed = false),
                    Subtask(title = "Underline 1 seed idea for drafting", completed = false)
                )
            ),
            HabitTemplate(
                id = "writer_draft_sprint",
                title = "Deep Writing Sprint (500 Words)",
                description = "Uncompromising drafting sprint on your article, manuscript, or chapter.",
                category = HabitTemplateCategory.WRITERS_THINKERS,
                icon = "target",
                colorTag = "#2C221E", // Espresso
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.HIGH,
                durationMinutes = 60,
                reminderTimeMinutes = 9 * 60 + 30, // 9:30 AM
                scienceNote = "Separating generative drafting from critical editing doubles creative output and flow states.",
                stackedCueText = "After closing email and browser tabs",
                subtasks = listOf(
                    Subtask(title = "All research tabs closed", completed = false),
                    Subtask(title = "Draft 500 new words minimum", completed = false),
                    Subtask(title = "Save file without line-editing", completed = false)
                )
            ),
            HabitTemplate(
                id = "writer_paper_reading",
                title = "Paper Book Reading (20 Pages)",
                description = "Immerse in literary fiction, essays, or philosophy printed on physical paper.",
                category = HabitTemplateCategory.WRITERS_THINKERS,
                icon = "book",
                colorTag = "#D4AF37", // Warm Gold
                timeOfDay = TimeOfDay.EVENING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 30,
                reminderTimeMinutes = 21 * 60 + 30, // 9:30 PM
                scienceNote = "Reading physical paper reduces cortisol by 68% and improves sentence cadence internalization.",
                stackedCueText = "After entering bedroom reading chair",
                subtasks = listOf(
                    Subtask(title = "Dim overhead lights to warm amber", completed = false),
                    Subtask(title = "Read 20 uninterrupted physical pages", completed = false),
                    Subtask(title = "Annotate one resonant sentence", completed = false)
                )
            ),
            HabitTemplate(
                id = "writer_idea_capture",
                title = "Commonplace Book & Note Filing",
                description = "Synthesize daily observations, overheard dialogue, and quotes into your archive.",
                category = HabitTemplateCategory.WRITERS_THINKERS,
                icon = "sparkles",
                colorTag = "#4E6542", // Matcha Green
                timeOfDay = TimeOfDay.AFTERNOON,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 15,
                reminderTimeMinutes = 17 * 60, // 5:00 PM
                scienceNote = "Luhmann's slip-box principle: Genuine original thinking emerges from cross-pollinating captured notes.",
                stackedCueText = "At the close of the afternoon workday",
                subtasks = listOf(
                    Subtask(title = "Review quick memos and bookmarks", completed = false),
                    Subtask(title = "File key ideas into permanent archive", completed = false)
                )
            ),

            // ── Creators & Studio ────────────────────────────────────────
            HabitTemplate(
                id = "creator_visual_audit",
                title = "Visual Inspiration & Taste Audit",
                description = "Curate reference design, typography, color palettes, and lighting choices.",
                category = HabitTemplateCategory.CREATORS_DESIGNERS,
                icon = "sparkles",
                colorTag = "#5E548E", // Lavender
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 15,
                reminderTimeMinutes = 10 * 60, // 10:00 AM
                scienceNote = "Input calibrates output: Deliberate exposure to world-class craft directly elevates design sensibility.",
                stackedCueText = "Before opening studio canvas",
                subtasks = listOf(
                    Subtask(title = "Collect 3 exemplary design references", completed = false),
                    Subtask(title = "Analyze typography and layout decisions", completed = false)
                )
            ),
            HabitTemplate(
                id = "creator_studio_block",
                title = "Studio Craft Block (90m)",
                description = "Deep creative flow: illustration, Figma design system, photography, or video editing.",
                category = HabitTemplateCategory.CREATORS_DESIGNERS,
                icon = "target",
                colorTag = "#8D5B4C", // Terracotta
                timeOfDay = TimeOfDay.AFTERNOON,
                energyLevel = EnergyLevel.HIGH,
                durationMinutes = 90,
                reminderTimeMinutes = 14 * 60, // 2:00 PM
                scienceNote = "Reaching flow state in visual arts requires at least 45 minutes of unbroken immersion.",
                stackedCueText = "After afternoon matcha or espresso",
                subtasks = listOf(
                    Subtask(title = "Prepare canvas & moodboard palette", completed = false),
                    Subtask(title = "Sprint on core composition / layout", completed = false),
                    Subtask(title = "Export work-in-progress snapshot", completed = false)
                )
            ),
            HabitTemplate(
                id = "creator_publish_share",
                title = "Ship & Share Process (Build in Public)",
                description = "Document one technique, layout iteration, or design decision publicly.",
                category = HabitTemplateCategory.CREATORS_DESIGNERS,
                icon = "feather",
                colorTag = "#D4AF37", // Warm Gold
                timeOfDay = TimeOfDay.AFTERNOON,
                energyLevel = EnergyLevel.MEDIUM,
                durationMinutes = 20,
                reminderTimeMinutes = 17 * 60 + 30, // 5:30 PM
                scienceNote = "Sharing creative processes compounds authority and builds genuine audience resonance.",
                stackedCueText = "After concluding studio sprint",
                subtasks = listOf(
                    Subtask(title = "Select 1 clean work-in-progress visual", completed = false),
                    Subtask(title = "Write 2 sentences explaining the design rationale", completed = false)
                )
            ),
            HabitTemplate(
                id = "creator_workspace_reset",
                title = "Studio Reset & File Hygiene",
                description = "Organize project artboards, name layers, and clear physical desk.",
                category = HabitTemplateCategory.CREATORS_DESIGNERS,
                icon = "spa",
                colorTag = "#4E6542", // Matcha Green
                timeOfDay = TimeOfDay.EVENING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 10,
                reminderTimeMinutes = 18 * 60 + 30, // 6:30 PM
                scienceNote = "Clear, clutter-free physical and digital workspaces drastically reduce morning creative friction.",
                stackedCueText = "Before stepping away from studio desk",
                subtasks = listOf(
                    Subtask(title = "Name and group all open layers", completed = false),
                    Subtask(title = "Backup project to cloud vault", completed = false),
                    Subtask(title = "Wipe desk surface clean", completed = false)
                )
            ),

            // ── Coders & Builders ────────────────────────────────────────
            HabitTemplate(
                id = "dev_clean_code_flow",
                title = "Deep Coding Block (90m)",
                description = "Write core feature logic and tests without Slack, emails, or meetings.",
                category = HabitTemplateCategory.DEVELOPERS_MAKERS,
                icon = "zap",
                colorTag = "#2C221E", // Espresso
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.HIGH,
                durationMinutes = 90,
                reminderTimeMinutes = 10 * 60 + 30, // 10:30 AM
                scienceNote = "Research shows software engineers lose ~23 minutes of mental stack context per interruption.",
                stackedCueText = "After morning standup and backlog check",
                subtasks = listOf(
                    Subtask(title = "Pull latest main & branch out", completed = false),
                    Subtask(title = "Write unit test / spec outline", completed = false),
                    Subtask(title = "Implement clean, modular solution", completed = false),
                    Subtask(title = "Run test suite to verify green", completed = false)
                )
            ),
            HabitTemplate(
                id = "dev_pr_review",
                title = "Thoughtful PR Review & Mentorship",
                description = "Review teammate code for architecture, test coverage, and edge cases.",
                category = HabitTemplateCategory.DEVELOPERS_MAKERS,
                icon = "target",
                colorTag = "#4E6542", // Matcha Green
                timeOfDay = TimeOfDay.AFTERNOON,
                energyLevel = EnergyLevel.MEDIUM,
                durationMinutes = 30,
                reminderTimeMinutes = 15 * 60, // 3:00 PM
                scienceNote = "Thorough peer reviews catch 60% of architectural regressions before production staging.",
                stackedCueText = "Mid-afternoon code review window",
                subtasks = listOf(
                    Subtask(title = "Read PR description & understand requirement", completed = false),
                    Subtask(title = "Inspect diff for performance & edge cases", completed = false),
                    Subtask(title = "Leave constructive, kind feedback", completed = false)
                )
            ),
            HabitTemplate(
                id = "dev_tech_reading",
                title = "Daily Architecture / Tech Reading",
                description = "Read official SDK release notes, RFCs, or engineering deep-dives.",
                category = HabitTemplateCategory.DEVELOPERS_MAKERS,
                icon = "book",
                colorTag = "#D4AF37", // Warm Gold
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 15,
                reminderTimeMinutes = 8 * 60 + 45, // 8:45 AM
                scienceNote = "Daily 15-minute technical reading keeps skills ahead of rapid framework evolution.",
                stackedCueText = "Before writing the first line of code",
                subtasks = listOf(
                    Subtask(title = "Read 1 engineering post or release note", completed = false),
                    Subtask(title = "Note 1 design pattern to adopt", completed = false)
                )
            ),
            HabitTemplate(
                id = "dev_git_hygiene",
                title = "Git Hygiene & Staging Clean Up",
                description = "Commit clean staged diffs, push branch, and leave tomorrow's TODO anchor.",
                category = HabitTemplateCategory.DEVELOPERS_MAKERS,
                icon = "sparkles",
                colorTag = "#5E548E", // Lavender
                timeOfDay = TimeOfDay.EVENING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 10,
                reminderTimeMinutes = 17 * 60 + 45, // 5:45 PM
                scienceNote = "Leaving an explicit TODO anchor comment reduces cognitive restart friction tomorrow morning.",
                stackedCueText = "Before closing terminal and IDE",
                subtasks = listOf(
                    Subtask(title = "Commit cleanly formatted changes", completed = false),
                    Subtask(title = "Push branch to remote", completed = false),
                    Subtask(title = "Write 1 TODO comment where to resume", completed = false)
                )
            ),

            // ── Mindful Living ───────────────────────────────────────────
            HabitTemplate(
                id = "mindful_sunlight_water",
                title = "Morning Sunlight & Hydration",
                description = "Drink 500ml water and view natural outdoor daylight for 10-15 minutes.",
                category = HabitTemplateCategory.MINDFUL_LIVING,
                icon = "spa",
                colorTag = "#D4AF37", // Warm Gold
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 15,
                reminderTimeMinutes = 7 * 60, // 7:00 AM
                scienceNote = "Triggers natural morning cortisol pulse and sets master circadian clock for deep night sleep.",
                stackedCueText = "Immediately after stepping out of bed",
                subtasks = listOf(
                    Subtask(title = "Drink 500ml room temp water with sea salt", completed = false),
                    Subtask(title = "10 minutes natural daylight outside", completed = false)
                )
            ),
            HabitTemplate(
                id = "mindful_box_breathing",
                title = "Midday Box Breathing (4-4-4-4)",
                description = "Inhale 4s, Hold 4s, Exhale 4s, Hold 4s for 5 continuous calm cycles.",
                category = HabitTemplateCategory.MINDFUL_LIVING,
                icon = "sparkles",
                colorTag = "#4E6542", // Matcha Green
                timeOfDay = TimeOfDay.AFTERNOON,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 5,
                reminderTimeMinutes = 13 * 60 + 30, // 1:30 PM
                scienceNote = "Downregulates autonomic nervous system, balancing heart rate variability.",
                stackedCueText = "When noticing midday tension or after lunch",
                subtasks = listOf(
                    Subtask(title = "Sit tall with unclenched jaw", completed = false),
                    Subtask(title = "Complete 5 rounds of 4-4-4-4 breathing", completed = false)
                )
            ),
            HabitTemplate(
                id = "mindful_nature_walk",
                title = "Post-Lunch Nature Walk",
                description = "15-minute unhurried walk in green space or fresh air without headphones.",
                category = HabitTemplateCategory.MINDFUL_LIVING,
                icon = "heart",
                colorTag = "#8D5B4C", // Terracotta
                timeOfDay = TimeOfDay.AFTERNOON,
                energyLevel = EnergyLevel.MEDIUM,
                durationMinutes = 15,
                reminderTimeMinutes = 14 * 60 + 30, // 2:30 PM
                scienceNote = "Blunts postprandial glucose spikes by up to 30% and stimulates divergent creative problem-solving.",
                stackedCueText = "After clearing lunch dishes",
                subtasks = listOf(
                    Subtask(title = "Phone left in pocket on silent", completed = false),
                    Subtask(title = "Notice 3 natural details in surroundings", completed = false)
                )
            ),

            // ── Evening Wind-down ────────────────────────────────────────
            HabitTemplate(
                id = "evening_digital_sunset",
                title = "Digital Sunset & Warm Lighting",
                description = "Switch off work screens 60m before sleep; switch room to warm amber lighting.",
                category = HabitTemplateCategory.EVENING_UNWIND,
                icon = "moon",
                colorTag = "#5E548E", // Lavender
                timeOfDay = TimeOfDay.EVENING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 45,
                reminderTimeMinutes = 21 * 60 + 30, // 9:30 PM
                scienceNote = "Preserves natural melatonin secretion and prevents REM sleep fragmentation.",
                stackedCueText = "When 9:30 PM evening chime sounds",
                subtasks = listOf(
                    Subtask(title = "Shut work laptop and screens", completed = false),
                    Subtask(title = "Switch ambient lighting to warm amber", completed = false),
                    Subtask(title = "Set phone on bedside charger", completed = false)
                )
            ),
            HabitTemplate(
                id = "evening_gratitude_journal",
                title = "Three Graces Gratitude Reflection",
                description = "Write down 3 specific, sensory moments that brought genuine gratitude today.",
                category = HabitTemplateCategory.EVENING_UNWIND,
                icon = "feather",
                colorTag = "#D4AF37", // Warm Gold
                timeOfDay = TimeOfDay.EVENING,
                energyLevel = EnergyLevel.LOW,
                durationMinutes = 10,
                reminderTimeMinutes = 22 * 60, // 10:00 PM
                scienceNote = "Cognitive reframing before sleep reduces pre-sleep autonomic arousal and accelerates sleep onset.",
                stackedCueText = "After getting into bed",
                subtasks = listOf(
                    Subtask(title = "Note 3 sensory moments of gratitude", completed = false),
                    Subtask(title = "Release any unfinished tasks for tomorrow", completed = false)
                )
            )
        )
    }
}
