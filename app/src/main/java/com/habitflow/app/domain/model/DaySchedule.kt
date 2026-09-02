package com.habitflow.app.domain.model

sealed interface TodayScheduleItem {
    val id: String
    val sortKey: String // HH:mm or 99:xx for sorting
    val isCompleted: Boolean

    data class HabitItem(
        val habit: Habit,
        val isDoneToday: Boolean,
        val currentStreak: Int,
        val completionId: String? = null
    ) : TodayScheduleItem {
        override val id: String = "habit_${habit.id}"
        override val sortKey: String = when (habit.timeOfDay) {
            TimeOfDay.MORNING -> "08:00_habit"
            TimeOfDay.AFTERNOON -> "13:00_habit"
            TimeOfDay.EVENING -> "19:00_habit"
            TimeOfDay.ANYTIME -> "23:59_habit"
        }
        override val isCompleted: Boolean = isDoneToday
    }

    data class TimelineBlock(
        val item: TimelineItem
    ) : TodayScheduleItem {
        override val id: String = "timeline_${item.id}"
        override val sortKey: String = item.startTime ?: "23:58_task"
        override val isCompleted: Boolean = item.completed
    }
}

data class DaySchedule(
    val date: String,
    val items: List<TodayScheduleItem>,
    val totalHabitsCount: Int,
    val completedHabitsCount: Int,
    val totalTasksCount: Int,
    val completedTasksCount: Int
) {
    val progress: Float
        get() {
            val total = totalHabitsCount + totalTasksCount
            if (total == 0) return 0f
            return (completedHabitsCount + completedTasksCount).toFloat() / total.toFloat()
        }
}
