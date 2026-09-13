package com.habitflow.app.core.di

import android.content.Context
import androidx.room.Room
import com.habitflow.app.core.util.Constants
import com.habitflow.app.data.local.DatabaseCallback
import com.habitflow.app.data.local.HabitFlowDatabase
import com.habitflow.app.data.local.dao.HabitCompletionDao
import com.habitflow.app.data.local.dao.HabitDao
import com.habitflow.app.data.local.dao.TimelineDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabaseCallback(
        habitDaoProvider: Provider<HabitDao>,
        habitCompletionDaoProvider: Provider<HabitCompletionDao>,
        timelineDaoProvider: Provider<TimelineDao>
    ): DatabaseCallback {
        return DatabaseCallback(
            habitDaoProvider = habitDaoProvider,
            habitCompletionDaoProvider = habitCompletionDaoProvider,
            timelineDaoProvider = timelineDaoProvider
        )
    }

    @Provides
    @Singleton
    fun provideHabitFlowDatabase(
        @ApplicationContext context: Context,
        databaseCallback: DatabaseCallback
    ): HabitFlowDatabase {
        return Room.databaseBuilder(
            context,
            HabitFlowDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .addCallback(databaseCallback)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideHabitDao(database: HabitFlowDatabase): HabitDao {
        return database.habitDao()
    }

    @Provides
    fun provideHabitCompletionDao(database: HabitFlowDatabase): HabitCompletionDao {
        return database.habitCompletionDao()
    }

    @Provides
    fun provideTimelineDao(database: HabitFlowDatabase): TimelineDao {
        return database.timelineDao()
    }

    @Provides
    fun provideDailyReflectionDao(database: HabitFlowDatabase): com.habitflow.app.data.local.dao.DailyReflectionDao {
        return database.dailyReflectionDao()
    }
}

