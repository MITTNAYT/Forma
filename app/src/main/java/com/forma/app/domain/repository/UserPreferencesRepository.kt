package com.forma.app.domain.repository

import kotlinx.coroutines.flow.Flow

enum class PaletteFamily(val displayName: String) {
    MATCHA_OAT("Matcha & Oat"),
    COFFEE_CREAM("Coffee & Cream"),
    MONOCHROME("Black & White"),
    TERRACOTTA_SAND("Terracotta & Sand"),
    LAVENDER_MILK("Lavender & Chamomile"),
    WALNUT_ESPRESSO("Coffee & Cream")
}

enum class DarkModeOption(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System")
}

enum class ThemeMode(val displayName: String) {
    MATCHA_OAT("Matcha & Oat"),
    COFFEE_CREAM("Coffee & Cream"),
    MONOCHROME("Black & White"),
    TERRACOTTA_SAND("Terracotta & Sand"),
    LAVENDER_MILK("Lavender & Chamomile"),
    DEFAULT_SAGE("Matcha & Oat"),
    SUNSET_AMBER("Matcha & Oat"),
    WALNUT_ESPRESSO("Coffee & Cream"),
    SYSTEM("System Auto"),
    LIGHT("Clean Light"),
    DARK("Warm Dark")
}

interface UserPreferencesRepository {
    val userName: Flow<String>
    val userHeadline: Flow<String>
    val themeMode: Flow<ThemeMode>
    val paletteFamily: Flow<PaletteFamily>
    val darkModeOption: Flow<DarkModeOption>
    val notificationsEnabled: Flow<Boolean>
    val dailySummaryTimeMinutes: Flow<Int>
    val isOnboardingCompleted: Flow<Boolean>
    val hasSeenTodayCoachMarks: Flow<Boolean>

    suspend fun setUserName(name: String)
    suspend fun setUserHeadline(headline: String)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setPaletteFamily(family: PaletteFamily)
    suspend fun setDarkModeOption(option: DarkModeOption)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setDailySummaryTimeMinutes(minutes: Int)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setHasSeenTodayCoachMarks(seen: Boolean)
}

