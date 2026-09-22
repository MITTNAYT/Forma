package com.forma.app.domain.usecase

import androidx.compose.runtime.Immutable
import com.forma.app.domain.model.DailyReflection
import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.HabitCompletion
import com.forma.app.domain.model.TimelineItem
import javax.inject.Inject
import kotlin.math.roundToInt

@Immutable
data class HabitCorrelationInsight(
    val primaryHabitId: String,
    val primaryHabitName: String,
    val primaryHabitIcon: String,
    val correlatedFactor: String,
    val impactPercentage: Int, // e.g. +38 or +25
    val insightSummary: String,
    val isPositive: Boolean = true
)

class CalculateHabitCorrelationsUseCase @Inject constructor() {

    operator fun invoke(
        habits: List<Habit>,
        completions: List<HabitCompletion>,
        reflections: List<DailyReflection>,
        timelineItems: List<TimelineItem>
    ): List<HabitCorrelationInsight> {
        if (habits.isEmpty() || completions.isEmpty()) {
            return emptyList()
        }

        val insights = mutableListOf<HabitCorrelationInsight>()
        val completionsByHabit = completions.groupBy { it.habitId }
        val completionsByDate = completions.groupBy { it.date }
        val reflectionsByDate = reflections.associateBy { it.date }
        val timelineByDate = timelineItems.groupBy { it.date }

        val activeHabits = habits.filter { !it.archived }

        for (habit in activeHabits) {
            val habitDates = (completionsByHabit[habit.id] ?: emptyList()).map { it.date }.toSet()
            if (habitDates.size < 2) continue

            // 1. Check Correlation with Mindfulness Peace Rating
            val ratingsWithHabit: List<Int> = habitDates.mapNotNull { reflectionsByDate[it]?.mindfulnessScore }
            val allRatings: List<Int> = reflections.map { it.mindfulnessScore }

            if (ratingsWithHabit.isNotEmpty() && allRatings.size > ratingsWithHabit.size) {
                val avgWith = ratingsWithHabit.map { it.toDouble() }.average()
                val nonHabitRatings = allRatings.filterIndexed { index, _ -> index >= ratingsWithHabit.size }
                val avgWithout = if (nonHabitRatings.isNotEmpty()) nonHabitRatings.map { it.toDouble() }.average() else 3.5
                val diff = avgWith - avgWithout

                if (diff >= 0.4) {
                    val boostPercent = ((diff / avgWithout) * 100).roundToInt().coerceIn(10, 95)
                    insights.add(
                        HabitCorrelationInsight(
                            primaryHabitId = habit.id,
                            primaryHabitName = habit.name,
                            primaryHabitIcon = habit.icon,
                            correlatedFactor = "Evening Serenity Rating",
                            impactPercentage = boostPercent,
                            insightSummary = "Days with '${habit.name}' correlate with +${boostPercent}% higher peace & mindfulness rating.",
                            isPositive = true
                        )
                    )
                }
            }

            // 2. Check Correlation with Focus Session Minutes
            val focusWithHabit: List<Int> = habitDates.map { date ->
                timelineByDate[date]?.filter { it.completed }?.sumOf { item -> item.getDurationMinutes() } ?: 0
            }.filter { it > 0 }

            val focusWithoutHabit: List<Int> = timelineByDate.filterKeys { !habitDates.contains(it) }.values.flatten()
                .filter { it.completed }.groupBy { it.date }
                .values.map { items -> items.sumOf { item -> item.getDurationMinutes() } }

            if (focusWithHabit.isNotEmpty() && focusWithoutHabit.isNotEmpty()) {
                val avgFocusWith = focusWithHabit.map { it.toDouble() }.average()
                val avgFocusWithout = focusWithoutHabit.map { it.toDouble() }.average()

                if (avgFocusWith > avgFocusWithout && avgFocusWithout > 0.0) {
                    val focusBoost = (((avgFocusWith - avgFocusWithout) / avgFocusWithout) * 100).roundToInt().coerceIn(12, 85)
                    insights.add(
                        HabitCorrelationInsight(
                            primaryHabitId = habit.id,
                            primaryHabitName = habit.name,
                            primaryHabitIcon = habit.icon,
                            correlatedFactor = "Daily Focus Output",
                            impactPercentage = focusBoost,
                            insightSummary = "Practicing '${habit.name}' is tied to a +${focusBoost}% surge in completed deep work minutes.",
                            isPositive = true
                        )
                    )
                }
            }

            // 3. Check Pairwise Habit Synergies
            for (otherHabit in activeHabits) {
                if (otherHabit.id == habit.id) continue
                val otherDates = (completionsByHabit[otherHabit.id] ?: emptyList()).map { it.date }.toSet()
                if (otherDates.size < 2) continue

                val coOccurrences = habitDates.intersect(otherDates).size
                val expectedRate = otherDates.size.toDouble() / 30.0.coerceAtLeast(habitDates.size.toDouble())
                val actualRate = coOccurrences.toDouble() / habitDates.size.toDouble()

                if (actualRate > expectedRate * 1.25 && coOccurrences >= 2) {
                    val synergyPercent = ((actualRate) * 100).roundToInt().coerceIn(60, 100)
                    insights.add(
                        HabitCorrelationInsight(
                            primaryHabitId = habit.id,
                            primaryHabitName = habit.name,
                            primaryHabitIcon = habit.icon,
                            correlatedFactor = otherHabit.name,
                            impactPercentage = synergyPercent,
                            insightSummary = "Synergy: On days you complete '${habit.name}', you finish '${otherHabit.name}' ${synergyPercent}% of the time.",
                            isPositive = true
                        )
                    )
                    break // 1 synergy insight per habit max
                }
            }
        }

        return insights.distinctBy { it.insightSummary }.take(6)
    }

    private fun TimelineItem.getDurationMinutes(): Int {
        if (startTime != null && endTime != null) {
            try {
                val startParts = startTime.split(":").map { it.toInt() }
                val endParts = endTime.split(":").map { it.toInt() }
                val startMin = startParts[0] * 60 + startParts[1]
                val endMin = endParts[0] * 60 + endParts[1]
                if (endMin > startMin) return endMin - startMin
            } catch (_: Exception) {}
        }
        return 25
    }
}
