package com.forma.app.core.di

import com.forma.app.data.repository.AiPlannerRepositoryImpl
import com.forma.app.data.repository.BillingRepositoryImpl
import com.forma.app.data.repository.FocusTrackerRepositoryImpl
import com.forma.app.data.repository.HabitRepositoryImpl
import com.forma.app.data.repository.TimelineRepositoryImpl
import com.forma.app.data.repository.UserPreferencesRepositoryImpl
import com.forma.app.domain.repository.AiPlannerRepository
import com.forma.app.domain.repository.BillingRepository
import com.forma.app.domain.repository.FocusTrackerRepository
import com.forma.app.domain.repository.HabitRepository
import com.forma.app.domain.repository.TimelineRepository
import com.forma.app.domain.repository.UserPreferencesRepository
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
        dailyReflectionRepositoryImpl: com.forma.app.data.repository.DailyReflectionRepositoryImpl
    ): com.forma.app.domain.repository.DailyReflectionRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: com.forma.app.data.repository.AuthRepositoryImpl
    ): com.forma.app.domain.repository.AuthRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        syncRepositoryImpl: com.forma.app.data.repository.SyncRepositoryImpl
    ): com.forma.app.domain.repository.SyncRepository

    @Binds
    @Singleton
    abstract fun bindCircleRepository(
        circleRepositoryImpl: com.forma.app.data.repository.CircleRepositoryImpl
    ): com.forma.app.domain.repository.CircleRepository
}

