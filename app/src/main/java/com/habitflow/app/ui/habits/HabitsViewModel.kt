package com.habitflow.app.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.core.notification.NotificationHelper
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.HabitStreakInfo
import com.habitflow.app.domain.model.TimeOfDay
import com.habitflow.app.domain.repository.HabitRepository
import com.habitflow.app.domain.usecase.CalculateStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitsUiState(
    val activeHabits: List<Habit> = emptyList(),
    val archivedHabits: List<Habit> = emptyList(),
    val streaksMap: Map<String, HabitStreakInfo> = emptyMap(),
    val selectedTimeOfDayFilter: TimeOfDay? = null,
    val showArchived: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val calculateStreakUseCase: CalculateStreakUseCase,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _selectedTimeOfDayFilter = MutableStateFlow<TimeOfDay?>(null)
    val selectedTimeOfDayFilter: StateFlow<TimeOfDay?> = _selectedTimeOfDayFilter.asStateFlow()

    private val _showArchived = MutableStateFlow(false)
    val showArchived: StateFlow<Boolean> = _showArchived.asStateFlow()

    val uiState: StateFlow<HabitsUiState> = combine(
        habitRepository.getAllHabits(includeArchived = true),
        habitRepository.getAllCompletions(),
        _selectedTimeOfDayFilter,
        _showArchived
    ) { allHabits, allCompletions, filter, showArchived ->

        val completionsByHabit = allCompletions.groupBy { it.habitId }
        val streaks = allHabits.associate { habit ->
            val completions = completionsByHabit[habit.id] ?: emptyList()
            habit.id to calculateStreakUseCase(habit, completions)
        }

        val active = allHabits.filter { !it.archived }
            .filter { filter == null || it.timeOfDay == filter }

        val archived = allHabits.filter { it.archived }

        HabitsUiState(
            activeHabits = active,
            archivedHabits = archived,
            streaksMap = streaks,
            selectedTimeOfDayFilter = filter,
            showArchived = showArchived,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HabitsUiState(isLoading = true))

    fun setTimeOfDayFilter(filter: TimeOfDay?) {
        _selectedTimeOfDayFilter.value = filter
    }

    fun setShowArchived(show: Boolean) {
        _showArchived.value = show
    }

    fun saveHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.insertHabit(habit)
            habit.reminderTimeMinutes?.let { minutes ->
                notificationHelper.scheduleHabitAlarm(
                    habitId = habit.id,
                    habitName = habit.name,
                    habitIcon = habit.icon,
                    minutesFromMidnight = minutes
                )
            }
        }
    }

    fun archiveHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.archiveHabit(habit.id, !habit.archived)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
        }
    }
}
