package com.habitflow.app.core.di

import android.content.Context
import androidx.room.Room
import com.habitflow.app.core.util.Constants
import com.habitflow.app.data.local.DatabaseCallback
import com.habitflow.app.data.local.FormaDatabase
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
    fun provideFormaDatabase(
        @ApplicationContext context: Context,
        databaseCallback: DatabaseCallback
    ): FormaDatabase {
        return Room.databaseBuilder(
            context,
            FormaDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .addCallback(databaseCallback)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideHabitDao(database: FormaDatabase): HabitDao {
        return database.habitDao()
    }

    @Provides
    fun provideHabitCompletionDao(database: FormaDatabase): HabitCompletionDao {
        return database.habitCompletionDao()
    }

    @Provides
    fun provideTimelineDao(database: FormaDatabase): TimelineDao {
        return database.timelineDao()
    }

    @Provides
    fun provideDailyReflectionDao(database: FormaDatabase): com.habitflow.app.data.local.dao.DailyReflectionDao {
        return database.dailyReflectionDao()
    }
}
