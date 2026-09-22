package com.forma.app.ui.timeline

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forma.app.core.notification.NotificationHelper
import com.forma.app.core.util.DateUtils
import com.forma.app.domain.model.EnergyLevel
import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.Subtask
import com.forma.app.domain.model.TimeOfDay
import com.forma.app.domain.model.TimelineItem
import com.forma.app.domain.repository.HabitRepository
import com.forma.app.domain.repository.TimelineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

enum class CreationType {
    TASK,
    HABIT
}

data class AddEditTimelineUiState(
    val id: String = UUID.randomUUID().toString(),
    val creationType: CreationType = CreationType.HABIT,
    val title: String = "",
    val date: String = DateUtils.formatDateIso(DateUtils.today()),
    val startDate: String = DateUtils.formatDateIso(DateUtils.today()),
    val endDate: String? = null,
    val isIndefinite: Boolean = true,
    val hasTime: Boolean = true,
    val startTime: String = "09:15",
    val durationMinutes: Int = 30,
    val endTime: String = "09:45",
    val icon: String = "target",
    val colorTag: String? = null,
    val notes: String = "",
    val subtasks: List<Subtask> = emptyList(),
    val timeOfDay: TimeOfDay = TimeOfDay.MORNING,
    val energyLevel: EnergyLevel = EnergyLevel.MEDIUM,
    val recurrenceType: String = "DAILY", // DAILY, ONCE, WEEKDAYS, CUSTOM
    val repeatDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
    val stackedCueText: String = "",
    val isWintering: Boolean = false,
    val hasReminder: Boolean = true,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val alerts: List<String> = listOf("At start of task", "15m before start"),
    val reminderMinutesBefore: Int? = 15,
    val completed: Boolean = false,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface AddEditTimelineEvent {
    object Saved : AddEditTimelineEvent
    object Deleted : AddEditTimelineEvent
}

@HiltViewModel
class AddEditTimelineViewModel @Inject constructor(
    private val timelineRepository: TimelineRepository,
    private val habitRepository: HabitRepository,
    private val notificationHelper: NotificationHelper,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditTimelineUiState())
    val uiState: StateFlow<AddEditTimelineUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddEditTimelineEvent>()
    val eventFlow: SharedFlow<AddEditTimelineEvent> = _eventFlow.asSharedFlow()

    init {
        val itemId: String? = savedStateHandle["itemId"]
        val selectedDate: String? = savedStateHandle["selectedDate"]
        val isInbox: Boolean = savedStateHandle["isInbox"] ?: false

        if (isInbox) {
            _uiState.value = _uiState.value.copy(hasTime = false)
        }

        if (!selectedDate.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(date = selectedDate)
        }

        if (!itemId.isNullOrBlank()) {
            loadItem(itemId)
        }
    }

    fun initialize(itemId: String?, selectedDate: String?) {
        if (!itemId.isNullOrBlank()) {
            loadItem(itemId)
        } else {
            val dateToUse = selectedDate ?: DateUtils.formatDateIso(DateUtils.today())
            _uiState.value = AddEditTimelineUiState(
                date = dateToUse,
                startDate = dateToUse
            )
        }
    }

    private fun loadItem(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val timelineItem = timelineRepository.getTimelineItemById(id).first()
            if (timelineItem != null) {
                val start = timelineItem.startTime ?: "09:15"
                val end = timelineItem.endTime ?: "09:45"
                val dur = calculateDuration(start, end)

                _uiState.value = AddEditTimelineUiState(
                    id = timelineItem.id,
                    creationType = if (timelineItem.isRecurring) CreationType.HABIT else CreationType.TASK,
                    title = timelineItem.title,
                    date = timelineItem.date,
                    hasTime = timelineItem.startTime != null,
                    startTime = start,
                    durationMinutes = dur,
                    endTime = end,
                    icon = timelineItem.icon,
                    colorTag = timelineItem.colorTag,
                    notes = timelineItem.notes,
                    subtasks = timelineItem.subtasks,
                    recurrenceType = if (timelineItem.isRecurring) "DAILY" else "ONCE",
                    repeatDays = timelineItem.repeatDays,
                    alerts = if (timelineItem.reminderMinutesBefore != null) listOf("At start of task", "${timelineItem.reminderMinutesBefore}m before start") else listOf("At start of task"),
                    reminderMinutesBefore = timelineItem.reminderMinutesBefore,
                    completed = timelineItem.completed,
                    isEditMode = true,
                    isLoading = false
                )
            } else {
                val habit = habitRepository.getHabitById(id).first()
                if (habit != null) {
                    val remTime = habit.reminderTimeMinutes
                    _uiState.value = AddEditTimelineUiState(
                        id = habit.id,
                        creationType = CreationType.HABIT,
                        title = habit.name,
                        icon = habit.icon,
                        colorTag = habit.colorTag,
                        timeOfDay = habit.timeOfDay,
                        energyLevel = habit.energyLevel,
                        repeatDays = habit.repeatDays,
                        startDate = habit.startDate ?: DateUtils.formatDateIso(DateUtils.today()),
                        endDate = habit.endDate,
                        isIndefinite = habit.isIndefinite,
                        stackedCueText = habit.stackedCueText ?: "",
                        isWintering = habit.isWintering,
                        hasReminder = remTime != null,
                        reminderHour = (remTime ?: 480) / 60,
                        reminderMinute = (remTime ?: 480) % 60,
                        recurrenceType = "DAILY",
                        isEditMode = true,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setCreationType(type: CreationType) {
        _uiState.value = _uiState.value.copy(
            creationType = type,
            recurrenceType = if (type == CreationType.HABIT) "DAILY" else "ONCE"
        )
    }

    fun setTitle(title: String) { _uiState.value = _uiState.value.copy(title = title) }

    fun setStartDate(date: String) { _uiState.value = _uiState.value.copy(startDate = date) }
    fun setEndDate(date: String?) { _uiState.value = _uiState.value.copy(endDate = date) }
    fun setIsIndefinite(indefinite: Boolean) { _uiState.value = _uiState.value.copy(isIndefinite = indefinite) }

    fun setEnergyLevel(energy: EnergyLevel) { _uiState.value = _uiState.value.copy(energyLevel = energy) }
    fun setStackedCueText(cue: String) { _uiState.value = _uiState.value.copy(stackedCueText = cue) }
    fun setIsWintering(wintering: Boolean) { _uiState.value = _uiState.value.copy(isWintering = wintering) }

    fun setHasReminder(enabled: Boolean) { _uiState.value = _uiState.value.copy(hasReminder = enabled) }
    fun setReminderTime(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(reminderHour = hour, reminderMinute = minute)
    }

    fun applySmartParse(parsed: com.forma.app.core.util.ParsedTaskResult) {
        val current = _uiState.value
        val hasTime = parsed.startTime != null
        val start = parsed.startTime ?: current.startTime
        val end = parsed.endTime ?: current.endTime
        val dur = parsed.isEstimatedDurationMinutes ?: calculateDuration(start, end)
        val dateStr = parsed.date?.let { DateUtils.formatDateIso(it) } ?: current.date

        _uiState.value = current.copy(
            title = parsed.cleanTitle,
            date = dateStr,
            hasTime = hasTime || current.hasTime,
            startTime = start,
            endTime = end,
            durationMinutes = dur
        )
    }

    fun setDate(date: String) { _uiState.value = _uiState.value.copy(date = date) }
    fun setHasTime(hasTime: Boolean) { _uiState.value = _uiState.value.copy(hasTime = hasTime) }

    fun setStartTime(startTime: String) {
        val calculatedEnd = calculateEndTime(startTime, _uiState.value.durationMinutes)
        _uiState.value = _uiState.value.copy(startTime = startTime, endTime = calculatedEnd)
    }

    fun setDuration(minutes: Int) {
        val calculatedEnd = calculateEndTime(_uiState.value.startTime, minutes)
        _uiState.value = _uiState.value.copy(durationMinutes = minutes, endTime = calculatedEnd)
    }

    fun setEndTime(endTime: String) {
        val dur = calculateDuration(_uiState.value.startTime, endTime)
        _uiState.value = _uiState.value.copy(endTime = endTime, durationMinutes = dur)
    }

    fun setIcon(icon: String) { _uiState.value = _uiState.value.copy(icon = icon) }
    fun setColorTag(colorTag: String?) { _uiState.value = _uiState.value.copy(colorTag = colorTag?.ifBlank { null }) }
    fun setNotes(notes: String) { _uiState.value = _uiState.value.copy(notes = notes) }
    fun setTimeOfDay(timeOfDay: TimeOfDay) { _uiState.value = _uiState.value.copy(timeOfDay = timeOfDay) }

    fun setRecurrenceType(type: String) {
        _uiState.value = _uiState.value.copy(
            recurrenceType = type,
            repeatDays = if (type == "DAILY") setOf(1, 2, 3, 4, 5, 6, 7) else setOf(1, 2, 3, 4, 5)
        )
    }

    fun toggleRepeatDay(dayOfWeek: Int) {
        val current = _uiState.value.repeatDays.toMutableSet()
        if (current.contains(dayOfWeek)) {
            if (current.size > 1) current.remove(dayOfWeek)
        } else {
            current.add(dayOfWeek)
        }
        _uiState.value = _uiState.value.copy(repeatDays = current)
    }

    fun addAlert(alertText: String) {
        val current = _uiState.value.alerts
        if (!current.contains(alertText)) {
            _uiState.value = _uiState.value.copy(alerts = current + alertText)
        }
    }

    fun removeAlert(alertText: String) {
        _uiState.value = _uiState.value.copy(alerts = _uiState.value.alerts.filter { it != alertText })
    }

    fun addSubtask(title: String) {
        if (title.isBlank()) return
        val current = _uiState.value.subtasks
        _uiState.value = _uiState.value.copy(
            subtasks = current + Subtask(title = title.trim(), completed = false)
        )
    }

    fun toggleSubtask(id: String) {
        val current = _uiState.value.subtasks
        _uiState.value = _uiState.value.copy(
            subtasks = current.map { if (it.id == id) it.copy(completed = !it.completed) else it }
        )
    }

    fun removeSubtask(id: String) {
        val current = _uiState.value.subtasks
        _uiState.value = _uiState.value.copy(
            subtasks = current.filter { it.id != id }
        )
    }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) return

        viewModelScope.launch {
            if (state.creationType == CreationType.HABIT) {
                // Save as recurring habit with complete attributes
                val remMinutes = if (state.hasReminder) {
                    state.reminderHour * 60 + state.reminderMinute
                } else null

                val habit = Habit(
                    id = state.id,
                    name = state.title.trim(),
                    icon = state.icon,
                    colorTag = state.colorTag ?: "",
                    timeOfDay = state.timeOfDay,
                    energyLevel = state.energyLevel,
                    repeatDays = state.repeatDays,
                    startDate = state.startDate,
                    endDate = if (state.isIndefinite) null else state.endDate,
                    isIndefinite = state.isIndefinite,
                    reminderTimeMinutes = remMinutes,
                    stackedCueText = state.stackedCueText.trim().ifEmpty { null },
                    isWintering = state.isWintering,
                    updatedAt = System.currentTimeMillis()
                )
                if (state.isEditMode) {
                    habitRepository.updateHabit(habit)
                } else {
                    habitRepository.insertHabit(habit)
                }
            } else {
                // Save as one-time or recurring timeline commitment
                val isRecurring = state.recurrenceType != "ONCE"
                val item = TimelineItem(
                    id = state.id,
                    title = state.title.trim(),
                    date = state.date,
                    startTime = if (state.hasTime) state.startTime else null,
                    endTime = if (state.hasTime) state.endTime else null,
                    icon = state.icon,
                    colorTag = state.colorTag ?: "",
                    notes = state.notes.trim(),
                    subtasks = state.subtasks,
                    isRecurring = isRecurring,
                    repeatDays = if (isRecurring) state.repeatDays else emptySet(),
                    reminderMinutesBefore = state.reminderMinutesBefore,
                    completed = state.completed,
                    updatedAt = System.currentTimeMillis()
                )

                if (state.isEditMode) {
                    timelineRepository.updateTimelineItem(item)
                } else {
                    timelineRepository.insertTimelineItem(item)
                }
            }

            _eventFlow.emit(AddEditTimelineEvent.Saved)
        }
    }

    fun delete() {
        val state = _uiState.value
        viewModelScope.launch {
            if (state.creationType == CreationType.HABIT) {
                val habit = habitRepository.getHabitById(state.id).first()
                if (habit != null) {
                    habitRepository.deleteHabit(habit)
                }
            } else {
                val item = timelineRepository.getTimelineItemById(state.id).first()
                if (item != null) {
                    timelineRepository.deleteTimelineItem(item)
                }
            }
            _eventFlow.emit(AddEditTimelineEvent.Deleted)
        }
    }

    private fun calculateEndTime(startTime: String, durationMinutes: Int): String {
        return try {
            val parsed = LocalTime.parse(startTime)
            val end = parsed.plusMinutes(durationMinutes.toLong())
            end.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (_: Exception) {
            "09:45"
        }
    }

    private fun calculateDuration(startTime: String, endTime: String): Int {
        return try {
            val s = LocalTime.parse(startTime)
            val e = LocalTime.parse(endTime)
            val dur = java.time.temporal.ChronoUnit.MINUTES.between(s, e).toInt()
            if (dur > 0) dur else 30
        } catch (_: Exception) {
            30
        }
    }
}
