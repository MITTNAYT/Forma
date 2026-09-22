package com.forma.app.domain.repository

import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.HabitCompletion
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAllHabits(includeArchived: Boolean = false): Flow<List<Habit>>
    fun getHabitById(id: String): Flow<Habit?>
    suspend fun insertHabit(habit: Habit)
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    suspend fun archiveHabit(id: String, archived: Boolean)

    // Completions
    fun getCompletionsForDate(date: String): Flow<List<HabitCompletion>>
    fun getCompletionsForHabit(habitId: String): Flow<List<HabitCompletion>>
    fun getAllCompletions(): Flow<List<HabitCompletion>>
    fun getCompletionsInRange(startDate: String, endDate: String): Flow<List<HabitCompletion>>
    suspend fun recordCompletion(completion: HabitCompletion)
    suspend fun deleteCompletion(habitId: String, date: String)
}
