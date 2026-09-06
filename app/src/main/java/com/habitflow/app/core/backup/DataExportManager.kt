package com.habitflow.app.core.backup

import android.content.Context
import android.content.Intent
import com.habitflow.app.core.util.DateUtils
import com.habitflow.app.domain.repository.FocusTrackerRepository
import com.habitflow.app.domain.repository.HabitRepository
import com.habitflow.app.domain.repository.TimelineRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val habitRepository: HabitRepository,
    private val timelineRepository: TimelineRepository,
    private val focusTrackerRepository: FocusTrackerRepository
) {

    suspend fun generateJsonExport(): String {
        val habits = habitRepository.getAllHabits(includeArchived = true).first()
        val timelineItems = timelineRepository.getAllTimelineItems().first()
        val focusStats = focusTrackerRepository.getFocusTimeStats().first()

        val root = JSONObject()
        root.put("appName", "Forma")
        root.put("exportDate", DateUtils.formatDateIso(DateUtils.today()))
        root.put("version", "2.0")

        // Habits
        val habitsArray = JSONArray()
        habits.forEach { habit ->
            val hObj = JSONObject()
            hObj.put("id", habit.id)
            hObj.put("name", habit.name)
            hObj.put("icon", habit.icon)
            hObj.put("colorTag", habit.colorTag)
            hObj.put("timeOfDay", habit.timeOfDay.name)
            hObj.put("energyLevel", habit.energyLevel.name)
            hObj.put("repeatDays", JSONArray(habit.repeatDays))
            habitsArray.put(hObj)
        }
        root.put("habits", habitsArray)

        // Timeline Items
        val timelineArray = JSONArray()
        timelineItems.forEach { item ->
            val tObj = JSONObject()
            tObj.put("id", item.id)
            tObj.put("title", item.title)
            tObj.put("date", item.date)
            tObj.put("startTime", item.startTime)
            tObj.put("endTime", item.endTime)
            tObj.put("completed", item.completed)
            tObj.put("notes", item.notes)
            timelineArray.put(tObj)
        }
        root.put("timelineItems", timelineArray)

        // Focus Metrics
        val focusObj = JSONObject()
        focusObj.put("thisWeekMinutes", focusStats.thisWeekMinutes)
        focusObj.put("thisMonthMinutes", focusStats.thisMonthMinutes)
        focusObj.put("allTimeMinutes", focusStats.allTimeMinutes)
        root.put("focusMetrics", focusObj)

        return root.toString(2)
    }

    suspend fun generateMarkdownJournal(): String {
        val habits = habitRepository.getAllHabits(includeArchived = false).first()
        val timelineItems = timelineRepository.getAllTimelineItems().first()
        val focusStats = focusTrackerRepository.getFocusTimeStats().first()

        val sb = StringBuilder()
        sb.append("# 🌿 Forma — Personal Mindful Flow Journal\n\n")
        sb.append("**Exported on:** ${DateUtils.formatDateIso(DateUtils.today())}\n\n")

        sb.append("## 📊 Focus Velocity Summary\n")
        sb.append("- **This Week:** ${focusStats.thisWeekMinutes / 60}h ${focusStats.thisWeekMinutes % 60}m\n")
        sb.append("- **This Month:** ${focusStats.thisMonthMinutes / 60}h ${focusStats.thisMonthMinutes % 60}m\n")
        sb.append("- **All-Time Focus:** ${focusStats.allTimeMinutes / 60}h ${focusStats.allTimeMinutes % 60}m\n\n")

        sb.append("## 🍃 Active Mindful Rituals (${habits.size})\n")
        habits.forEachIndexed { i, h ->
            sb.append("${i + 1}. **${h.name}** — ${h.timeOfDay.name.lowercase().replaceFirstChar { it.uppercase() }}\n")
        }
        sb.append("\n")

        sb.append("## 📅 Recent Intentions & Time Blocks (${timelineItems.size})\n")
        timelineItems.take(20).forEach { item ->
            val status = if (item.completed) "[x]" else "[ ]"
            val time = if (item.startTime != null) "(${item.startTime} - ${item.endTime})" else ""
            sb.append("- $status **${item.title}** $time\n")
            if (item.notes.isNotBlank()) {
                sb.append("  > *${item.notes}*\n")
            }
        }
        sb.append("\n---\n*Created with Forma — Serene Daily Rituals & Flow Architecture*\n")
        return sb.toString()
    }

    fun shareContent(activityContext: Context, title: String, content: String, mimeType: String = "text/plain") {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TITLE, title)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
            type = mimeType
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(sendIntent, title).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        activityContext.startActivity(chooser)
    }
}
