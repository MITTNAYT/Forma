package com.habitflow.app.core.di

import com.habitflow.app.data.repository.AiPlannerRepositoryImpl
import com.habitflow.app.data.repository.BillingRepositoryImpl
import com.habitflow.app.data.repository.FocusTrackerRepositoryImpl
import com.habitflow.app.data.repository.HabitRepositoryImpl
import com.habitflow.app.data.repository.TimelineRepositoryImpl
import com.habitflow.app.data.repository.UserPreferencesRepositoryImpl
import com.habitflow.app.domain.repository.AiPlannerRepository
import com.habitflow.app.domain.repository.BillingRepository
import com.habitflow.app.domain.repository.FocusTrackerRepository
import com.habitflow.app.domain.repository.HabitRepository
import com.habitflow.app.domain.repository.TimelineRepository
import com.habitflow.app.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHabitRepository(
        habitRepositoryImpl: HabitRepositoryImpl
    ): HabitRepository

    @Binds
    @Singleton
    abstract fun bindTimelineRepository(
        timelineRepositoryImpl: TimelineRepositoryImpl
    ): TimelineRepository

    @Binds
    @Singleton
    abstract fun bindBillingRepository(
        billingRepositoryImpl: BillingRepositoryImpl
    ): BillingRepository

    @Binds
    @Singleton
    abstract fun bindAiPlannerRepository(
        aiPlannerRepositoryImpl: AiPlannerRepositoryImpl
    ): AiPlannerRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindFocusTrackerRepository(
        focusTrackerRepositoryImpl: FocusTrackerRepositoryImpl
    ): FocusTrackerRepository

    @Binds
    @Singleton
    abstract fun bindDailyReflectionRepository(
        dailyReflectionRepositoryImpl: com.habitflow.app.data.repository.DailyReflectionRepositoryImpl
    ): com.habitflow.app.domain.repository.DailyReflectionRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: com.habitflow.app.data.repository.AuthRepositoryImpl
    ): com.habitflow.app.domain.repository.AuthRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        syncRepositoryImpl: com.habitflow.app.data.repository.SyncRepositoryImpl
    ): com.habitflow.app.domain.repository.SyncRepository

    @Binds
    @Singleton
    abstract fun bindCircleRepository(
        circleRepositoryImpl: com.habitflow.app.data.repository.CircleRepositoryImpl
    ): com.habitflow.app.domain.repository.CircleRepository
}

