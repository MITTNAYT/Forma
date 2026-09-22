package com.forma.app.domain.usecase

import com.forma.app.domain.model.DailyReflection
import com.forma.app.domain.model.EnergyLevel
import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.HabitCompletion
import com.forma.app.domain.model.Subtask
import com.forma.app.domain.model.TimeOfDay
import com.forma.app.domain.model.TimelineItem
import com.forma.app.domain.repository.DailyReflectionRepository
import com.forma.app.domain.repository.HabitRepository
import com.forma.app.domain.repository.TimelineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class ExportDataUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val timelineRepository: TimelineRepository,
    private val dailyReflectionRepository: DailyReflectionRepository
) {
    suspend operator fun invoke(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 2)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("appName", "Forma")

        // 1. Habits
        val habits = habitRepository.getAllHabits(includeArchived = true).first()
        val habitsArray = JSONArray()
        for (h in habits) {
            val hObj = JSONObject().apply {
                put("id", h.id)
                put("name", h.name)
                put("icon", h.icon)
                put("colorTag", h.colorTag)
                put("timeOfDay", h.timeOfDay.name)
                put("energyLevel", h.energyLevel.name)
                put("repeatDays", JSONArray(h.repeatDays))
                put("createdAt", h.createdAt)
                put("archived", h.archived)
                put("reminderTimeMinutes", h.reminderTimeMinutes)
                put("updatedAt", h.updatedAt)
                put("stackedAfterHabitId", h.stackedAfterHabitId)
                put("stackedCueText", h.stackedCueText)
                put("isWintering", h.isWintering)
                put("startDate", h.startDate)
                put("endDate", h.endDate)
                put("isIndefinite", h.isIndefinite)
            }
            habitsArray.put(hObj)
        }
        root.put("habits", habitsArray)

        // 2. Completions
        val completions = habitRepository.getAllCompletions().first()
        val completionsArray = JSONArray()
        for (c in completions) {
            val cObj = JSONObject().apply {
                put("id", c.id)
                put("habitId", c.habitId)
                put("date", c.date)
                put("completedAt", c.completedAt)
                put("updatedAt", c.updatedAt)
            }
            completionsArray.put(cObj)
        }
        root.put("completions", completionsArray)

        // 3. Timeline Items
        val timelineItems = timelineRepository.getAllTimelineItems().first()
        val timelineArray = JSONArray()
        for (t in timelineItems) {
            val tObj = JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("date", t.date)
                put("startTime", t.startTime)
                put("endTime", t.endTime)
                put("icon", t.icon)
                put("colorTag", t.colorTag)
                put("notes", t.notes)
                put("isRecurring", t.isRecurring)
                put("repeatDays", JSONArray(t.repeatDays))
                put("reminderMinutesBefore", t.reminderMinutesBefore)
                put("completed", t.completed)
                put("updatedAt", t.updatedAt)

                val subtasksArray = JSONArray()
                for (s in t.subtasks) {
                    subtasksArray.put(JSONObject().apply {
                        put("id", s.id)
                        put("title", s.title)
                        put("completed", s.completed)
                    })
                }
                put("subtasks", subtasksArray)
            }
            timelineArray.put(tObj)
        }
        root.put("timelineItems", timelineArray)

        // 4. Daily Reflections
        val reflections = dailyReflectionRepository.getRecentReflections().first()
        val reflectionsArray = JSONArray()
        for (r in reflections) {
            val rObj = JSONObject().apply {
                put("date", r.date)
                put("keystoneIntentions", JSONArray(r.keystoneIntentions))
                put("gratitudeNote", r.gratitudeNote)
                put("mindfulnessScore", r.mindfulnessScore)
                put("energyLevel", r.energyLevel.name)
                put("isMorningCompleted", r.isMorningCompleted)
                put("isEveningCompleted", r.isEveningCompleted)
                put("updatedAt", r.updatedAt)
            }
            reflectionsArray.put(rObj)
        }
        root.put("reflections", reflectionsArray)

        root.toString(2)
    }
}

class ImportDataUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val timelineRepository: TimelineRepository,
    private val dailyReflectionRepository: DailyReflectionRepository
) {
    suspend operator fun invoke(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            var itemsRestored = 0

            // 1. Restore Habits
            if (root.has("habits")) {
                val habitsArray = root.getJSONArray("habits")
                for (i in 0 until habitsArray.length()) {
                    val hObj = habitsArray.getJSONObject(i)
                    val repeatDays = mutableSetOf<Int>()
                    if (hObj.has("repeatDays")) {
                        val arr = hObj.getJSONArray("repeatDays")
                        for (j in 0 until arr.length()) repeatDays.add(arr.getInt(j))
                    } else {
                        repeatDays.addAll(setOf(1, 2, 3, 4, 5, 6, 7))
                    }

                    val timeOfDayStr = hObj.optString("timeOfDay", TimeOfDay.ANYTIME.name)
                    val timeOfDay = try { TimeOfDay.valueOf(timeOfDayStr) } catch (_: Exception) { TimeOfDay.ANYTIME }

                    val energyLevelStr = hObj.optString("energyLevel", EnergyLevel.MEDIUM.name)
                    val energyLevel = try { EnergyLevel.valueOf(energyLevelStr) } catch (_: Exception) { EnergyLevel.MEDIUM }

                    val habit = Habit(
                        id = hObj.optString("id", java.util.UUID.randomUUID().toString()),
                        name = hObj.optString("name", "Habit"),
                        icon = hObj.optString("icon", "target"),
                        colorTag = hObj.optString("colorTag", "#4E6542"),
                        timeOfDay = timeOfDay,
                        energyLevel = energyLevel,
                        repeatDays = repeatDays,
                        createdAt = hObj.optLong("createdAt", System.currentTimeMillis()),
                        archived = hObj.optBoolean("archived", false),
                        reminderTimeMinutes = if (hObj.isNull("reminderTimeMinutes")) null else hObj.optInt("reminderTimeMinutes"),
                        updatedAt = hObj.optLong("updatedAt", System.currentTimeMillis()),
                        stackedAfterHabitId = if (hObj.isNull("stackedAfterHabitId")) null else hObj.optString("stackedAfterHabitId"),
                        stackedCueText = if (hObj.isNull("stackedCueText")) null else hObj.optString("stackedCueText"),
                        isWintering = hObj.optBoolean("isWintering", false),
                        startDate = if (hObj.isNull("startDate")) null else hObj.optString("startDate"),
                        endDate = if (hObj.isNull("endDate")) null else hObj.optString("endDate"),
                        isIndefinite = hObj.optBoolean("isIndefinite", true)
                    )
                    habitRepository.insertHabit(habit)
                    itemsRestored++
                }
            }

            // 2. Restore Completions
            if (root.has("completions")) {
                val compArray = root.getJSONArray("completions")
                for (i in 0 until compArray.length()) {
                    val cObj = compArray.getJSONObject(i)
                    val completion = HabitCompletion(
                        id = cObj.optString("id", java.util.UUID.randomUUID().toString()),
                        habitId = cObj.optString("habitId", ""),
                        date = cObj.optString("date", ""),
                        completedAt = cObj.optLong("completedAt", System.currentTimeMillis()),
                        updatedAt = cObj.optLong("updatedAt", System.currentTimeMillis())
                    )
                    if (completion.habitId.isNotBlank() && completion.date.isNotBlank()) {
                        habitRepository.recordCompletion(completion)
                        itemsRestored++
                    }
                }
            }

            // 3. Restore Timeline Items
            if (root.has("timelineItems")) {
                val timeArray = root.getJSONArray("timelineItems")
                for (i in 0 until timeArray.length()) {
                    val tObj = timeArray.getJSONObject(i)

                    val subtasks = mutableListOf<Subtask>()
                    if (tObj.has("subtasks")) {
                        val sArr = tObj.getJSONArray("subtasks")
                        for (k in 0 until sArr.length()) {
                            val sObj = sArr.getJSONObject(k)
                            subtasks.add(
                                Subtask(
                                    id = sObj.optString("id", java.util.UUID.randomUUID().toString()),
                                    title = sObj.optString("title", ""),
                                    completed = sObj.optBoolean("completed", false)
                                )
                            )
                        }
                    }

                    val repeatDays = mutableSetOf<Int>()
                    if (tObj.has("repeatDays")) {
                        val arr = tObj.getJSONArray("repeatDays")
                        for (j in 0 until arr.length()) repeatDays.add(arr.getInt(j))
                    } else {
                        repeatDays.addAll(setOf(1, 2, 3, 4, 5, 6, 7))
                    }

                    val timeline = TimelineItem(
                        id = tObj.optString("id", java.util.UUID.randomUUID().toString()),
                        title = tObj.optString("title", "Task"),
                        date = tObj.optString("date", ""),
                        startTime = if (tObj.isNull("startTime")) null else tObj.optString("startTime"),
                        endTime = if (tObj.isNull("endTime")) null else tObj.optString("endTime"),
                        icon = tObj.optString("icon", "pin"),
                        colorTag = tObj.optString("colorTag", "#EB5757"),
                        notes = tObj.optString("notes", ""),
                        subtasks = subtasks,
                        isRecurring = tObj.optBoolean("isRecurring", false),
                        repeatDays = repeatDays,
                        reminderMinutesBefore = if (tObj.isNull("reminderMinutesBefore")) null else tObj.optInt("reminderMinutesBefore"),
                        completed = tObj.optBoolean("completed", false),
                        updatedAt = tObj.optLong("updatedAt", System.currentTimeMillis())
                    )
                    timelineRepository.insertTimelineItem(timeline)
                    itemsRestored++
                }
            }

            // 4. Restore Daily Reflections
            if (root.has("reflections")) {
                val refArray = root.getJSONArray("reflections")
                for (i in 0 until refArray.length()) {
                    val rObj = refArray.getJSONObject(i)
                    val intentions = mutableListOf<String>()
                    if (rObj.has("keystoneIntentions")) {
                        val kArr = rObj.getJSONArray("keystoneIntentions")
                        for (k in 0 until kArr.length()) intentions.add(kArr.getString(k))
                    }
                    val energyStr = rObj.optString("energyLevel", EnergyLevel.MEDIUM.name)
                    val energy = try { EnergyLevel.valueOf(energyStr) } catch (_: Exception) { EnergyLevel.MEDIUM }

                    val reflection = DailyReflection(
                        date = rObj.optString("date", ""),
                        keystoneIntentions = intentions,
                        gratitudeNote = rObj.optString("gratitudeNote", ""),
                        mindfulnessScore = rObj.optInt("mindfulnessScore", 5),
                        energyLevel = energy,
                        isMorningCompleted = rObj.optBoolean("isMorningCompleted", false),
                        isEveningCompleted = rObj.optBoolean("isEveningCompleted", false),
                        updatedAt = rObj.optLong("updatedAt", System.currentTimeMillis())
                    )
                    dailyReflectionRepository.saveReflection(reflection)
                    itemsRestored++
                }
            }

            Result.success(itemsRestored)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
