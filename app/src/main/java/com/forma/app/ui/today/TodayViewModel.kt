package com.forma.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forma.app.core.util.DateUtils
import com.forma.app.domain.model.AiGenerationResult
import com.forma.app.domain.model.DaySchedule
import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.TimeOfDay
import com.forma.app.domain.model.TimelineItem
import com.forma.app.domain.model.TodayScheduleItem
import com.forma.app.domain.repository.AiPlanPreset
import com.forma.app.domain.repository.BillingRepository
import com.forma.app.domain.repository.HabitRepository
import com.forma.app.domain.repository.TimelineRepository
import com.forma.app.domain.repository.UserPreferencesRepository
import com.forma.app.domain.usecase.GetTodayTimelineUseCase
import com.forma.app.domain.usecase.PlanDayWithAiUseCase
import com.forma.app.domain.usecase.ToggleHabitCompletionUseCase
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
    private val habitRepository: HabitRepository,
    private val planDayWithAiUseCase: PlanDayWithAiUseCase,
    private val rebalanceTimelineUseCase: com.forma.app.domain.usecase.RebalanceTimelineUseCase,
    private val zenFeedback: com.forma.app.core.audio.ZenFeedbackManager,
    private val billingRepository: BillingRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val dailyReflectionRepository: com.forma.app.domain.repository.DailyReflectionRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(DateUtils.today())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val userName: StateFlow<String> = preferencesRepository.userName
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Alex")

    private val _isAiPlanning = MutableStateFlow(false)
    val isAiPlanning: StateFlow<Boolean> = _isAiPlanning.asStateFlow()

    private val _aiPreviewResult = MutableStateFlow<AiGenerationResult?>(null)
    val aiPreviewResult: StateFlow<AiGenerationResult?> = _aiPreviewResult.asStateFlow()

    private val _eventFlow = MutableSharedFlow<TodayUiEvent>()
    val eventFlow: SharedFlow<TodayUiEvent> = _eventFlow.asSharedFlow()

    val isPro: StateFlow<Boolean> = billingRepository.isPro
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val chronotype: StateFlow<com.forma.app.domain.model.Chronotype> = preferencesRepository.chronotype
        .stateIn(viewModelScope, SharingStarted.Eagerly, com.forma.app.domain.model.Chronotype.BEAR)

    val hasSeenTodayCoachMarks: StateFlow<Boolean> = preferencesRepository.hasSeenTodayCoachMarks
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    @OptIn(ExperimentalCoroutinesApi::class)
    val daySchedule: StateFlow<DaySchedule?> = _selectedDate
        .flatMapLatest { date -> getTodayTimelineUseCase(date) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setChronotype(chronotype: com.forma.app.domain.model.Chronotype) {
        viewModelScope.launch {
            preferencesRepository.setChronotype(chronotype)
        }
    }

    fun dismissCoachMarks() {
        viewModelScope.launch {
            preferencesRepository.setHasSeenTodayCoachMarks(true)
        }
    }

    fun restartCoachMarks() {
        viewModelScope.launch {
            preferencesRepository.setHasSeenTodayCoachMarks(false)
        }
    }

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

    fun skipHabit(habitId: String, habitName: String = "Habit") {
        viewModelScope.launch {
            zenFeedback.onTaskToggled()
            val dateIso = DateUtils.formatDateIso(_selectedDate.value)
            preferencesRepository.skipHabit(dateIso, habitId)
            _eventFlow.emit(TodayUiEvent.ShowToast("$habitName skipped today. Streak preserved."))
        }
    }

    fun unskipHabit(habitId: String) {
        viewModelScope.launch {
            zenFeedback.onTaskToggled()
            val dateIso = DateUtils.formatDateIso(_selectedDate.value)
            preferencesRepository.unskipHabit(dateIso, habitId)
        }
    }

    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.insertHabit(habit)
            zenFeedback.onHabitCompleted()
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

    fun toggleHabitSubtask(habit: Habit, subtaskId: String) {
        viewModelScope.launch {
            val updatedSubtasks = habit.subtasks.map {
                if (it.id == subtaskId) it.copy(completed = !it.completed) else it
            }
            val allDone = updatedSubtasks.isNotEmpty() && updatedSubtasks.all { it.completed }
            if (allDone) {
                zenFeedback.onHabitCompleted()
            } else {
                zenFeedback.onTaskToggled()
            }
            val updatedHabit = habit.copy(subtasks = updatedSubtasks)
            habitRepository.updateHabit(updatedHabit)
        }
    }

    fun toggleHabitPause(habit: Habit) {
        viewModelScope.launch {
            val updated = habit.copy(isWintering = !habit.isWintering)
            habitRepository.updateHabit(updated)
            zenFeedback.onTaskToggled()
        }
    }

    fun clearAiPreview() {
        _aiPreviewResult.value = null
    }

    fun generateAiPlan(
        prompt: String = "",
        preset: AiPlanPreset? = null,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isAiPlanning.value = true
            val dateIso = DateUtils.formatDateIso(_selectedDate.value)
            try {
                val result = planDayWithAiUseCase.generatePreview(
                    date = dateIso,
                    prompt = prompt,
                    preset = preset
                )
                _aiPreviewResult.value = result
                _isAiPlanning.value = false
                zenFeedback.onMilestoneReached()
                onSuccess?.invoke()
            } catch (e: Exception) {
                _isAiPlanning.value = false
                _eventFlow.emit(TodayUiEvent.ShowToast("AI Generation error: ${e.localizedMessage}"))
            }
        }
    }

    fun decomposeGoalWithAi(goal: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isAiPlanning.value = true
            val dateIso = DateUtils.formatDateIso(_selectedDate.value)
            try {
                val result = planDayWithAiUseCase.decomposeGoal(goal, dateIso)
                _aiPreviewResult.value = result
                _isAiPlanning.value = false
                zenFeedback.onMilestoneReached()
                onSuccess?.invoke()
            } catch (e: Exception) {
                _isAiPlanning.value = false
                _eventFlow.emit(TodayUiEvent.ShowToast("Goal decomposition error: ${e.localizedMessage}"))
            }
        }
    }

    fun applyAiPlanBatch(
        tasks: List<TimelineItem>,
        habits: List<Habit>,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val dateIso = DateUtils.formatDateIso(_selectedDate.value)
            tasks.forEach { task ->
                timelineRepository.insertTimelineItem(task.copy(date = dateIso))
            }
            habits.forEach { habit ->
                habitRepository.insertHabit(habit)
            }
            _aiPreviewResult.value = null
            zenFeedback.onMilestoneReached()
            _eventFlow.emit(TodayUiEvent.ShowToast("Added ${tasks.size} tasks & ${habits.size} habits to your flow."))
            onComplete()
        }
    }

    fun addSingleHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.insertHabit(habit)
            zenFeedback.onHabitCompleted()
            _eventFlow.emit(TodayUiEvent.ShowToast("Habit '${habit.name}' added to sanctuary."))
        }
    }

    fun requestAiDayPlan(preset: AiPlanPreset = AiPlanPreset.DEEP_WORK) {
        viewModelScope.launch {
            _isAiPlanning.value = true
            val dateIso = DateUtils.formatDateIso(_selectedDate.value)
            when (val result = planDayWithAiUseCase(dateIso, preset)) {
                is PlanDayWithAiUseCase.Result.Success -> {
                    _isAiPlanning.value = false
                    zenFeedback.onMilestoneReached()
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

    fun saveDailyGratitude(gratitude: String) {
        viewModelScope.launch {
            val dateIso = DateUtils.formatDateIso(_selectedDate.value)
            val existing = dailyReflectionRepository.getReflectionDirect(dateIso)
            val updated = existing?.copy(
                gratitudeNote = gratitude,
                updatedAt = System.currentTimeMillis()
            ) ?: com.forma.app.domain.model.DailyReflection(
                date = dateIso,
                gratitudeNote = gratitude,
                updatedAt = System.currentTimeMillis()
            )
            dailyReflectionRepository.saveReflection(updated)
            _eventFlow.emit(TodayUiEvent.ShowToast("Gratitude note recorded."))
        }
    }

    fun quickAddInlineItem(title: String, isHabit: Boolean = false) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val dateIso = DateUtils.formatDateIso(_selectedDate.value)
            if (isHabit) {
                val newHabit = Habit(
                    name = title.trim(),
                    icon = "target",
                    colorTag = "#4E6542",
                    timeOfDay = TimeOfDay.ANYTIME
                )
                habitRepository.insertHabit(newHabit)
                zenFeedback.onHabitCompleted()
                _eventFlow.emit(TodayUiEvent.ShowToast("Daily ritual '${title.trim()}' created."))
            } else {
                val newItem = TimelineItem(
                    title = title.trim(),
                    date = dateIso,
                    icon = "pin",
                    colorTag = "#2F80ED",
                    completed = false
                )
                timelineRepository.insertTimelineItem(newItem)
                zenFeedback.onTaskToggled()
                _eventFlow.emit(TodayUiEvent.ShowToast("Intention '${title.trim()}' added."))
            }
        }
    }
}
