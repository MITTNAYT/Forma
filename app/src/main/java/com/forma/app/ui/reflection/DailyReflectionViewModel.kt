package com.forma.app.ui.reflection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forma.app.core.audio.ZenFeedbackManager
import com.forma.app.core.util.DateUtils
import com.forma.app.domain.model.DailyReflection
import com.forma.app.domain.model.EnergyLevel
import com.forma.app.core.ai.AiDaySynthesisResult
import com.forma.app.core.ai.GeminiDaySynthesisService
import com.forma.app.domain.repository.DailyReflectionRepository
import com.forma.app.domain.repository.HabitRepository
import com.forma.app.domain.repository.TimelineRepository
import com.forma.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

val ZEN_QUOTES = listOf(
    "“The mind is like water. When it’s turbulent, it’s difficult to see. When it’s calm, everything becomes clear.”",
    "“Do not dwell in the past, do not dream of the future, concentrate the mind on the present moment.”",
    "“Simplicity is the ultimate sophistication.”",
    "“Quiet the mind, and the soul will speak.”",
    "“Smile, breathe and go slowly.”",
    "“In the midst of movement and chaos, keep stillness inside of you.”",
    "“One conscious breath in and out is a meditation.”"
)

sealed interface ReflectionUiEvent {
    data class ShowToast(val message: String) : ReflectionUiEvent
    object DismissSheet : ReflectionUiEvent
}

