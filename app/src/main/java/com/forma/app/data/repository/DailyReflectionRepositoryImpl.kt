package com.forma.app.data.repository

import com.forma.app.data.local.dao.DailyReflectionDao
import com.forma.app.data.local.entity.DailyReflectionEntity
import com.forma.app.domain.model.DailyReflection
import com.forma.app.domain.repository.DailyReflectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyReflectionRepositoryImpl @Inject constructor(
    private val dailyReflectionDao: DailyReflectionDao
) : DailyReflectionRepository {

    override fun getReflectionForDate(date: String): Flow<DailyReflection?> {
        return dailyReflectionDao.getReflectionForDate(date).map { it?.toDomain() }
    }

    override suspend fun getReflectionDirect(date: String): DailyReflection? {
        return dailyReflectionDao.getReflectionDirect(date)?.toDomain()
    }

    override suspend fun saveReflection(reflection: DailyReflection) {
        dailyReflectionDao.upsertReflection(DailyReflectionEntity.fromDomain(reflection))
    }

    override fun getRecentReflections(): Flow<List<DailyReflection>> {
        return dailyReflectionDao.getRecentReflections().map { list ->
            list.map { it.toDomain() }
        }
    }
}
