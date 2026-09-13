package com.habitflow.app.domain.usecase

import com.habitflow.app.core.util.DateUtils
import com.habitflow.app.domain.model.DaySchedule
import com.habitflow.app.domain.model.TodayScheduleItem
import com.habitflow.app.domain.repository.HabitRepository
import com.habitflow.app.domain.repository.TimelineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class GetTodayTimelineUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val timelineRepository: TimelineRepository,
    private val calculateStreakUseCase: CalculateStreakUseCase
) {
    operator fun invoke(date: LocalDate): Flow<DaySchedule> {
        val dateIso = DateUtils.formatDateIso(date)
        val dayOfWeekInt = DateUtils.getDayOfWeekInt(date)

        return combine(
            habitRepository.getAllHabits(includeArchived = false),
            habitRepository.getAllCompletions(),
            timelineRepository.getTimelineItemsForDate(dateIso)
        ) { habits, allCompletions, timelineItems ->

            val completionsByHabit = allCompletions.groupBy { it.habitId }
            val completedDatesByHabit = completionsByHabit.mapValues { (_, list) -> list.map { it.date }.toSet() }

            val completionsForDate = allCompletions.filter { it.date == dateIso }
            val completedHabitIds = completionsForDate.map { it.habitId }.toSet()
            val completionsMap = completionsForDate.associateBy { it.habitId }

            // Filter habits that repeat on this day of week
            val scheduledHabits = habits.filter { habit ->
                habit.repeatDays.isEmpty() || habit.repeatDays.contains(dayOfWeekInt)
            }

            // Create habit schedule items with fast streak info
            val habitScheduleItems: List<TodayScheduleItem.HabitItem> = scheduledHabits.map { habit ->
                val completedDates = completedDatesByHabit[habit.id] ?: emptySet()
                val currentStreak = calculateStreakUseCase.calculateCurrentStreak(habit, completedDates, date)
                val isDone = completedHabitIds.contains(habit.id)

                TodayScheduleItem.HabitItem(
                    habit = habit,
                    isDoneToday = isDone,
                    currentStreak = currentStreak,
                    completionId = completionsMap[habit.id]?.id
                )
            }

            // Create timeline block items
            val timelineScheduleItems: List<TodayScheduleItem.TimelineBlock> = timelineItems.map { item ->
                TodayScheduleItem.TimelineBlock(item = item)
            }

            // Merge & Sort chronologically
            val allItems: List<TodayScheduleItem> = (habitScheduleItems + timelineScheduleItems)
                .sortedBy { it.sortKey }

            val completedHabitsCount = habitScheduleItems.count { it.isCompleted }
            val completedTasksCount = timelineScheduleItems.count { it.isCompleted }

            DaySchedule(
                date = dateIso,
                items = allItems,
                totalHabitsCount = habitScheduleItems.size,
                completedHabitsCount = completedHabitsCount,
                totalTasksCount = timelineScheduleItems.size,
                completedTasksCount = completedTasksCount
            )
        }
    }
}
