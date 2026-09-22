package com.habitflow.app.data.repository

import com.habitflow.app.BuildConfig
import com.habitflow.app.domain.model.AiGenerationResult
import com.habitflow.app.domain.model.EnergyLevel
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.Subtask
import com.habitflow.app.domain.model.TimeOfDay
import com.habitflow.app.domain.model.TimelineItem
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
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiPlannerRepositoryImpl @Inject constructor() : AiPlannerRepository {

    private fun getEffectiveApiKey(): String {
        return BuildConfig.GEMINI_API_KEY.ifBlank {
            System.getenv("GEMINI_API_KEY") ?: System.getenv("OPENROUTER_API_KEY") ?: ""
        }
    }

    override suspend fun generateComprehensivePlan(
        prompt: String,
        date: String,
        habits: List<Habit>,
        existingItems: List<TimelineItem>,
        preset: AiPlanPreset?
    ): Result<AiGenerationResult> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()

        if (apiKey.isNotBlank()) {
            try {
                val remoteResult = if (apiKey.startsWith("sk-or-")) {
                    fetchComprehensivePlanFromOpenRouter(apiKey, prompt, date, habits, existingItems, preset)
                } else {
                    fetchComprehensivePlanFromGoogleGemini(apiKey, prompt, date, habits, existingItems, preset)
                }
                if (remoteResult.isSuccess) {
                    return@withContext remoteResult
                }
            } catch (_: Exception) {
                // Network or rate-limit issue: gracefully fall back to local heuristic intelligence
            }
        }

        // Offline / fallback dynamic engine
        val heuristicResult = generateAdaptiveHeuristicResult(prompt, date, habits, existingItems, preset)
        Result.success(heuristicResult)
    }

    override suspend fun decomposeGoal(
        goal: String,
        date: String,
        currentHabits: List<Habit>
    ): Result<AiGenerationResult> = withContext(Dispatchers.IO) {
        val prompt = "Decompose this major ambition into actionable daily timeline tasks and supporting core habits: '$goal'"
        generateComprehensivePlan(prompt, date, currentHabits, emptyList(), AiPlanPreset.PRODUCTIVITY_SPRINT)
    }

    override suspend fun suggestHabitPacks(
        intent: String,
        currentHabits: List<Habit>
    ): Result<List<Habit>> = withContext(Dispatchers.IO) {
        val prompt = "Recommend 4 transformative daily habits and rituals for the theme: '$intent'"
        val result = generateComprehensivePlan(prompt, "today", currentHabits, emptyList(), null)
        if (result.isSuccess) {
            Result.success(result.getOrNull()?.suggestedHabits ?: emptyList())
        } else {
            Result.success(getFallbackHabitPack(intent))
        }
    }

    override suspend fun suggestDayPlan(
        date: String,
        habits: List<Habit>,
        existingItems: List<TimelineItem>,
        preset: AiPlanPreset
    ): Result<List<TimelineItem>> = withContext(Dispatchers.IO) {
        val result = generateComprehensivePlan("", date, habits, existingItems, preset)
        if (result.isSuccess) {
            Result.success(result.getOrNull()?.tasks ?: emptyList())
        } else {
            Result.success(generateAdaptiveHeuristicResult("", date, habits, existingItems, preset).tasks)
        }
    }

    private fun fetchComprehensivePlanFromOpenRouter(
        apiKey: String,
        userPrompt: String,
        date: String,
        habits: List<Habit>,
        existingItems: List<TimelineItem>,
        preset: AiPlanPreset?
    ): Result<AiGenerationResult> {
        val endpoint = "https://openrouter.ai/api/v1/chat/completions"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $apiKey")
        conn.setRequestProperty("HTTP-Referer", "https://forma.app")
        conn.setRequestProperty("X-Title", "Forma HabitFlow")
        conn.doOutput = true
        conn.connectTimeout = 12000
        conn.readTimeout = 12000

        val habitNames = habits.joinToString { "${it.name} (${it.timeOfDay})" }
        val occupiedSlots = existingItems.mapNotNull {
            if (it.startTime != null && it.endTime != null) "${it.startTime}-${it.endTime} (${it.title})" else null
        }.joinToString()

        val systemInstruction = """
            You are Forma AI, a premier daily architect, high-performance executive coach, and mindfulness mentor.
            Generate a rich, structured schedule with 4 to 7 realistic time-blocked tasks and 2 to 3 transformative daily habits.
            Return ONLY a valid JSON object matching this schema:
            {
              "title": "Title of the day plan",
              "summary": "1-2 sentence mindful overview of the day",
              "tasks": [
                {
                  "title": "Task title",
                  "startTime": "HH:mm",
                  "endTime": "HH:mm",
                  "icon": "computer|spa|fitness_center|school|timer|wb_sunny|menu_book|brush|bolt|mail",
                  "colorTag": "#4E6542",
                  "notes": "Short actionable guidance",
                  "subtasks": ["subtask 1", "subtask 2", "subtask 3"]
                }
              ],
              "suggestedHabits": [
                {
                  "name": "Habit name",
                  "timeOfDay": "MORNING|AFTERNOON|EVENING|ANYTIME",
                  "icon": "target|wb_sunny|spa|fitness_center|menu_book|self_improvement",
                  "energyLevel": "HIGH|MEDIUM|LOW",
                  "category": "Mindfulness|Productivity|Health|Learning",
                  "targetStreak": 21
                }
              ],
              "tips": ["coaching tip 1", "coaching tip 2"]
            }
        """.trimIndent()

        val userContext = """
            Date: $date
            Preset: ${preset?.title ?: "Custom"}
            User Prompt: ${userPrompt.ifBlank { "Plan an optimal high-impact mindful day" }}
            Active User Habits: $habitNames
            Occupied Slots: $occupiedSlots
        """.trimIndent()

        val requestBody = JSONObject().apply {
            put("model", "google/gemini-flash-1.5")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemInstruction)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userContext)
                })
            })
            put("response_format", JSONObject().apply {
                put("type", "json_object")
            })
            put("temperature", 0.4)
        }

        OutputStreamWriter(conn.outputStream).use { writer ->
            writer.write(requestBody.toString())
            writer.flush()
        }

        if (conn.responseCode in 200..299) {
            val responseText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            val jsonRoot = JSONObject(responseText)
            val choices = jsonRoot.getJSONArray("choices")
            val firstChoice = choices.getJSONObject(0)
            val message = firstChoice.getJSONObject("message")
            val content = message.getString("content")
            return parseComprehensiveJson(content, date)
        } else {
            return Result.failure(Exception("OpenRouter API returned HTTP ${conn.responseCode}"))
        }
    }

    private fun fetchComprehensivePlanFromGoogleGemini(
        apiKey: String,
        userPrompt: String,
        date: String,
        habits: List<Habit>,
        existingItems: List<TimelineItem>,
        preset: AiPlanPreset?
    ): Result<AiGenerationResult> {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 10000
        conn.readTimeout = 10000

        val habitNames = habits.joinToString { "${it.name} (${it.timeOfDay})" }
        val prompt = """
            You are Forma AI, a mindful daily flow architect.
            Plan a structured schedule for date $date with theme '${preset?.title ?: "Custom Plan"}'.
            User instructions: ${userPrompt.ifBlank { "Create an optimal flow with deep work, health, and mindful habits" }}
            Active habits: $habitNames.
            Return ONLY a valid JSON object matching this schema:
            {
              "title": "Title of the day plan",
              "summary": "1-2 sentence mindful overview of the day",
              "tasks": [
                {
                  "title": "Task title",
                  "startTime": "09:00",
                  "endTime": "10:30",
                  "icon": "computer",
                  "colorTag": "#4E6542",
                  "notes": "Action notes",
                  "subtasks": ["step 1", "step 2"]
                }
              ],
              "suggestedHabits": [
                {
                  "name": "Habit name",
                  "timeOfDay": "MORNING",
                  "icon": "target",
                  "energyLevel": "MEDIUM",
                  "category": "Mindfulness",
                  "targetStreak": 21
                }
              ],
              "tips": ["tip 1", "tip 2"]
            }
        """.trimIndent()

        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.35)
            })
        }

        OutputStreamWriter(conn.outputStream).use { writer ->
            writer.write(requestBody.toString())
            writer.flush()
        }

        if (conn.responseCode == HttpURLConnection.HTTP_OK) {
            val responseText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            val jsonRoot = JSONObject(responseText)
            val candidates = jsonRoot.getJSONArray("candidates")
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val rawText = parts.getJSONObject(0).getString("text")
            return parseComprehensiveJson(rawText, date)
        } else {
            return Result.failure(Exception("Gemini API HTTP ${conn.responseCode}"))
        }
    }

    private fun parseComprehensiveJson(rawJson: String, date: String): Result<AiGenerationResult> {
        return try {
            val cleanJson = rawJson
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val root = JSONObject(cleanJson)
            val title = root.optString("title", "Forma Daily Flow")
            val summary = root.optString("summary", "Mindfully crafted daily agenda tailored to your goals.")

            val tasksList = mutableListOf<TimelineItem>()
            val tasksArray = root.optJSONArray("tasks")
            if (tasksArray != null) {
                for (i in 0 until tasksArray.length()) {
                    val obj = tasksArray.getJSONObject(i)
                    val subtasksJson = obj.optJSONArray("subtasks")
                    val subtasks = mutableListOf<Subtask>()
                    if (subtasksJson != null) {
                        for (s in 0 until subtasksJson.length()) {
                            subtasks.add(Subtask(title = subtasksJson.getString(s), completed = false))
                        }
                    }

                    tasksList.add(
                        TimelineItem(
                            id = UUID.randomUUID().toString(),
                            title = obj.getString("title"),
                            date = date,
                            startTime = obj.optString("startTime", "09:00"),
                            endTime = obj.optString("endTime", "10:00"),
                            icon = obj.optString("icon", "auto_awesome"),
                            colorTag = obj.optString("colorTag", "#4E6542"),
                            notes = obj.optString("notes", ""),
                            subtasks = subtasks
                        )
                    )
                }
            }

            val habitsList = mutableListOf<Habit>()
            val habitsArray = root.optJSONArray("suggestedHabits")
            if (habitsArray != null) {
                for (i in 0 until habitsArray.length()) {
                    val obj = habitsArray.getJSONObject(i)
                    val todString = obj.optString("timeOfDay", "MORNING").uppercase()
                    val tod = try { TimeOfDay.valueOf(todString) } catch (_: Exception) { TimeOfDay.MORNING }
                    val energyString = obj.optString("energyLevel", "MEDIUM").uppercase()
                    val energy = try { EnergyLevel.valueOf(energyString) } catch (_: Exception) { EnergyLevel.MEDIUM }

                    habitsList.add(
                        Habit(
                            id = UUID.randomUUID().toString(),
                            name = obj.getString("name"),
                            icon = obj.optString("icon", "target"),
                            colorTag = "#4E6542",
                            timeOfDay = tod,
                            energyLevel = energy
                        )
                    )
                }
            }

            val tipsList = mutableListOf<String>()
            val tipsArray = root.optJSONArray("tips")
            if (tipsArray != null) {
                for (i in 0 until tipsArray.length()) {
                    tipsList.add(tipsArray.getString(i))
                }
            }

            Result.success(
                AiGenerationResult(
                    title = title,
                    summary = summary,
                    tasks = tasksList,
                    suggestedHabits = habitsList,
                    tips = tipsList
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateAdaptiveHeuristicResult(
        prompt: String,
        date: String,
        habits: List<Habit>,
        existingItems: List<TimelineItem>,
        preset: AiPlanPreset?
    ): AiGenerationResult {
        val tasks = mutableListOf<TimelineItem>()
        val suggestedHabits = mutableListOf<Habit>()
        val tips = mutableListOf<String>()

        val effectivePreset = preset ?: AiPlanPreset.DEEP_WORK

        when (effectivePreset) {
            AiPlanPreset.DEEP_WORK, AiPlanPreset.PRODUCTIVITY_SPRINT -> {
                tasks.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Priority Objective Sprint (Block 1)",
                        date = date,
                        startTime = "09:00",
                        endTime = "11:30",
                        icon = "computer",
                        colorTag = "#4E6542",
                        notes = "Guarded cognitive flow. Zero context switching.",
                        subtasks = listOf(
                            Subtask(title = "Define 1 key milestone for this block", completed = false),
                            Subtask(title = "Execute with notifications silenced", completed = false),
                            Subtask(title = "Commit progress and document findings", completed = false)
                        ),
                        reminderMinutesBefore = 10
                    )
                )
                tasks.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Midday Mindful Reset & Walk",
                        date = date,
                        startTime = "12:30",
                        endTime = "13:15",
                        icon = "self_improvement",
                        colorTag = "#8F9E8B",
                        notes = "Nervous system decompression and daylight exposure.",
                        subtasks = listOf(
                            Subtask(title = "15-minute outdoor walk without phone", completed = false),
                            Subtask(title = "Mindful hydration with minerals", completed = false)
                        )
                    )
                )
                tasks.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Deep Flow Execution (Block 2)",
                        date = date,
                        startTime = "14:00",
                        endTime = "16:00",
                        icon = "bolt",
                        colorTag = "#4E6542",
                        notes = "Afternoon deep building and problem solving.",
                        subtasks = listOf(
                            Subtask(title = "Tackle complex architecture/refactor", completed = false),
                            Subtask(title = "Test and verify edge cases", completed = false)
                        )
                    )
                )
                tasks.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Synthesis & Next-Day Alignment",
                        date = date,
                        startTime = "16:45",
                        endTime = "17:30",
                        icon = "mail",
                        colorTag = "#4A5568",
                        notes = "Close loops, clear messages, and prep tomorrow's top 3 keystones.",
                        subtasks = listOf(
                            Subtask(title = "Clear actionable messages & inbox zero", completed = false),
                            Subtask(title = "Write tomorrow's top 3 intentions", completed = false)
                        )
                    )
                )

                suggestedHabits.add(
                    Habit(
                        id = UUID.randomUUID().toString(),
                        name = "90-Min Deep Focus Window",
                        icon = "computer",
                        colorTag = "#4E6542",
                        timeOfDay = TimeOfDay.MORNING,
                        energyLevel = EnergyLevel.HIGH
                    )
                )
                suggestedHabits.add(
                    Habit(
                        id = UUID.randomUUID().toString(),
                        name = "No Phone First 30 Mins",
                        icon = "spa",
                        colorTag = "#8F9E8B",
                        timeOfDay = TimeOfDay.MORNING,
                        energyLevel = EnergyLevel.MEDIUM
                    )
                )
                suggestedHabits.add(
                    Habit(
                        id = UUID.randomUUID().toString(),
                        name = "Evening Digital Sunset",
                        icon = "nights_stay",
                        colorTag = "#2D3748",
                        timeOfDay = TimeOfDay.EVENING,
                        energyLevel = EnergyLevel.LOW
                    )
                )

                tips.add("Protect your first 2 hours of morning energy before checking notifications.")
                tips.add("Take a 5-minute movement break between every 45 minutes of intense focus.")
            }

            AiPlanPreset.HEALTH_BALANCE, AiPlanPreset.MINDFUL_WEEKEND -> {
                tasks.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Morning Sunlight & Mineral Hydration",
                        date = date,
                        startTime = "07:30",
                        endTime = "08:15",
                        icon = "wb_sunny",
                        colorTag = "#D4A373",
                        notes = "Circadian rhythm anchoring with natural light.",
                        subtasks = listOf(
                            Subtask(title = "500ml water + pinch of salt/lemon", completed = false),
                            Subtask(title = "15-minute sunlight eye exposure", completed = false)
                        )
                    )
                )
                tasks.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Movement & Zone-2 Mobility",
                        date = date,
                        startTime = "10:00",
                        endTime = "11:00",
                        icon = "fitness_center",
                        colorTag = "#4E6542",
                        notes = "Aerobic base work or full-body functional stretching.",
                        subtasks = listOf(
                            Subtask(title = "Dynamic warm-up & joint mobility", completed = false),
                            Subtask(title = "30-min steady state movement", completed = false),
                            Subtask(title = "Cool down & breathing reset", completed = false)
                        )
                    )
                )
                tasks.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Nourishing Meal & Mindful Walk",
                        date = date,
                        startTime = "13:00",
                        endTime = "14:00",
                        icon = "restaurant",
                        colorTag = "#8F9E8B",
                        notes = "High-protein unhurried lunch followed by digestion walk.",
                        subtasks = listOf(
                            Subtask(title = "Nutrient-dense lunch without screens", completed = false),
                            Subtask(title = "10-minute post-meal stroll", completed = false)
                        )
                    )
                )
                tasks.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Mindful Reading & Hot Tea",
                        date = date,
                        startTime = "16:00",
                        endTime = "17:00",
                        icon = "menu_book",
                        colorTag = "#4A5568",
                        notes = "Slow contemplative leisure and offline learning.",
                        subtasks = listOf(
                            Subtask(title = "Read 20 pages of inspiring literature", completed = false),
                            Subtask(title = "Brew herbal tea & journal reflections", completed = false)
                        )
                    )
                )

                suggestedHabits.add(
                    Habit(
                        id = UUID.randomUUID().toString(),
                        name = "Morning Sunlight Walk",
                        icon = "wb_sunny",
                        colorTag = "#D4A373",
                        timeOfDay = TimeOfDay.MORNING,
                        energyLevel = EnergyLevel.HIGH
                    )
                )
                suggestedHabits.add(
                    Habit(
                        id = UUID.randomUUID().toString(),
                        name = "Daily 2L Hydration",
                        icon = "spa",
                        colorTag = "#4E6542",
                        timeOfDay = TimeOfDay.ANYTIME,
                        energyLevel = EnergyLevel.LOW
                    )
                )
                suggestedHabits.add(
                    Habit(
                        id = UUID.randomUUID().toString(),
                        name = "20-Min Nightly Reading",
                        icon = "menu_book",
                        colorTag = "#8F9E8B",
                        timeOfDay = TimeOfDay.EVENING,
                        energyLevel = EnergyLevel.LOW
                    )
                )

                tips.add("Movement in nature lowers cortisol and restores mental clarity.")
                tips.add("Dim blue light screens 90 minutes before bedtime.")
            }

            AiPlanPreset.EXAM_STUDY, AiPlanPreset.CREATIVE_FLOW -> {
                tasks.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Active Recall Sprint (Core Concepts)",
                        date = date,
                        startTime = "09:00",
                        endTime = "10:45",
                        icon = "school",
                        colorTag = "#E67E22",
                        notes = "Test yourself before reviewing materials to build neural connections.",
                        subtasks = listOf(
                            Subtask(title = "Write down formulas/concepts from memory", completed = false),
                            Subtask(title = "Highlight specific blindspots to drill", completed = false)
                        ),
                        reminderMinutesBefore = 10
                    )
                )
                tasks.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Spaced Repetition & Flashcard Drill",
                        date = date,
                        startTime = "11:30",
                        endTime = "12:30",
                        icon = "timer",
                        colorTag = "#3498DB",
                        notes = "High-volume flashcard review and error analysis.",
                        subtasks = listOf(
                            Subtask(title = "Review due flashcards", completed = false),
                            Subtask(title = "Solve 5 practice application problems", completed = false)
                        )
                    )
                )
                tasks.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Timed Mock Exam Simulation",
                        date = date,
                        startTime = "14:30",
                        endTime = "16:00",
                        icon = "brush",
                        colorTag = "#4E6542",
                        notes = "Strict test environment with zero notes or disruptions.",
                        subtasks = listOf(
                            Subtask(title = "Complete 45-minute timed exam block", completed = false),
                            Subtask(title = "Detailed review of missed questions", completed = false)
                        )
                    )
                )
                tasks.add(
                    TimelineItem(
                        id = UUID.randomUUID().toString(),
                        title = "Concept Synthesis & Mind Mapping",
                        date = date,
                        startTime = "16:45",
                        endTime = "17:45",
                        icon = "menu_book",
                        colorTag = "#8F9E8B",
                        notes = "Connect the dots between separate modules for holistic mastery.",
                        subtasks = listOf(
                            Subtask(title = "Draw one-page concept diagram", completed = false),
                            Subtask(title = "Teach the topic aloud (Feynman technique)", completed = false)
                        )
                    )
                )

                suggestedHabits.add(
                    Habit(
                        id = UUID.randomUUID().toString(),
                        name = "Daily Active Recall Drill",
                        icon = "school",
                        colorTag = "#3498DB",
                        timeOfDay = TimeOfDay.MORNING,
                        energyLevel = EnergyLevel.HIGH
                    )
                )
                suggestedHabits.add(
                    Habit(
                        id = UUID.randomUUID().toString(),
                        name = "Teach-Back Reflection",
                        icon = "self_improvement",
                        colorTag = "#4E6542",
                        timeOfDay = TimeOfDay.AFTERNOON,
                        energyLevel = EnergyLevel.MEDIUM
                    )
                )

                tips.add("Interleaving different subjects boosts long-term retention compared to single-subject cramming.")
                tips.add("Sleep is when memory consolidation occurs — aim for 7.5+ hours tonight.")
            }
        }

        return AiGenerationResult(
            title = prompt.ifBlank { effectivePreset.title },
            summary = "AI architected a high-leverage agenda balancing deep focus, restorative pauses, and supporting rituals.",
            tasks = tasks,
            suggestedHabits = suggestedHabits,
            tips = tips
        )
    }

    private fun getFallbackHabitPack(intent: String): List<Habit> {
        return listOf(
            Habit(
                id = UUID.randomUUID().toString(),
                name = "Morning Sunlight & Breath",
                icon = "wb_sunny",
                colorTag = "#D4A373",
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.HIGH
            ),
            Habit(
                id = UUID.randomUUID().toString(),
                name = "Deep Focus Sprint (60m)",
                icon = "computer",
                colorTag = "#4E6542",
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.HIGH
            ),
            Habit(
                id = UUID.randomUUID().toString(),
                name = "Evening Mindful Wind-Down",
                icon = "nights_stay",
                colorTag = "#2D3748",
                timeOfDay = TimeOfDay.EVENING,
                energyLevel = EnergyLevel.LOW
            )
        )
    }
}
