package com.habitflow.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.core.util.DateUtils
import com.habitflow.app.domain.model.DaySchedule
import com.habitflow.app.domain.model.TimelineItem
import com.habitflow.app.domain.model.TodayScheduleItem
import com.habitflow.app.domain.repository.BillingRepository
import com.habitflow.app.domain.repository.TimelineRepository
import com.habitflow.app.domain.repository.UserPreferencesRepository
import com.habitflow.app.domain.usecase.GetTodayTimelineUseCase
import com.habitflow.app.domain.usecase.PlanDayWithAiUseCase
import com.habitflow.app.domain.usecase.ToggleHabitCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface TodayUiEvent {
    object ShowProPaywall : TodayUiEvent
    data class ShowToast(val message: String) : TodayUiEvent
}

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getTodayTimelineUseCase: GetTodayTimelineUseCase,
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase,
    private val timelineRepository: TimelineRepository,
    private val planDayWithAiUseCase: PlanDayWithAiUseCase,
    private val billingRepository: BillingRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(DateUtils.today())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val userName: StateFlow<String> = preferencesRepository.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Mojammel")

    private val _isAiPlanning = MutableStateFlow(false)
    val isAiPlanning: StateFlow<Boolean> = _isAiPlanning.asStateFlow()

    private val _eventFlow = MutableSharedFlow<TodayUiEvent>()
    val eventFlow: SharedFlow<TodayUiEvent> = _eventFlow.asSharedFlow()

    val isPro: StateFlow<Boolean> = billingRepository.isPro
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val daySchedule: StateFlow<DaySchedule?> = _selectedDate
        .flatMapLatest { date -> getTodayTimelineUseCase(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun toggleHabit(item: TodayScheduleItem.HabitItem) {
        viewModelScope.launch {
            val dateIso = DateUtils.formatDateIso(_selectedDate.value)
            toggleHabitCompletionUseCase(
                habitId = item.habit.id,
                date = dateIso,
                currentlyCompleted = item.isDoneToday
            )
        }
    }

    fun toggleTask(item: TodayScheduleItem.TimelineBlock) {
        viewModelScope.launch {
            timelineRepository.toggleTimelineItemCompletion(
                id = item.item.id,
                completed = !item.item.completed
            )
        }
    }

    fun toggleSubtask(item: TimelineItem, subtaskId: String) {
        viewModelScope.launch {
            val updatedSubtasks = item.subtasks.map {
                if (it.id == subtaskId) it.copy(completed = !it.completed) else it
            }
            val allDone = updatedSubtasks.isNotEmpty() && updatedSubtasks.all { it.completed }
            timelineRepository.updateTimelineItem(
                item.copy(subtasks = updatedSubtasks, completed = allDone)
            )
        }
    }

    fun requestAiDayPlan() {
        viewModelScope.launch {
            _isAiPlanning.value = true
            val dateIso = DateUtils.formatDateIso(_selectedDate.value)
            when (val result = planDayWithAiUseCase(dateIso)) {
                is PlanDayWithAiUseCase.Result.Success -> {
                    _isAiPlanning.value = false
                    _eventFlow.emit(TodayUiEvent.ShowToast("AI added ${result.generatedItems.size} optimized time blocks!"))
                }
                is PlanDayWithAiUseCase.Result.RequiresPro -> {
                    _isAiPlanning.value = false
                    _eventFlow.emit(TodayUiEvent.ShowProPaywall)
                }
                is PlanDayWithAiUseCase.Result.Error -> {
                    _isAiPlanning.value = false
                    _eventFlow.emit(TodayUiEvent.ShowToast("Planning error: ${result.message}"))
                }
            }
        }
    }
}
