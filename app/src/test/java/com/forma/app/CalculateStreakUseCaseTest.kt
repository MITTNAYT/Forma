package com.forma.app

import com.forma.app.core.util.DateUtils
import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.HabitCompletion
import com.forma.app.domain.usecase.CalculateStreakUseCase
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CalculateStreakUseCaseTest {

    private lateinit var calculateStreakUseCase: CalculateStreakUseCase

    @Before
    fun setUp() {
        calculateStreakUseCase = CalculateStreakUseCase()
    }


    @Test
    fun `consecutive daily completions calculate active streak correctly`() {
        val referenceDate = LocalDate.of(2026, 9, 2)
        val habit = Habit(
            id = "habit_1",
            name = "Morning Hydration",
            repeatDays = setOf(1, 2, 3, 4, 5, 6, 7),
            createdAt = referenceDate.minusDays(10).toEpochDay() * 24 * 60 * 60 * 1000
        )

        val completions = listOf(
            HabitCompletion(habitId = "habit_1", date = "2026-09-02"),
            HabitCompletion(habitId = "habit_1", date = "2026-09-01"),
            HabitCompletion(habitId = "habit_1", date = "2026-08-31"),
            HabitCompletion(habitId = "habit_1", date = "2026-08-30")
        )

        val streakInfo = calculateStreakUseCase(habit, completions, referenceDate)

        assertEquals(4, streakInfo.currentStreak)
        assertEquals(4, streakInfo.longestStreak)
        assertEquals(4, streakInfo.totalCompletions)
    }

    @Test
    fun `weekday-only habit does not break streak over weekend`() {
        // 2026-09-07 is a Monday (Day 1)
        val monday = LocalDate.of(2026, 9, 7)
        val habit = Habit(
            id = "habit_work",
            name = "Deep Work Session",
            repeatDays = setOf(1, 2, 3, 4, 5), // Mon-Fri
            createdAt = monday.minusDays(14).toEpochDay() * 24 * 60 * 60 * 1000
        )

        // Completed on Monday (2026-09-07) and preceding Friday (2026-09-04)
        val completions = listOf(
            HabitCompletion(habitId = "habit_work", date = "2026-09-07"),
            HabitCompletion(habitId = "habit_work", date = "2026-09-04"),
            HabitCompletion(habitId = "habit_work", date = "2026-09-03")
        )

        val streakInfo = calculateStreakUseCase(habit, completions, monday)

        assertEquals(3, streakInfo.currentStreak)
    }

    @Test
    fun `missed day breaks current streak`() {
        val referenceDate = LocalDate.of(2026, 9, 5)
        val habit = Habit(
            id = "habit_daily",
            name = "Reading",
            repeatDays = setOf(1, 2, 3, 4, 5, 6, 7),
            createdAt = referenceDate.minusDays(10).toEpochDay() * 24 * 60 * 60 * 1000
        )

        // Completed on Sep 5 and Sep 4, but missed Sep 3, completed Sep 2 and Sep 1
        val completions = listOf(
            HabitCompletion(habitId = "habit_daily", date = "2026-09-05"),
            HabitCompletion(habitId = "habit_daily", date = "2026-09-04"),
            HabitCompletion(habitId = "habit_daily", date = "2026-09-02"),
            HabitCompletion(habitId = "habit_daily", date = "2026-09-01")
        )

        val streakInfo = calculateStreakUseCase(habit, completions, referenceDate)

        assertEquals(2, streakInfo.currentStreak)
        assertEquals(2, streakInfo.longestStreak)
        assertEquals(4, streakInfo.totalCompletions)
    }
}
