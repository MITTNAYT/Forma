package com.habitflow.app.domain.repository

import com.habitflow.app.domain.model.DailyReflection
import kotlinx.coroutines.flow.Flow

interface DailyReflectionRepository {
    fun getReflectionForDate(date: String): Flow<DailyReflection?>
    suspend fun getReflectionDirect(date: String): DailyReflection?
    suspend fun saveReflection(reflection: DailyReflection)
    fun getRecentReflections(): Flow<List<DailyReflection>>
}
