package com.habitflow.app.core.ai

import com.habitflow.app.domain.model.EnergyLevel
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.TimeOfDay
import com.habitflow.app.domain.model.TimelineItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class AiDaySynthesisResult(
    val suggestedKeystones: List<String>,
    val habitStackRecommendations: List<String>,
    val energyCadenceNote: String,
    val zenAffirmation: String
)

@Singleton
class GeminiDaySynthesisService @Inject constructor() {

    suspend fun synthesizeDay(
        userName: String,
        currentEnergy: EnergyLevel,
        habits: List<Habit>,
        timelineItems: List<TimelineItem>,
        apiKey: String? = null
    ): AiDaySynthesisResult = withContext(Dispatchers.IO) {
        if (!apiKey.isNullOrBlank()) {
            try {
                return@withContext callGeminiApi(userName, currentEnergy, habits, timelineItems, apiKey)
            } catch (_: Exception) {
                // Graceful fallback to heuristic synthesis on network or auth issue
            }
        }
        return@withContext performHeuristicSynthesis(userName, currentEnergy, habits, timelineItems)
    }

    private fun callGeminiApi(
        userName: String,
        currentEnergy: EnergyLevel,
        habits: List<Habit>,
        timelineItems: List<TimelineItem>,
        apiKey: String
    ): AiDaySynthesisResult {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 8000
        conn.readTimeout = 8000

        val habitSummary = habits.joinToString(", ") { "${it.name} (${it.timeOfDay}, energy: ${it.energyLevel})" }
        val itemSummary = timelineItems.joinToString(", ") { it.title }

        val prompt = """
            You are Forma AI, a mindful daily flow architect.
            Synthesize an intentional day plan for $userName based on:
            - Current Energy: $currentEnergy
            - Today's Habits: $habitSummary
            - Today's Tasks: $itemSummary

            Respond ONLY with a valid JSON object matching this schema:
            {
              "suggestedKeystones": ["intention 1", "intention 2", "intention 3"],
              "habitStackRecommendations": ["stack advice 1", "stack advice 2"],
              "energyCadenceNote": "one-sentence energy cadence summary",
              "zenAffirmation": "a calm, serene 1-sentence mindful affirmation"
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
                put("temperature", 0.3)
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
            val text = parts.getJSONObject(0).getString("text")

            val parsed = JSONObject(text)
            val keystones = mutableListOf<String>()
            val keystonesJson = parsed.optJSONArray("suggestedKeystones")
            if (keystonesJson != null) {
                for (i in 0 until keystonesJson.length()) {
                    keystones.add(keystonesJson.getString(i))
                }
            }

            val stacks = mutableListOf<String>()
            val stacksJson = parsed.optJSONArray("habitStackRecommendations")
            if (stacksJson != null) {
                for (i in 0 until stacksJson.length()) {
                    stacks.add(stacksJson.getString(i))
                }
            }

            return AiDaySynthesisResult(
                suggestedKeystones = if (keystones.isNotEmpty()) keystones else listOf("Protect deep morning focus", "Hydrate and walk midday", "Reflect and unwind at dusk"),
                habitStackRecommendations = if (stacks.isNotEmpty()) stacks else listOf("Stack high-energy intentions before noon"),
                energyCadenceNote = parsed.optString("energyCadenceNote", "Honor your natural rhythm with intentional pacing."),
                zenAffirmation = parsed.optString("zenAffirmation", "In stillness and clarity, purposeful progress unfolds.")
            )
        } else {
            throw RuntimeException("Gemini API returned code: ${conn.responseCode}")
        }
    }

    private fun performHeuristicSynthesis(
        userName: String,
        currentEnergy: EnergyLevel,
        habits: List<Habit>,
        timelineItems: List<TimelineItem>
    ): AiDaySynthesisResult {
        val morningHabits = habits.filter { it.timeOfDay == TimeOfDay.MORNING }
        val eveningHabits = habits.filter { it.timeOfDay == TimeOfDay.EVENING }
        val highEnergyHabits = habits.filter { it.energyLevel == EnergyLevel.HIGH }

        val keystones = mutableListOf<String>()

        when (currentEnergy) {
            EnergyLevel.HIGH -> {
                keystones.add(highEnergyHabits.firstOrNull()?.let { "Conquer key ritual: ${it.name}" } ?: "Execute 90-minute deep focus block")
                keystones.add("Channel peak energy into high-leverage outcomes")
                keystones.add("Close the loop on priority tasks before 4 PM")
            }
            EnergyLevel.MEDIUM -> {
                keystones.add(morningHabits.firstOrNull()?.let { "Ground morning rhythm with ${it.name}" } ?: "Protect steady morning momentum")
                keystones.add("Complete core essentials without multitasking")
                keystones.add(eveningHabits.firstOrNull()?.let { "Gentle evening winding down with ${it.name}" } ?: "Evening mindful reflection")
            }
            EnergyLevel.LOW -> {
                keystones.add("Pace gently — focus only on 1 keystone intention")
                keystones.add("Take restorative breathwork breaks between tasks")
                keystones.add("Protect restorative 8-hour sleep sanctuary tonight")
            }
        }

        val stacks = mutableListOf<String>()
        if (morningHabits.size >= 2) {
            stacks.add("Stack: After completing '${morningHabits[0].name}', immediately transition into '${morningHabits[1].name}'.")
        } else if (habits.isNotEmpty()) {
            stacks.add("Stack: After morning sunlight, anchor '${habits[0].name}' into your routine.")
        } else {
            stacks.add("Stack: Anchor your first mindful intention right after pouring your morning tea.")
        }

        val cadence = when (currentEnergy) {
            EnergyLevel.HIGH -> "High energy window active. Front-load deep analytical tasks before midday."
            EnergyLevel.MEDIUM -> "Balanced rhythm. Alternate 45-minute focus intervals with gentle standing breaks."
            EnergyLevel.LOW -> "Conserving energy. Simplify your timeline to the single highest-value priority."
        }

        val affirmation = when (currentEnergy) {
            EnergyLevel.HIGH -> "“Clarity of purpose turns boundless energy into meaningful form.”"
            EnergyLevel.MEDIUM -> "“Small, deliberate actions done with presence create lasting harmony.”"
            EnergyLevel.LOW -> "“Rest is not the absence of progress; it is the soil from which focus grows.”"
        }

        return AiDaySynthesisResult(
            suggestedKeystones = keystones.take(3),
            habitStackRecommendations = stacks,
            energyCadenceNote = cadence,
            zenAffirmation = affirmation
        )
    }
}
