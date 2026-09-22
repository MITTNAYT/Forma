package com.forma.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.forma.app.core.util.DateUtils
import com.forma.app.domain.repository.FocusItemSummary
import com.forma.app.domain.repository.FocusTimeStats
import com.forma.app.domain.repository.FocusTrackerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

private val Context.focusDataStore: DataStore<Preferences> by preferencesDataStore(name = "habitflow_focus_sessions")

@Singleton
class FocusTrackerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FocusTrackerRepository {

    override fun getFocusTimeForTask(itemId: String): Flow<Int> {
        val key = intPreferencesKey("focus_item_$itemId")
        return context.focusDataStore.data.map { preferences ->
            preferences[key] ?: 0
        }
    }

    override fun getTotalFocusTimeForDate(date: String): Flow<Int> {
        val key = intPreferencesKey("focus_date_$date")
        return context.focusDataStore.data.map { preferences ->
            preferences[key] ?: 0
        }
    }

    override fun getDailyFocusSecondsForDates(dates: List<String>): Flow<Map<String, Int>> {
        return context.focusDataStore.data.map { preferences ->
            dates.associateWith { date ->
                val key = intPreferencesKey("focus_date_$date")
                preferences[key] ?: 0
            }
        }
    }

    override fun getFocusTimeStats(): Flow<FocusTimeStats> {
        return context.focusDataStore.data.map { preferences ->
            val today = LocalDate.now()
            val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val startOfMonth = today.withDayOfMonth(1)

            // Calculate this week's minutes
            var weekSeconds = 0
            var currentDay = startOfWeek
            while (!currentDay.isAfter(today)) {
                val dateIso = DateUtils.formatDateIso(currentDay)
                val dayKey = intPreferencesKey("focus_date_$dateIso")
                weekSeconds += preferences[dayKey] ?: 0
                currentDay = currentDay.plusDays(1)
            }

            // Calculate this month's minutes
            var monthSeconds = 0
            var monthDay = startOfMonth
            while (!monthDay.isAfter(today)) {
                val dateIso = DateUtils.formatDateIso(monthDay)
                val dayKey = intPreferencesKey("focus_date_$dateIso")
                monthSeconds += preferences[dayKey] ?: 0
                monthDay = monthDay.plusDays(1)
            }

            // Total all-time seconds
            val allTimeTotalSeconds = preferences[intPreferencesKey("focus_all_time_total")] ?: (monthSeconds.coerceAtLeast(weekSeconds))

            // Build top items breakdown
            val knownIdsStr = preferences[stringPreferencesKey("focus_item_ids_list")] ?: ""
            val knownIds = if (knownIdsStr.isBlank()) emptyList() else knownIdsStr.split(",").filter { it.isNotBlank() }

            val itemSummaries = knownIds.mapNotNull { id ->
                val seconds = preferences[intPreferencesKey("focus_item_$id")] ?: 0
                val title = preferences[stringPreferencesKey("focus_title_$id")] ?: "Focus Ritual"
                val isHabit = preferences[booleanPreferencesKey("focus_ishabit_$id")] ?: false

                if (seconds > 0) {
                    FocusItemSummary(
                        itemId = id,
                        title = title,
                        totalMinutes = (seconds / 60).coerceAtLeast(1),
                        isHabit = isHabit
                    )
                } else null
            }.sortedByDescending { it.totalMinutes }

            val finalWeekMins = weekSeconds / 60
            val finalMonthMins = monthSeconds / 60
            val finalAllTimeMins = allTimeTotalSeconds / 60

            FocusTimeStats(
                thisWeekMinutes = finalWeekMins,
                thisMonthMinutes = finalMonthMins,
                allTimeMinutes = finalAllTimeMins,
                topFocusedItems = itemSummaries
            )
        }
    }

    override suspend fun recordFocusSession(
        itemId: String,
        itemTitle: String,
        secondsSpent: Int,
        isHabit: Boolean,
        date: String
    ) {
        if (secondsSpent <= 0) return
        val itemKey = intPreferencesKey("focus_item_$itemId")
        val titleKey = stringPreferencesKey("focus_title_$itemId")
        val isHabitKey = booleanPreferencesKey("focus_ishabit_$itemId")
        val dateKey = intPreferencesKey("focus_date_$date")
        val allTimeKey = intPreferencesKey("focus_all_time_total")
        val idsListKey = stringPreferencesKey("focus_item_ids_list")

        context.focusDataStore.edit { preferences ->
            val currentItemSeconds = preferences[itemKey] ?: 0
            preferences[itemKey] = currentItemSeconds + secondsSpent
            preferences[titleKey] = itemTitle
            preferences[isHabitKey] = isHabit

            val currentDateSeconds = preferences[dateKey] ?: 0
            preferences[dateKey] = currentDateSeconds + secondsSpent

            val currentAllTime = preferences[allTimeKey] ?: 0
            preferences[allTimeKey] = currentAllTime + secondsSpent

            val currentIds = preferences[idsListKey]?.split(",")?.filter { it.isNotBlank() }?.toMutableSet() ?: mutableSetOf()
            currentIds.add(itemId)
            preferences[idsListKey] = currentIds.joinToString(",")
        }
    }
}
