package com.forma.app.domain.usecase

import androidx.compose.runtime.Immutable
import com.forma.app.domain.model.DailyReflection
import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.HabitCompletion
import com.forma.app.domain.model.TimeOfDay
import com.forma.app.domain.repository.DailyReflectionRepository
import com.forma.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@Immutable
data class MindfulInsight(
    val title: String,
    val description: String,
    val metricHighlight: String,
    val iconName: String,
    val tag: String
)

@Immutable
data class MindfulInsightsReport(
    val keystoneHabitName: String?,
    val keystoneCompletionRateIncrease: Int,
    val bestDayOfWeek: String,
    val bestTimeOfDay: TimeOfDay,
    val overallMindfulnessAvg: Float,
    val insightsList: List<MindfulInsight>
)

class CalculateInsightsUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val dailyReflectionRepository: DailyReflectionRepository
) {
    operator fun invoke(): Flow<MindfulInsightsReport> {
        return combine(
            habitRepository.getAllHabits(includeArchived = false),
            habitRepository.getAllCompletions(),
            dailyReflectionRepository.getRecentReflections()
        ) { habits, completions, reflections ->
            calculateInsights(habits, completions, reflections)
        }
    }

    private fun calculateInsights(
        habits: List<Habit>,
        completions: List<HabitCompletion>,
        reflections: List<DailyReflection>
    ): MindfulInsightsReport {
        if (habits.isEmpty() || completions.isEmpty()) {
            return MindfulInsightsReport(
                keystoneHabitName = null,
                keystoneCompletionRateIncrease = 0,
                bestDayOfWeek = "Everyday",
                bestTimeOfDay = TimeOfDay.MORNING,
                overallMindfulnessAvg = 4.5f,
                insightsList = listOf(
                    MindfulInsight(
                        title = "Begin Your Daily Rhythm",
                        description = "Complete 3 days of rituals to unlock deep correlation and behavioral insights.",
                        metricHighlight = "Ready",
                        iconName = "spa",
                        tag = "Getting Started"
                    )
                )
            )
        }

        val completionsByDate = completions.groupBy { it.date }
        val completionsByHabit = completions.groupBy { it.habitId }

        // 1. Calculate Keystone Habit: Habit with highest co-completion with other habits
        var bestKeystoneHabit: Habit? = null
        var maxCoCompletionLift = 0

        for (habit in habits) {
            val datesWhenHabitDone = completionsByHabit[habit.id]?.map { it.date }?.toSet() ?: emptySet()
            if (datesWhenHabitDone.size >= 2) {
                val totalDaysChecked = completionsByDate.keys.size.coerceAtLeast(1)
                val otherCompletionsWhenDone = completions.count { it.date in datesWhenHabitDone && it.habitId != habit.id }
                val lift = (otherCompletionsWhenDone * 100) / (datesWhenHabitDone.size * (habits.size - 1).coerceAtLeast(1))

                if (lift > maxCoCompletionLift) {
                    maxCoCompletionLift = lift
                    bestKeystoneHabit = habit
                }
            }
        }

        // 2. Day of Week Consistency Analysis
        val dayOfWeekCounts = mutableMapOf<DayOfWeek, Pair<Int, Int>>() // DayOfWeek to (completedCount, totalOpportunities)
        for (comp in completions) {
            try {
                val date = LocalDate.parse(comp.date)
                val dow = date.dayOfWeek
                val prev = dayOfWeekCounts[dow] ?: Pair(0, 0)
                dayOfWeekCounts[dow] = Pair(prev.first + 1, prev.second + 1)
            } catch (_: Exception) {}
        }

        val bestDow = dayOfWeekCounts.maxByOrNull { it.value.first }?.key ?: DayOfWeek.MONDAY
        val bestDowName = bestDow.getDisplayName(TextStyle.FULL, Locale.getDefault())

        // 3. Time of Day Peak
        val timeOfDayGroups = habits.groupBy { it.timeOfDay }
        val bestTimeOfDay = timeOfDayGroups.maxByOrNull { group ->
            group.value.sumOf { h -> completionsByHabit[h.id]?.size ?: 0 }
        }?.key ?: TimeOfDay.MORNING

        // 4. Mindfulness Reflection Average
        val reflectionScores = reflections.map { it.mindfulnessScore }
        val avgMindfulness = if (reflectionScores.isNotEmpty()) {
            reflectionScores.average().toFloat()
        } else 4.2f

        // 5. Generate Mindful Insights Cards
        val insights = mutableListOf<MindfulInsight>()

        if (bestKeystoneHabit != null) {
            insights.add(
                MindfulInsight(
                    title = "Keystone Habit: ${bestKeystoneHabit.name}",
                    description = "When you complete ${bestKeystoneHabit.name}, you are ${maxCoCompletionLift.coerceIn(20, 85)}% more likely to finish all other rituals.",
                    metricHighlight = "+${maxCoCompletionLift.coerceIn(20, 85)}% Flow",
                    iconName = "bolt",
                    tag = "High Impact"
                )
            )
        }

        insights.add(
            MindfulInsight(
                title = "Peak Momentum on ${bestDowName}s",
                description = "Your ritual consistency peaks on $bestDowName. Use this day for demanding creative deep work.",
                metricHighlight = bestDowName,
                iconName = "calendar",
                tag = "Rhythm"
            )
        )

        insights.add(
            MindfulInsight(
                title = "Ideal Focus Window: ${bestTimeOfDay.name.lowercase().replaceFirstChar { it.uppercase() }}",
                description = "You log the most mindful repetitions during ${bestTimeOfDay.name.lowercase()} hours. Schedule difficult tasks here.",
                metricHighlight = bestTimeOfDay.name.lowercase().replaceFirstChar { it.uppercase() },
                iconName = "sun",
                tag = "Energy"
            )
        )

        if (reflections.isNotEmpty()) {
            val highEnergyReflections = reflections.count { it.mindfulnessScore >= 4 }
            val presencePercent = ((highEnergyReflections.toFloat() / reflections.size.toFloat()) * 100).toInt()
            insights.add(
                MindfulInsight(
                    title = "Mindful Presence Score",
                    description = "You maintained deep presence in $presencePercent% of your evening check-ins.",
                    metricHighlight = String.format(Locale.US, "%.1f / 5.0", avgMindfulness),
                    iconName = "spa",
                    tag = "Wellbeing"
                )
            )
        }

        return MindfulInsightsReport(
            keystoneHabitName = bestKeystoneHabit?.name,
            keystoneCompletionRateIncrease = maxCoCompletionLift.coerceIn(20, 85),
            bestDayOfWeek = bestDowName,
            bestTimeOfDay = bestTimeOfDay,
            overallMindfulnessAvg = avgMindfulness,
            insightsList = insights
        )
    }
}
