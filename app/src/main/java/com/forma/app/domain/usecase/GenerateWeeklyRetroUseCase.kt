package com.forma.app.domain.usecase

import androidx.compose.runtime.Immutable
import com.forma.app.core.util.DateUtils
import com.forma.app.domain.model.DailyReflection
import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.HabitCompletion
import com.forma.app.domain.model.TimelineItem
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

@Immutable
data class WeeklyZenRetro(
    val weekDateRange: String,
    val totalHabitsCompleted: Int,
    val totalFocusMinutes: Int,
    val averagePeaceRating: Float,
    val topPerformingHabit: Habit?,
    val frictionHabit: Habit?,
    val frictionRecommendation: String?,
    val consistencyScore: Int, // 0 - 100%
    val zenAffirmation: String
)

class GenerateWeeklyRetroUseCase @Inject constructor() {

    operator fun invoke(
        habits: List<Habit>,
        completions: List<HabitCompletion>,
        reflections: List<DailyReflection>,
        timelineItems: List<TimelineItem>,
        referenceDate: LocalDate = LocalDate.now()
    ): WeeklyZenRetro {
        val weekDates = (0..6).map { offset ->
            DateUtils.formatDateIso(referenceDate.minusDays(offset.toLong()))
        }.toSet()

        val weekCompletions = completions.filter { weekDates.contains(it.date) }
        val weekReflections = reflections.filter { weekDates.contains(it.date) }
        val weekTimeline = timelineItems.filter { weekDates.contains(it.date) && it.completed }

        val totalHabitsDone = weekCompletions.size
        val totalFocusMins = weekTimeline.sumOf { item ->
            if (item.startTime != null && item.endTime != null) {
                try {
                    val startParts = item.startTime.split(":").map { it.toInt() }
                    val endParts = item.endTime.split(":").map { it.toInt() }
                    val startMin = startParts[0] * 60 + startParts[1]
                    val endMin = endParts[0] * 60 + endParts[1]
                    if (endMin > startMin) endMin - startMin else 25
                } catch (_: Exception) { 25 }
            } else 25
        }
        val avgPeace = if (weekReflections.isNotEmpty()) {
            weekReflections.map { it.mindfulnessScore.toDouble() }.average().toFloat()
        } else 4.8f

        val activeHabits = habits.filter { !it.archived }
        val completionsByHabit = weekCompletions.groupBy { it.habitId }

        var topHabit: Habit? = null
        var maxDone = -1
        var frictionHabit: Habit? = null
        var minDone = 999

        for (habit in activeHabits) {
            val count = completionsByHabit[habit.id]?.size ?: 0
            if (count > maxDone) {
                maxDone = count
                topHabit = habit
            }
            if (count < 3 && count < minDone) {
                minDone = count
                frictionHabit = habit
            }
        }

        val frictionRecommendation = frictionHabit?.let { habit ->
            when {
                !habit.stackedCueText.isNullOrBlank() -> "Friction detected on '${habit.name}'. Try anchoring it to a more prominent cue or shortening its scope to 2 minutes."
                habit.energyLevel.name == "HIGH" -> "'${habit.name}' requires high cognitive energy. Consider rescheduling it earlier during your morning alignment."
                else -> "'${habit.name}' was completed $minDone/7 days. You can enable 'Wintering Mode' if this is a busy season, or pair it with an atomic cue."
            }
        }

        val totalExpected = (activeHabits.size * 7).coerceAtLeast(1)
        val consistencyScore = ((totalHabitsDone.toDouble() / totalExpected.toDouble()) * 100).roundToInt().coerceIn(15, 100)

        val startDateStr = referenceDate.minusDays(6).let { "${it.month.name.take(3)} ${it.dayOfMonth}" }
        val endDateStr = referenceDate.let { "${it.month.name.take(3)} ${it.dayOfMonth}, ${it.year}" }

        val affirmations = listOf(
            "Progress is not perfection; it is the quiet devotion of returning to the path.",
            "Each mindful breath creates the calm architecture of your life.",
            "Water shapes the hardest rock not by force, but by daily gentle flow.",
            "You are the master of your attention and the guardian of your peace."
        )

        return WeeklyZenRetro(
            weekDateRange = "$startDateStr – $endDateStr",
            totalHabitsCompleted = totalHabitsDone,
            totalFocusMinutes = totalFocusMins,
            averagePeaceRating = avgPeace,
            topPerformingHabit = topHabit,
            frictionHabit = frictionHabit,
            frictionRecommendation = frictionRecommendation,
            consistencyScore = consistencyScore,
            zenAffirmation = affirmations.random()
        )
    }
}
