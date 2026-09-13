package com.habitflow.app.ui.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.core.audio.AmbientSound
import com.habitflow.app.core.audio.AmbientSoundManager
import com.habitflow.app.core.util.DateUtils
import com.habitflow.app.domain.model.TodayScheduleItem
import com.habitflow.app.domain.repository.FocusTrackerRepository
import com.habitflow.app.domain.repository.TimelineRepository
import com.habitflow.app.domain.usecase.GetTodayTimelineUseCase
import com.habitflow.app.domain.usecase.ToggleHabitCompletionUseCase
import com.habitflow.app.core.notification.FocusActionBus
import com.habitflow.app.core.notification.FocusActionEvent
import com.habitflow.app.core.notification.FocusNotificationManager
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
    private val timelineRepository: TimelineRepository,
    private val ambientSoundManager: AmbientSoundManager,
    private val focusNotificationManager: FocusNotificationManager,
    private val focusActionBus: FocusActionBus
) : ViewModel() {

    private val _selectedMode = MutableStateFlow(PomodoroMode.FOCUS)
    val selectedMode: StateFlow<PomodoroMode> = _selectedMode.asStateFlow()

    private val _selectedSound = MutableStateFlow(AmbientSound.OFF)
    val selectedSound: StateFlow<AmbientSound> = _selectedSound.asStateFlow()

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

    init {
        viewModelScope.launch {
            focusActionBus.events.collect { event ->
                when (event) {
                    is FocusActionEvent.Pause -> pauseTimer()
                    is FocusActionEvent.Resume -> startTimer()
                    is FocusActionEvent.Complete -> finishAndLog()
                }
            }
        }
    }

    val todaySchedule = getTodayTimelineUseCase(DateUtils.today())
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

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

    fun setAmbientSound(sound: AmbientSound) {
        _selectedSound.value = sound
        if (_isRunning.value) {
            ambientSoundManager.play(sound)
        } else if (sound == AmbientSound.OFF) {
            ambientSoundManager.stop()
        }
    }

    fun startTimer() {
        if (_isRunning.value) return
        _isRunning.value = true

        if (_selectedSound.value != AmbientSound.OFF) {
            ambientSoundManager.play(_selectedSound.value)
        }

        updateNotification()

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isRunning.value && _timeRemainingSeconds.value > 0) {
                delay(1000)
                _timeRemainingSeconds.value -= 1
                if (_selectedMode.value == PomodoroMode.FOCUS) {
                    _secondsElapsedThisSession.value += 1
                }

                // Update notification every second
                updateNotification()

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
        ambientSoundManager.stop()
        if (_timeRemainingSeconds.value < _totalDurationSeconds.value && _timeRemainingSeconds.value > 0) {
            updateNotification()
        } else {
            focusNotificationManager.dismissFocusNotification()
        }
    }

    fun resetTimer() {
        pauseTimer()
        _timeRemainingSeconds.value = _totalDurationSeconds.value
        _secondsElapsedThisSession.value = 0
        focusNotificationManager.dismissFocusNotification()
    }

    fun addFiveMinutes() {
        _totalDurationSeconds.value += 300
        _timeRemainingSeconds.value += 300
        if (_isRunning.value) updateNotification()
    }

    fun subtractFiveMinutes() {
        val newTotal = (_totalDurationSeconds.value - 300).coerceAtLeast(60)
        val newRemaining = (_timeRemainingSeconds.value - 300).coerceAtLeast(1)
        _totalDurationSeconds.value = newTotal
        _timeRemainingSeconds.value = newRemaining
        if (_isRunning.value) updateNotification()
    }

    private fun updateNotification() {
        val title = when (val item = _selectedItem.value) {
            is TodayScheduleItem.HabitItem -> item.habit.name
            is TodayScheduleItem.TimelineBlock -> item.item.title
            null -> _selectedMode.value.title
        }
        focusNotificationManager.updateFocusNotification(
            taskTitle = title,
            remainingSeconds = _timeRemainingSeconds.value,
            totalSeconds = _totalDurationSeconds.value,
            isRunning = _isRunning.value
        )
    }

    private suspend fun onTimerFinished() {
        ambientSoundManager.stop()
        focusNotificationManager.dismissFocusNotification()
        saveAndLogSession()
        _eventFlow.emit(PomodoroUiEvent.ShowToast("Flow session complete! Great focus."))
        _eventFlow.emit(PomodoroUiEvent.SessionFinished)
    }

    fun finishAndLog() {
        pauseTimer()
        focusNotificationManager.dismissFocusNotification()
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
        ambientSoundManager.stop()
        focusNotificationManager.dismissFocusNotification()
    }
}
