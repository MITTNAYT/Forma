package com.habitflow.app.data.repository

import com.habitflow.app.data.local.dao.HabitCompletionDao
import com.habitflow.app.data.local.dao.HabitDao
import com.habitflow.app.data.local.entity.HabitCompletionEntity
import com.habitflow.app.data.local.entity.HabitEntity
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.HabitCompletion
import com.habitflow.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao
) : HabitRepository {

    override fun getAllHabits(includeArchived: Boolean): Flow<List<Habit>> {
        val flow = if (includeArchived) habitDao.getAllHabits() else habitDao.getActiveHabits()
        return flow.map { list -> list.map { it.toDomain() } }
    }

    override fun getHabitById(id: String): Flow<Habit?> {
        return habitDao.getHabitById(id).map { it?.toDomain() }
    }

    override suspend fun insertHabit(habit: Habit) {
        habitDao.insertHabit(HabitEntity.fromDomain(habit))
    }

    override suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(HabitEntity.fromDomain(habit.copy(updatedAt = System.currentTimeMillis())))
    }

    override suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(HabitEntity.fromDomain(habit))
        habitCompletionDao.deleteAllCompletionsForHabit(habit.id)
    }

    override suspend fun archiveHabit(id: String, archived: Boolean) {
        habitDao.setArchived(id, archived)
    }

    override fun getCompletionsForDate(date: String): Flow<List<HabitCompletion>> {
        return habitCompletionDao.getCompletionsForDate(date).map { list -> list.map { it.toDomain() } }
    }

    override fun getCompletionsForHabit(habitId: String): Flow<List<HabitCompletion>> {
        return habitCompletionDao.getCompletionsForHabit(habitId).map { list -> list.map { it.toDomain() } }
    }

    override fun getAllCompletions(): Flow<List<HabitCompletion>> {
        return habitCompletionDao.getAllCompletions().map { list -> list.map { it.toDomain() } }
    }

    override fun getCompletionsInRange(startDate: String, endDate: String): Flow<List<HabitCompletion>> {
        return habitCompletionDao.getCompletionsInRange(startDate, endDate).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun recordCompletion(completion: HabitCompletion) {
        habitCompletionDao.insertCompletion(HabitCompletionEntity.fromDomain(completion))
    }

    override suspend fun deleteCompletion(habitId: String, date: String) {
        habitCompletionDao.deleteCompletion(habitId, date)
    }
}
