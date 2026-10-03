package com.forma.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forma.app.domain.model.EnergyLevel
import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.TimeOfDay
import com.forma.app.domain.repository.HabitRepository
import com.forma.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class OnboardingStep {
    WELCOME_NAME,
    STARTER_HABITS,
    READY
}

data class StarterHabitItem(
    val name: String,
    val subtitle: String,
    val icon: String,
    val colorTag: String,
    val timeOfDay: TimeOfDay,
    val energyLevel: EnergyLevel,
    val cueText: String? = null,
    val reminderTimeMinutes: Int? = null,
    val isSelected: Boolean = true
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _step = MutableStateFlow(OnboardingStep.WELCOME_NAME)
    val step: StateFlow<OnboardingStep> = _step.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _starterHabits = MutableStateFlow(
        listOf(
            StarterHabitItem(
                name = "Morning Hydration",
                subtitle = "500ml glass right after waking up",
                icon = "water",
                colorTag = "#4A90E2",
                timeOfDay = TimeOfDay.MORNING,
                energyLevel = EnergyLevel.LOW,
                cueText = "Immediately after stepping out of bed",
                reminderTimeMinutes = 7 * 60,
                isSelected = true
            ),
            StarterHabitItem(
                name = "15-Minute Daily Walk",
                subtitle = "Natural sunlight & clear thinking",
                icon = "walk",
                colorTag = "#4E6542",
                timeOfDay = TimeOfDay.AFTERNOON,
                energyLevel = EnergyLevel.MEDIUM,
                cueText = "During afternoon break",
                reminderTimeMinutes = 13 * 60 + 30,
                isSelected = true
            ),
            StarterHabitItem(
                name = "Read 10 Pages",
                subtitle = "Distraction-free focus & learning",
                icon = "book",
                colorTag = "#D4AF37",
                timeOfDay = TimeOfDay.EVENING,
                energyLevel = EnergyLevel.LOW,
                cueText = "Before opening social apps",
                reminderTimeMinutes = 20 * 60,
                isSelected = true
            ),
            StarterHabitItem(
                name = "Plan Tomorrow Tonight",
                subtitle = "3 core priorities before sleep",
                icon = "sparkles",
                colorTag = "#5E548E",
                timeOfDay = TimeOfDay.EVENING,
                energyLevel = EnergyLevel.LOW,
                cueText = "Before turning off desk lamp",
                reminderTimeMinutes = 21 * 60 + 30,
                isSelected = false
            )
        )
    )
    val starterHabits: StateFlow<List<StarterHabitItem>> = _starterHabits.asStateFlow()

    fun onNameChange(newName: String) {
        _name.value = newName
    }

    fun submitName() {
        _step.value = OnboardingStep.STARTER_HABITS
    }

    fun goToPreviousStep() {
        _step.value = when (_step.value) {
            OnboardingStep.READY -> OnboardingStep.STARTER_HABITS
            OnboardingStep.STARTER_HABITS -> OnboardingStep.WELCOME_NAME
            OnboardingStep.WELCOME_NAME -> OnboardingStep.WELCOME_NAME
        }
    }

    fun toggleStarterHabit(index: Int) {
        val current = _starterHabits.value.toMutableList()
        if (index in current.indices) {
            val item = current[index]
            current[index] = item.copy(isSelected = !item.isSelected)
            _starterHabits.value = current
        }
    }

    fun submitHabits() {
        _step.value = OnboardingStep.READY
    }

    fun completeOnboarding(onFinish: () -> Unit) {
        viewModelScope.launch {
            val finalName = _name.value.trim().ifBlank { "Friend" }
            preferencesRepository.setUserName(finalName)
            preferencesRepository.setOnboardingCompleted(true)

            // Seed selected starter habits
            val habitsToSeed = _starterHabits.value.filter { it.isSelected }
            habitsToSeed.forEach { starter ->
                val habit = Habit(
                    id = UUID.randomUUID().toString(),
                    name = starter.name,
                    icon = starter.icon,
                    colorTag = starter.colorTag,
                    timeOfDay = starter.timeOfDay,
                    energyLevel = starter.energyLevel,
                    stackedCueText = starter.cueText,
                    reminderTimeMinutes = starter.reminderTimeMinutes,
                    repeatDays = setOf(1, 2, 3, 4, 5, 6, 7),
                    isIndefinite = true
                )
                habitRepository.insertHabit(habit)
            }

            onFinish()
        }
    }
}
