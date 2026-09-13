package com.habitflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.habitflow.app.data.local.entity.DailyReflectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyReflectionDao {
    @Query("SELECT * FROM daily_reflections WHERE date = :date LIMIT 1")
    fun getReflectionForDate(date: String): Flow<DailyReflectionEntity?>

    @Query("SELECT * FROM daily_reflections WHERE date = :date LIMIT 1")
    suspend fun getReflectionDirect(date: String): DailyReflectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReflection(reflection: DailyReflectionEntity)

    @Query("SELECT * FROM daily_reflections ORDER BY date DESC LIMIT 30")
    fun getRecentReflections(): Flow<List<DailyReflectionEntity>>
}
