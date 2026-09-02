package com.habitflow.app.ui.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.core.util.DateUtils
import com.habitflow.app.domain.model.TodayScheduleItem
import com.habitflow.app.domain.repository.FocusTrackerRepository
import com.habitflow.app.domain.repository.TimelineRepository
import com.habitflow.app.domain.usecase.GetTodayTimelineUseCase
import com.habitflow.app.domain.usecase.ToggleHabitCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PomodoroMode(val title: String, val defaultMinutes: Int) {
    FOCUS("Focus Flow", 25),
    SHORT_BREAK("Short Rest", 5),
    LONG_BREAK("Long Rest", 15)
}

sealed interface PomodoroUiEvent {
    object SessionFinished : PomodoroUiEvent
    data class ShowToast(val message: String) : PomodoroUiEvent
}

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val focusTrackerRepository: FocusTrackerRepository,
    private val getTodayTimelineUseCase: GetTodayTimelineUseCase,
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase,
    private val timelineRepository: TimelineRepository
) : ViewModel() {

    private val _selectedMode = MutableStateFlow(PomodoroMode.FOCUS)
    val selectedMode: StateFlow<PomodoroMode> = _selectedMode.asStateFlow()

    private val _totalDurationSeconds = MutableStateFlow(25 * 60)
    val totalDurationSeconds: StateFlow<Int> = _totalDurationSeconds.asStateFlow()

    private val _timeRemainingSeconds = MutableStateFlow(25 * 60)
    val timeRemainingSeconds: StateFlow<Int> = _timeRemainingSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _secondsElapsedThisSession = MutableStateFlow(0)
    val secondsElapsedThisSession: StateFlow<Int> = _secondsElapsedThisSession.asStateFlow()

    private val _selectedItem = MutableStateFlow<TodayScheduleItem?>(null)
    val selectedItem: StateFlow<TodayScheduleItem?> = _selectedItem.asStateFlow()

    val todaySchedule = getTodayTimelineUseCase(DateUtils.today())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalFocusSecondsToday: StateFlow<Int> = focusTrackerRepository
        .getTotalFocusTimeForDate(DateUtils.formatDateIso(DateUtils.today()))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _eventFlow = MutableSharedFlow<PomodoroUiEvent>()
    val eventFlow: SharedFlow<PomodoroUiEvent> = _eventFlow.asSharedFlow()

    private var timerJob: Job? = null

    fun selectMode(mode: PomodoroMode) {
        pauseTimer()
        _selectedMode.value = mode
        _totalDurationSeconds.value = mode.defaultMinutes * 60
        _timeRemainingSeconds.value = mode.defaultMinutes * 60
        _secondsElapsedThisSession.value = 0
    }

    fun selectScheduleItem(item: TodayScheduleItem?) {
        _selectedItem.value = item
    }

    fun togglePlayPause() {
        if (_isRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun startTimer() {
        if (_isRunning.value) return
        _isRunning.value = true

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isRunning.value && _timeRemainingSeconds.value > 0) {
                delay(1000)
                _timeRemainingSeconds.value -= 1
                if (_selectedMode.value == PomodoroMode.FOCUS) {
                    _secondsElapsedThisSession.value += 1
                }

                if (_timeRemainingSeconds.value <= 0) {
                    _isRunning.value = false
                    onTimerFinished()
                }
            }
        }
    }

    fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _timeRemainingSeconds.value = _totalDurationSeconds.value
        _secondsElapsedThisSession.value = 0
    }

    fun addFiveMinutes() {
        _totalDurationSeconds.value += 300
        _timeRemainingSeconds.value += 300
    }

    private suspend fun onTimerFinished() {
        saveAndLogSession()
        _eventFlow.emit(PomodoroUiEvent.ShowToast("Flow session complete! Great focus."))
        _eventFlow.emit(PomodoroUiEvent.SessionFinished)
    }

    fun finishAndLog() {
        pauseTimer()
        viewModelScope.launch {
            saveAndLogSession()
            _eventFlow.emit(PomodoroUiEvent.ShowToast("Focus session logged successfully."))
            resetTimer()
        }
    }

    private suspend fun saveAndLogSession() {
        val seconds = _secondsElapsedThisSession.value
        if (seconds <= 0 || _selectedMode.value != PomodoroMode.FOCUS) return

        val todayStr = DateUtils.formatDateIso(DateUtils.today())
        val item = _selectedItem.value

        val itemId = item?.id ?: "general_pomodoro"
        val title = when (item) {
            is TodayScheduleItem.HabitItem -> item.habit.name
            is TodayScheduleItem.TimelineBlock -> item.item.title
            null -> "Mindful Focus"
        }
        val isHabit = item is TodayScheduleItem.HabitItem

        focusTrackerRepository.recordFocusSession(
            itemId = itemId,
            itemTitle = title,
            secondsSpent = seconds,
            isHabit = isHabit,
            date = todayStr
        )

        // If user finished significant duration on an attached task, complete it
        if (item != null && seconds >= 60) {
            if (isHabit && item is TodayScheduleItem.HabitItem) {
                toggleHabitCompletionUseCase(habitId = item.habit.id, date = todayStr, currentlyCompleted = false)
            } else if (item is TodayScheduleItem.TimelineBlock) {
                timelineRepository.toggleTimelineItemCompletion(id = item.item.id, completed = true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
