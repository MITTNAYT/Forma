package com.habitflow.app.ui.focus

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.core.util.DateUtils
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
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase
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

    val totalRecordedSecondsOnItem: StateFlow<Int> = focusTrackerRepository.getFocusTimeForTask(itemId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _eventFlow = MutableSharedFlow<FocusTimerUiEvent>()
    val eventFlow: SharedFlow<FocusTimerUiEvent> = _eventFlow.asSharedFlow()

    private var timerJob: Job? = null

    init {
        // Automatically start focus session gently
        startTimer()
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
                _secondsElapsed.value += 1

                if (_timeRemainingSeconds.value <= 0) {
                    _isRunning.value = false
                    onTimerCompletedNaturally()
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
    }

    fun addFiveMinutes() {
        _totalDurationSeconds.value += 300
        _timeRemainingSeconds.value += 300
    }

    private suspend fun onTimerCompletedNaturally() {
        recordTimeAndMarkComplete()
        _eventFlow.emit(FocusTimerUiEvent.ShowCelebration("Flow session completed! Time recorded."))
        _eventFlow.emit(FocusTimerUiEvent.TimerFinished)
    }

    fun finishAndSave(onDone: () -> Unit) {
        pauseTimer()
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
        timerJob?.cancel()
    }
}
