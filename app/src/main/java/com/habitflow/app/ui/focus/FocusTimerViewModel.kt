package com.habitflow.app.ui.focus

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.core.audio.SoundscapeType
import com.habitflow.app.core.audio.ZenSoundscapeEngine
import com.habitflow.app.core.util.DateUtils
import com.habitflow.app.core.notification.FocusActionBus
import com.habitflow.app.core.notification.FocusActionEvent
import com.habitflow.app.core.notification.FocusNotificationManager
import com.habitflow.app.domain.repository.FocusTrackerRepository
import com.habitflow.app.domain.repository.TimelineRepository
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
import java.net.URLDecoder
import javax.inject.Inject

sealed interface FocusTimerUiEvent {
    object TimerFinished : FocusTimerUiEvent
    data class ShowCelebration(val message: String) : FocusTimerUiEvent
}

@HiltViewModel
class FocusTimerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val focusTrackerRepository: FocusTrackerRepository,
    private val timelineRepository: TimelineRepository,
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase,
    val soundscapeEngine: ZenSoundscapeEngine,
    private val focusNotificationManager: FocusNotificationManager,
    private val focusActionBus: FocusActionBus
) : ViewModel() {

    val itemId: String = savedStateHandle.get<String>("itemId") ?: ""
    private val rawTitle: String = savedStateHandle.get<String>("title") ?: "Mindful Focus"
    val itemTitle: String = try { URLDecoder.decode(rawTitle, "UTF-8") } catch (_: Exception) { rawTitle }
    private val initialDurationMinutes: Int = (savedStateHandle.get<String>("duration")?.toIntOrNull() ?: 25).coerceAtLeast(1)
    val isHabit: Boolean = savedStateHandle.get<String>("isHabit")?.toBoolean() ?: false

    private val _totalDurationSeconds = MutableStateFlow(initialDurationMinutes * 60)
    val totalDurationSeconds: StateFlow<Int> = _totalDurationSeconds.asStateFlow()

    private val _timeRemainingSeconds = MutableStateFlow(initialDurationMinutes * 60)
    val timeRemainingSeconds: StateFlow<Int> = _timeRemainingSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _secondsElapsed = MutableStateFlow(0)
    val secondsElapsed: StateFlow<Int> = _secondsElapsed.asStateFlow()

    private val _selectedSoundscape = MutableStateFlow(SoundscapeType.NONE)
    val selectedSoundscape: StateFlow<SoundscapeType> = _selectedSoundscape.asStateFlow()

    val totalRecordedSecondsOnItem: StateFlow<Int> = focusTrackerRepository.getFocusTimeForTask(itemId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _eventFlow = MutableSharedFlow<FocusTimerUiEvent>()
    val eventFlow: SharedFlow<FocusTimerUiEvent> = _eventFlow.asSharedFlow()

    private var timerJob: Job? = null

    init {
        // Collect actions from lock screen / notification shade controller
        viewModelScope.launch {
            focusActionBus.events.collect { event ->
                when (event) {
                    FocusActionEvent.Pause -> pauseTimer()
                    FocusActionEvent.Resume -> startTimer()
                    FocusActionEvent.Complete -> finishAndSave {}
                }
            }
        }

        // Automatically start focus session gently
        startTimer()
    }

    fun selectSoundscape(type: SoundscapeType) {
        _selectedSoundscape.value = type
        if (_isRunning.value) {
            soundscapeEngine.startSoundscape(type)
        }
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

        if (_selectedSoundscape.value != SoundscapeType.NONE) {
            soundscapeEngine.startSoundscape(_selectedSoundscape.value)
        }

        focusNotificationManager.updateFocusNotification(
            taskTitle = itemTitle,
            remainingSeconds = _timeRemainingSeconds.value,
            totalSeconds = _totalDurationSeconds.value,
            isRunning = true
        )

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isRunning.value && _timeRemainingSeconds.value > 0) {
                delay(1000)
                _timeRemainingSeconds.value -= 1
                _secondsElapsed.value += 1

                focusNotificationManager.updateFocusNotification(
                    taskTitle = itemTitle,
                    remainingSeconds = _timeRemainingSeconds.value,
                    totalSeconds = _totalDurationSeconds.value,
                    isRunning = true
                )

                if (_timeRemainingSeconds.value <= 0) {
                    _isRunning.value = false
                    soundscapeEngine.stopSoundscape()
                    soundscapeEngine.playSingingBowlChime(4.0f)
                    focusNotificationManager.dismissFocusNotification()
                    onTimerCompletedNaturally()
                }
            }
        }
    }

    fun pauseTimer() {
        _isRunning.value = false
        soundscapeEngine.stopSoundscape()
        timerJob?.cancel()

        focusNotificationManager.updateFocusNotification(
            taskTitle = itemTitle,
            remainingSeconds = _timeRemainingSeconds.value,
            totalSeconds = _totalDurationSeconds.value,
            isRunning = false
        )
    }

    fun resetTimer() {
        pauseTimer()
        _timeRemainingSeconds.value = _totalDurationSeconds.value
        focusNotificationManager.dismissFocusNotification()
    }

    fun addFiveMinutes() {
        _totalDurationSeconds.value += 300
        _timeRemainingSeconds.value += 300
        focusNotificationManager.updateFocusNotification(
            taskTitle = itemTitle,
            remainingSeconds = _timeRemainingSeconds.value,
            totalSeconds = _totalDurationSeconds.value,
            isRunning = _isRunning.value
        )
    }

    private suspend fun onTimerCompletedNaturally() {
        focusNotificationManager.dismissFocusNotification()
        recordTimeAndMarkComplete()
        _eventFlow.emit(FocusTimerUiEvent.ShowCelebration("Flow session completed! Time recorded."))
        _eventFlow.emit(FocusTimerUiEvent.TimerFinished)
    }

    fun finishAndSave(onDone: () -> Unit) {
        pauseTimer()
        focusNotificationManager.dismissFocusNotification()
        soundscapeEngine.playSingingBowlChime(3.0f)
        viewModelScope.launch {
            recordTimeAndMarkComplete()
            _eventFlow.emit(FocusTimerUiEvent.ShowCelebration("Focus time saved successfully."))
            onDone()
        }
    }

    private suspend fun recordTimeAndMarkComplete() {
        val seconds = _secondsElapsed.value
        val todayStr = DateUtils.formatDateIso(DateUtils.today())

        // 1. Record focus duration in repository
        focusTrackerRepository.recordFocusSession(
            itemId = itemId,
            itemTitle = itemTitle,
            secondsSpent = seconds,
            isHabit = isHabit,
            date = todayStr
        )

        // 2. Automatically mark task or habit as completed
        if (isHabit) {
            toggleHabitCompletionUseCase(habitId = itemId, date = todayStr, currentlyCompleted = false)
        } else {
            timelineRepository.toggleTimelineItemCompletion(id = itemId, completed = true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        focusNotificationManager.dismissFocusNotification()
        soundscapeEngine.stopSoundscape()
        timerJob?.cancel()
    }
}
