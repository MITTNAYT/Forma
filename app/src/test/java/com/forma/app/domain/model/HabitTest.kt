package com.forma.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class HabitTest {

    @Test
    fun effectiveStartLocalDate_usesExplicitStartDateWhenPresent() {
        val habit = Habit(
            name = "Morning Meditation",
            startDate = "2026-09-30",
            createdAt = 1700000000000L // earlier timestamp
        )

        assertEquals(LocalDate.of(2026, 9, 30), habit.effectiveStartLocalDate)
    }

    @Test
    fun effectiveStartLocalDate_fallsBackToCreatedAtDateWhenStartDateNull() {
        val targetDate = LocalDate.of(2026, 9, 25)
        val epochMillis = targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val habit = Habit(
            name = "Hydration",
            startDate = null,
            createdAt = epochMillis
        )

        assertEquals(targetDate, habit.effectiveStartLocalDate)
    }

    @Test
    fun isScheduledOnDate_returnsFalseBeforeStartDate() {
        val habit = Habit(
            name = "Deep Work",
            startDate = "2026-09-30",
            repeatDays = setOf(1, 2, 3, 4, 5, 6, 7)
        )

        // Day before start
        assertFalse(habit.isScheduledOnDate(LocalDate.of(2026, 9, 29)))
        // 1 month before start
        assertFalse(habit.isScheduledOnDate(LocalDate.of(2026, 8, 30)))
    }

    @Test
    fun isScheduledOnDate_returnsTrueOnAndAfterStartDateWhenDayMatches() {
        val habit = Habit(
            name = "Deep Work",
            startDate = "2026-09-30", // Wednesday (dayOfWeek = 3)
            repeatDays = setOf(3) // Wednesday only
        )

        // On start date (Wednesday)
        assertTrue(habit.isScheduledOnDate(LocalDate.of(2026, 9, 30)))
        // Next Wednesday
        assertTrue(habit.isScheduledOnDate(LocalDate.of(2026, 10, 7)))
        // Following day (Thursday, not in repeatDays)
        assertFalse(habit.isScheduledOnDate(LocalDate.of(2026, 10, 1)))
    }

    @Test
    fun isScheduledOnDate_respectsEndDate() {
        val habit = Habit(
            name = "30-Day Sprint",
            startDate = "2026-09-01",
            endDate = "2026-09-30",
            repeatDays = emptySet() // every day
        )

        assertTrue(habit.isScheduledOnDate(LocalDate.of(2026, 9, 15)))
        assertTrue(habit.isScheduledOnDate(LocalDate.of(2026, 9, 30)))
        // After end date
        assertFalse(habit.isScheduledOnDate(LocalDate.of(2026, 10, 1)))
    }
}
