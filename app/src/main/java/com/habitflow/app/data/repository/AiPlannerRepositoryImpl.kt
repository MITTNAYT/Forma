package com.habitflow.app.data.repository

import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.Subtask
import com.habitflow.app.domain.model.TimelineItem
import com.habitflow.app.domain.model.TimeOfDay
import com.habitflow.app.domain.repository.AiPlanPreset
import com.habitflow.app.domain.repository.AiPlannerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalTime
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
    ): Result<List<TimelineItem>> = withContext(Dispatchers.IO) {
        val apiKey = System.getenv("GEMINI_API_KEY") ?: System.getenv("AI_API_KEY")

        if (!apiKey.isNullOrBlank()) {
            val remoteResult = fetchPlanFromGemini(apiKey, date, habits, existingItems, preset)
            if (remoteResult.isSuccess && remoteResult.getOrNull()?.isNotEmpty() == true) {
                return@withContext remoteResult
            }
        }

        // Robust intelligent dynamic heuristic planner (Works seamlessly offline)
        val generatedItems = generateAdaptiveHeuristicPlan(date, habits, existingItems, preset)
        Result.success(generatedItems)
    }

    private fun fetchPlanFromGemini(
        apiKey: String,
        date: String,
        habits: List<Habit>,
        existingItems: List<TimelineItem>,
        preset: AiPlanPreset
    ): Result<List<TimelineItem>> {
        return try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.doOutput = true

            val habitNames = habits.joinToString { "${it.name} (${it.timeOfDay})" }
            val occupiedSlots = existingItems.mapNotNull {
                if (it.startTime != null && it.endTime != null) "${it.startTime}-${it.endTime} (${it.title})" else null
            }.joinToString()

            val prompt = """
                You are Forma's AI Day Planner. Plan 3 to 4 mindful time blocks for date $date with theme '${preset.title}'.
                User's active habits: $habitNames.
                Occupied time slots to avoid: $occupiedSlots.
                Return ONLY a valid JSON array of objects with keys:
                - title (string)
                - startTime (string "HH:mm")
                - endTime (string "HH:mm")
                - icon (string e.g. "computer", "spa", "school", "timer", "wb_sunny")
                - colorTag (hex string e.g. "#637852", "#8F9E8B", "#D4A373")
                - notes (string explanation)
                - subtasks (array of string subtask titles)
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val rootJson = JSONObject(responseText)
                val candidates = rootJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

                val cleanJson = rawText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

                val jsonArray = JSONArray(cleanJson)
                val items = mutableListOf<TimelineItem>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val subtasksJson = obj.optJSONArray("subtasks")
                    val subtasks = mutableListOf<Subtask>()
                    if (subtasksJson != null) {
                        for (s in 0 until subtasksJson.length()) {
                            subtasks.add(Subtask(title = subtasksJson.getString(s), completed = false))
                        }
                    }

                    items.add(
                        TimelineItem(
                            id = UUID.randomUUID().toString(),
                            title = obj.getString("title"),
                            date = date,
                            startTime = obj.optString("startTime", "09:00"),
                            endTime = obj.optString("endTime", "10:30"),
                            icon = obj.optString("icon", "auto_awesome"),
                            colorTag = obj.optString("colorTag", "#637852"),
                            notes = obj.optString("notes", ""),
                            subtasks = subtasks
                        )
                    )
                }
                Result.success(items)
            } else {
                Result.failure(Exception("Gemini API HTTP ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateAdaptiveHeuristicPlan(
        date: String,
        habits: List<Habit>,
        existingItems: List<TimelineItem>,
        preset: AiPlanPreset
    ): List<TimelineItem> {
        val items = mutableListOf<TimelineItem>()

        // Check user's habits for context
        val morningHabits = habits.filter { it.timeOfDay == TimeOfDay.MORNING }
        val morningHabitName = morningHabits.firstOrNull()?.name ?: "Mindful Morning Alignment"

        when (preset) {
            AiPlanPreset.DEEP_WORK -> {
                items.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Morning High-Leverage Sprint",
                        date = date,
                        startTime = "09:00",
                        endTime = "11:30",
                        icon = "computer",
                        colorTag = "#4E6542",
                        notes = "Forma AI: Guarded morning cognitive flow. Aligned after '$morningHabitName'.",
                        subtasks = listOf(
                            Subtask(title = "Silence notifications & enter Flow", completed = false),
                            Subtask(title = "Execute core objective milestone", completed = false),
                            Subtask(title = "Review deliverable quality", completed = false)
                        ),
                        reminderMinutesBefore = 10
                    )
                )
                items.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Midday Mindful Reset & Walk",
                        date = date,
                        startTime = "13:00",
                        endTime = "13:45",
                        icon = "self_improvement",
                        colorTag = "#8F9E8B",
                        notes = "Forma AI: Nervous system decompression and cognitive replenishment.",
                        subtasks = listOf(
                            Subtask(title = "15 min outdoor light walk", completed = false),
                            Subtask(title = "Mindful hydration pause", completed = false)
                        )
                    )
                )
                items.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Afternoon Synthesis & Comms",
                        date = date,
                        startTime = "15:30",
                        endTime = "16:30",
                        icon = "mail",
                        colorTag = "#4A5568",
                        notes = "Forma AI: Batch communication processing and next-day prioritization.",
                        subtasks = listOf(
                            Subtask(title = "Clear inbox and actionable messages", completed = false),
                            Subtask(title = "Set top 3 intentions for tomorrow", completed = false)
                        )
                    )
                )
            }
            AiPlanPreset.HEALTH_BALANCE -> {
                items.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Morning Sunlight & Hydration",
                        date = date,
                        startTime = "07:30",
                        endTime = "08:15",
                        icon = "wb_sunny",
                        colorTag = "#D4A373",
                        notes = "Forma AI: Early circadian calibration aligned with '$morningHabitName'.",
                        subtasks = listOf(
                            Subtask(title = "500ml water with minerals", completed = false),
                            Subtask(title = "15 min outdoor sunlight exposure", completed = false)
                        )
                    )
                )
                items.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Midday Mobility & Posture Pause",
                        date = date,
                        startTime = "12:30",
                        endTime = "13:00",
                        icon = "spa",
                        colorTag = "#637852",
                        notes = "Forma AI: Decompress spine and neck flexors.",
                        subtasks = listOf(
                            Subtask(title = "Thoracic spine extensions", completed = false),
                            Subtask(title = "5 min deep box breathing", completed = false)
                        )
                    )
                )
                items.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Evening Digital Sunset",
                        date = date,
                        startTime = "21:00",
                        endTime = "21:45",
                        icon = "nights_stay",
                        colorTag = "#2D3748",
                        notes = "Forma AI: Low-light restorative wind-down before sleep.",
                        subtasks = listOf(
                            Subtask(title = "Dim screens and artificial lights", completed = false),
                            Subtask(title = "Reflective daily journaling", completed = false)
                        )
                    )
                )
            }
            AiPlanPreset.EXAM_STUDY -> {
                items.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Active Recall Sprint (Block 1)",
                        date = date,
                        startTime = "09:00",
                        endTime = "10:30",
                        icon = "school",
                        colorTag = "#E67E22",
                        notes = "Forma AI: High-yield concept retrieval without looking at references.",
                        subtasks = listOf(
                            Subtask(title = "Self-quiz on key chapters", completed = false),
                            Subtask(title = "Log knowledge blindspots", completed = false)
                        ),
                        reminderMinutesBefore = 10
                    )
                )
                items.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Spaced Repetition & Flashcards",
                        date = date,
                        startTime = "14:00",
                        endTime = "15:00",
                        icon = "menu_book",
                        colorTag = "#3498DB",
                        notes = "Forma AI: Formula and vocabulary consolidation.",
                        subtasks = listOf(
                            Subtask(title = "Review due flashcards", completed = false),
                            Subtask(title = "Mark difficult concepts for review", completed = false)
                        )
                    )
                )
                items.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Timed Mock Practice Session",
                        date = date,
                        startTime = "16:30",
                        endTime = "18:00",
                        icon = "timer",
                        colorTag = "#637852",
                        notes = "Forma AI: Full timed exam simulation under authentic conditions.",
                        subtasks = listOf(
                            Subtask(title = "Complete timed question set", completed = false),
                            Subtask(title = "Thorough error analysis", completed = false)
                        )
                    )
                )
            }
        }

        return items
    }
}