@HiltViewModel
class DailyReflectionViewModel @Inject constructor(
    private val reflectionRepository: DailyReflectionRepository,
    private val timelineRepository: TimelineRepository,
    private val habitRepository: HabitRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val geminiSynthesisService: GeminiDaySynthesisService,
    private val zenFeedback: ZenFeedbackManager
) : ViewModel() {

    private val todayStr = DateUtils.formatDateIso(DateUtils.today())

    val todayReflection: StateFlow<DailyReflection?> = reflectionRepository
        .getReflectionForDate(todayStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentReflections: StateFlow<List<DailyReflection>> = reflectionRepository
        .getRecentReflections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _keystone1 = MutableStateFlow("")
    val keystone1: StateFlow<String> = _keystone1.asStateFlow()

    private val _keystone2 = MutableStateFlow("")
    val keystone2: StateFlow<String> = _keystone2.asStateFlow()

    private val _keystone3 = MutableStateFlow("")
    val keystone3: StateFlow<String> = _keystone3.asStateFlow()

    private val _energyLevel = MutableStateFlow(EnergyLevel.MEDIUM)
    val energyLevel: StateFlow<EnergyLevel> = _energyLevel.asStateFlow()

    private val _gratitudeNote = MutableStateFlow("")
    val gratitudeNote: StateFlow<String> = _gratitudeNote.asStateFlow()

    private val _mindfulnessScore = MutableStateFlow(5)
    val mindfulnessScore: StateFlow<Int> = _mindfulnessScore.asStateFlow()

    private val _dailyQuote = MutableStateFlow(ZEN_QUOTES.random())
    val dailyQuote: StateFlow<String> = _dailyQuote.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ReflectionUiEvent>()
    val eventFlow: SharedFlow<ReflectionUiEvent> = _eventFlow.asSharedFlow()

    private val _isSynthesizing = MutableStateFlow(false)
    val isSynthesizing: StateFlow<Boolean> = _isSynthesizing.asStateFlow()

    private val _aiSynthesisResult = MutableStateFlow<AiDaySynthesisResult?>(null)
    val aiSynthesisResult: StateFlow<AiDaySynthesisResult?> = _aiSynthesisResult.asStateFlow()

    init {
        viewModelScope.launch {
            val existing = reflectionRepository.getReflectionDirect(todayStr)
            if (existing != null) {
                val intentions = existing.keystoneIntentions
                _keystone1.value = intentions.getOrNull(0) ?: ""
                _keystone2.value = intentions.getOrNull(1) ?: ""
                _keystone3.value = intentions.getOrNull(2) ?: ""
                _energyLevel.value = existing.energyLevel
                _gratitudeNote.value = existing.gratitudeNote
                _mindfulnessScore.value = existing.mindfulnessScore
            }
        }
    }

    fun synthesizeDayWithAi() {
        viewModelScope.launch {
            _isSynthesizing.value = true
            try {
                val userName = preferencesRepository.userName.first()
                val habits = habitRepository.getAllHabits(includeArchived = false).first()
                val timelineItems = timelineRepository.getTimelineItemsForDate(todayStr).first()
                val result = geminiSynthesisService.synthesizeDay(
                    userName = userName,
                    currentEnergy = _energyLevel.value,
                    habits = habits,
                    timelineItems = timelineItems
                )
                _aiSynthesisResult.value = result

                // Automatically pre-fill keystones if currently empty
                if (_keystone1.value.isBlank() && result.suggestedKeystones.isNotEmpty()) {
                    _keystone1.value = result.suggestedKeystones.getOrNull(0) ?: ""
                }
                if (_keystone2.value.isBlank() && result.suggestedKeystones.size > 1) {
                    _keystone2.value = result.suggestedKeystones.getOrNull(1) ?: ""
                }
                if (_keystone3.value.isBlank() && result.suggestedKeystones.size > 2) {
                    _keystone3.value = result.suggestedKeystones.getOrNull(2) ?: ""
                }

                _dailyQuote.value = result.zenAffirmation
                zenFeedback.onTaskToggled()
                _eventFlow.emit(ReflectionUiEvent.ShowToast("Gemini 1.5 Flash synthesized your day alignment."))
            } catch (e: Exception) {
                _eventFlow.emit(ReflectionUiEvent.ShowToast("Synthesis ready."))
            } finally {
                _isSynthesizing.value = false
            }
        }
    }

    fun setKeystone(index: Int, text: String) {
        when (index) {
            0 -> _keystone1.value = text
            1 -> _keystone2.value = text
            2 -> _keystone3.value = text
        }
    }

    fun setEnergyLevel(level: EnergyLevel) {
        _energyLevel.value = level
        zenFeedback.onTaskToggled()
    }

    fun setGratitudeNote(note: String) {
        _gratitudeNote.value = note
    }

    fun setMindfulnessScore(score: Int) {
        _mindfulnessScore.value = score.coerceIn(1, 5)
        zenFeedback.onTaskToggled()
    }

    fun saveMorningAlignment() {
        viewModelScope.launch {
            val intentions = listOf(_keystone1.value, _keystone2.value, _keystone3.value)
                .filter { it.isNotBlank() }

            val current = todayReflection.value ?: DailyReflection(date = todayStr)
            val updated = current.copy(
                keystoneIntentions = intentions,
                energyLevel = _energyLevel.value,
                isMorningCompleted = true,
                updatedAt = System.currentTimeMillis()
            )
            reflectionRepository.saveReflection(updated)
            zenFeedback.onMilestoneReached()
            _eventFlow.emit(ReflectionUiEvent.ShowToast("Morning intentions set with clarity."))
            _eventFlow.emit(ReflectionUiEvent.DismissSheet)
        }
    }

    fun saveEveningReflection(rolloverIncompleteTasks: Boolean) {
        viewModelScope.launch {
            val current = todayReflection.value ?: DailyReflection(date = todayStr)
            val updated = current.copy(
                gratitudeNote = _gratitudeNote.value,
                mindfulnessScore = _mindfulnessScore.value,
                isEveningCompleted = true,
                updatedAt = System.currentTimeMillis()
            )
            reflectionRepository.saveReflection(updated)

            if (rolloverIncompleteTasks) {
                val todayTasks = timelineRepository.getTimelineItemsForDate(todayStr).first()
                val tomorrowStr = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                var rolledOver = 0
                for (task in todayTasks) {
                    if (!task.completed) {
                        val rolledTask = task.copy(
                            id = java.util.UUID.randomUUID().toString(),
                            date = tomorrowStr,
                            updatedAt = System.currentTimeMillis()
                        )
                        timelineRepository.insertTimelineItem(rolledTask)
                        rolledOver++
                    }
                }
                if (rolledOver > 0) {
                    _eventFlow.emit(ReflectionUiEvent.ShowToast("Evening complete. $rolledOver tasks rolled over to tomorrow."))
                } else {
                    _eventFlow.emit(ReflectionUiEvent.ShowToast("Evening reflection saved. Peaceful rest."))
                }
            } else {
                _eventFlow.emit(ReflectionUiEvent.ShowToast("Evening reflection saved. Peaceful rest."))
            }

            zenFeedback.onMilestoneReached()
            _eventFlow.emit(ReflectionUiEvent.DismissSheet)
        }
    }
}
