package com.habitflow.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.habitflow.app.core.util.Constants
import com.habitflow.app.domain.repository.DarkModeOption
import com.habitflow.app.domain.repository.PaletteFamily
import com.habitflow.app.domain.repository.ThemeMode
import com.habitflow.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.PREFERENCES_NAME)

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_HEADLINE = stringPreferencesKey("user_headline")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PALETTE_FAMILY = stringPreferencesKey("palette_family")
        val DARK_MODE_OPTION = stringPreferencesKey("dark_mode_option")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DAILY_SUMMARY_TIME = intPreferencesKey("daily_summary_time")
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val HAS_SEEN_TODAY_COACH_MARKS = booleanPreferencesKey("has_seen_today_coach_marks")
    }

    override val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] ?: false
    }

    override val hasSeenTodayCoachMarks: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_SEEN_TODAY_COACH_MARKS] ?: false
    }

    override val userName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_NAME] ?: ""
    }

    override val userHeadline: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_HEADLINE] ?: "Architect of Daily Flow"
    }

    override val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val modeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.MATCHA_OAT.name
        try {
            ThemeMode.valueOf(modeStr)
        } catch (_: Exception) {
            ThemeMode.MATCHA_OAT
        }
    }

    override val paletteFamily: Flow<PaletteFamily> = context.dataStore.data.map { preferences ->
        val familyStr = preferences[PreferencesKeys.PALETTE_FAMILY] ?: PaletteFamily.MATCHA_OAT.name
        try {
            PaletteFamily.valueOf(familyStr)
        } catch (_: Exception) {
            PaletteFamily.MATCHA_OAT
        }
    }

    override val darkModeOption: Flow<DarkModeOption> = context.dataStore.data.map { preferences ->
        val optStr = preferences[PreferencesKeys.DARK_MODE_OPTION] ?: DarkModeOption.LIGHT.name
        try {
            DarkModeOption.valueOf(optStr)
        } catch (_: Exception) {
            DarkModeOption.LIGHT
        }
    }

    override val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
    }

    override val dailySummaryTimeMinutes: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DAILY_SUMMARY_TIME] ?: 480 // 08:00 AM
    }

    override suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }

    override suspend fun setUserHeadline(headline: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_HEADLINE] = headline
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    override suspend fun setPaletteFamily(family: PaletteFamily) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PALETTE_FAMILY] = family.name
        }
    }

    override suspend fun setDarkModeOption(option: DarkModeOption) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE_OPTION] = option.name
        }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override suspend fun setDailySummaryTimeMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_SUMMARY_TIME] = minutes
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] = completed
        }
    }

    override suspend fun setHasSeenTodayCoachMarks(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_TODAY_COACH_MARKS] = seen
        }
    }
}

