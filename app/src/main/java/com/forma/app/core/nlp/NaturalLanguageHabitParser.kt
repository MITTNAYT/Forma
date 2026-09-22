package com.forma.app.core.nlp

import androidx.compose.runtime.Immutable
import com.forma.app.domain.model.Habit
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
data class ParsedVoiceAction(
    val matchedHabits: List<Habit>,
    val gratitudeNote: String?,
    val detectedMinutes: Int?,
    val rawTranscript: String
)

@Singleton
class NaturalLanguageHabitParser @Inject constructor() {

    fun parseTranscript(transcript: String, activeHabits: List<Habit>): ParsedVoiceAction {
        val clean = transcript.trim().lowercase()
        if (clean.isBlank()) {
            return ParsedVoiceAction(emptyList(), null, null, transcript)
        }

        // 1. Match Habits by Name Keywords
        val matched = mutableListOf<Habit>()
        for (habit in activeHabits) {
            val habitNameClean = habit.name.lowercase()
            val habitKeywords = habitNameClean.split(" ", "-", "_").filter { it.length > 2 }

            val directMatch = clean.contains(habitNameClean)
            val keywordMatch = habitKeywords.any { clean.contains(it) }

            if (directMatch || keywordMatch) {
                matched.add(habit)
            }
        }

        // 2. Extract Gratitude / Reflection Note
        var gratitude: String? = null
        val gratitudeTriggers = listOf("grateful for", "thankful for", "grateful", "thankful", "feeling", "proud of")
        for (trigger in gratitudeTriggers) {
            val idx = clean.indexOf(trigger)
            if (idx != -1) {
                val rawNote = transcript.substring(idx + trigger.length).trim()
                if (rawNote.isNotBlank()) {
                    gratitude = rawNote.replaceFirstChar { it.uppercase() }
                    break
                }
            }
        }

        // 3. Extract Duration
        var minutes: Int? = null
        val durationRegex = Regex("""(\d+)\s*(mins|min|minutes|minute|hours|hour|hr)""")
        val match = durationRegex.find(clean)
        if (match != null) {
            val value = match.groupValues[1].toIntOrNull()
            val unit = match.groupValues[2]
            if (value != null) {
                minutes = if (unit.startsWith("h")) value * 60 else value
            }
        }

        return ParsedVoiceAction(
            matchedHabits = matched.distinctBy { it.id },
            gratitudeNote = gratitude,
            detectedMinutes = minutes,
            rawTranscript = transcript
        )
    }
}
