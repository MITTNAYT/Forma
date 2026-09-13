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
    private val rebalanceTimelineUseCase: com.habitflow.app.domain.usecase.RebalanceTimelineUseCase,
    private val zenFeedback: com.habitflow.app.core.audio.ZenFeedbackManager,
    private val billingRepository: BillingRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(DateUtils.today())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val userName: StateFlow<String> = preferencesRepository.userName
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Alex")

    private val _isAiPlanning = MutableStateFlow(false)
    val isAiPlanning: StateFlow<Boolean> = _isAiPlanning.asStateFlow()

    private val _eventFlow = MutableSharedFlow<TodayUiEvent>()
    val eventFlow: SharedFlow<TodayUiEvent> = _eventFlow.asSharedFlow()

    val isPro: StateFlow<Boolean> = billingRepository.isPro
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val daySchedule: StateFlow<DaySchedule?> = _selectedDate
        .flatMapLatest { date -> getTodayTimelineUseCase(date) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun toggleHabit(item: TodayScheduleItem.HabitItem) {
        viewModelScope.launch {
            val dateIso = DateUtils.formatDateIso(_selectedDate.value)
            val isNowCompleted = !item.isDoneToday
            if (isNowCompleted) {
                zenFeedback.onHabitCompleted()
            } else {
                zenFeedback.onTaskToggled()
            }
            toggleHabitCompletionUseCase(
                habitId = item.habit.id,
                date = dateIso,
                currentlyCompleted = item.isDoneToday
            )
        }
    }

    fun completeHabitById(habitId: String) {
        viewModelScope.launch {
            val dateIso = DateUtils.formatDateIso(_selectedDate.value)
            zenFeedback.onHabitCompleted()
            toggleHabitCompletionUseCase(
                habitId = habitId,
                date = dateIso,
                currentlyCompleted = false
            )
        }
    }

    fun toggleTask(item: TodayScheduleItem.TimelineBlock) {
        viewModelScope.launch {
            val isNowCompleted = !item.item.completed
            if (isNowCompleted) {
                zenFeedback.onHabitCompleted()
            } else {
                zenFeedback.onTaskToggled()
            }
            timelineRepository.toggleTimelineItemCompletion(
                id = item.item.id,
                completed = !item.item.completed
            )
        }
    }

    fun rebalanceDayTimeline() {
        viewModelScope.launch {
            val dateIso = DateUtils.formatDateIso(_selectedDate.value)
            val shifted = rebalanceTimelineUseCase(date = dateIso)
            if (shifted > 0) {
                zenFeedback.onMilestoneReached()
                _eventFlow.emit(TodayUiEvent.ShowToast("Zen Rebalance: Adjusted $shifted blocks with mindful breathing buffers."))
            } else {
                _eventFlow.emit(TodayUiEvent.ShowToast("Schedule is already balanced and peaceful."))
            }
        }
    }

    fun toggleSubtask(item: TimelineItem, subtaskId: String) {
        viewModelScope.launch {
            zenFeedback.onTaskToggled()
            val updatedSubtasks = item.subtasks.map {
                if (it.id == subtaskId) it.copy(completed = !it.completed) else it
            }
            val allDone = updatedSubtasks.isNotEmpty() && updatedSubtasks.all { it.completed }
            timelineRepository.updateTimelineItem(
                item.copy(subtasks = updatedSubtasks, completed = allDone)
            )
        }
    }

    fun requestAiDayPlan(preset: com.habitflow.app.domain.repository.AiPlanPreset = com.habitflow.app.domain.repository.AiPlanPreset.DEEP_WORK) {
        viewModelScope.launch {
            _isAiPlanning.value = true
            val dateIso = DateUtils.formatDateIso(_selectedDate.value)
            when (val result = planDayWithAiUseCase(dateIso, preset)) {
                is PlanDayWithAiUseCase.Result.Success -> {
                    _isAiPlanning.value = false
                    _eventFlow.emit(TodayUiEvent.ShowToast("AI added ${result.generatedItems.size} optimized time blocks for ${preset.title}!"))
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
