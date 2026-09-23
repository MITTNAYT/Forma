package com.forma.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forma.app.domain.model.Chronotype
import com.forma.app.domain.model.EnergyLevel
import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.TimeOfDay
import com.forma.app.domain.repository.HabitRepository
import com.forma.app.domain.repository.PaletteFamily
import com.forma.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class OnboardingStep {
    WELCOME,
    ENTER_NAME,
    CHOOSE_CHRONOTYPE,
    CHOOSE_THEME,
    CHOOSE_STARTER_PACK,
    GREETING
}

data class StarterHabitItem(
    val name: String,
    val icon: String,
    val colorTag: String,
    val timeOfDay: TimeOfDay,
    val energyLevel: EnergyLevel,
    val cueText: String? = null,
    val isSelected: Boolean = true
)

enum class StarterPackType(val title: String, val subtitle: String, val iconName: String) {
    MINDFUL_LIVING("Mindful Living", "Calm mornings, hydration, and peaceful evening rest", "spa"),
    DEEP_WORK("Deep Work & Focus", "Unbroken focus blocks, coding cadence, and walks", "terminal"),
    HEALTH_VITALITY("Health & Vitality", "Movement, hydration tracking, and restorative sleep", "fitness_center")
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _step = MutableStateFlow(OnboardingStep.WELCOME)
    val step: StateFlow<OnboardingStep> = _step.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _selectedChronotype = MutableStateFlow(Chronotype.BIMODAL_NOCTURNAL)
    val selectedChronotype: StateFlow<Chronotype> = _selectedChronotype.asStateFlow()

    private val _selectedPack = MutableStateFlow(StarterPackType.MINDFUL_LIVING)
    val selectedPack: StateFlow<StarterPackType> = _selectedPack.asStateFlow()

    private val _starterHabits = MutableStateFlow<List<StarterHabitItem>>(emptyList())
    val starterHabits: StateFlow<List<StarterHabitItem>> = _starterHabits.asStateFlow()

    init {
        loadStarterHabitsForPack(StarterPackType.MINDFUL_LIVING)
    }

    fun onNameChange(newName: String) {
        _name.value = newName
    }

    fun goToNameStep() {
        _step.value = OnboardingStep.ENTER_NAME
    }

    fun submitName() {
        if (_name.value.isNotBlank()) {
            _step.value = OnboardingStep.CHOOSE_CHRONOTYPE
        }
    }

    fun selectChronotype(chronotype: Chronotype) {
        _selectedChronotype.value = chronotype
        viewModelScope.launch {
            preferencesRepository.setChronotype(chronotype)
        }
    }

    fun submitChronotype() {
        _step.value = OnboardingStep.CHOOSE_THEME
    }

    fun selectTheme(palette: PaletteFamily) {
        viewModelScope.launch {
            preferencesRepository.setPaletteFamily(palette)
            _step.value = OnboardingStep.CHOOSE_STARTER_PACK
        }
    }

    fun selectStarterPack(pack: StarterPackType) {
        _selectedPack.value = pack
        loadStarterHabitsForPack(pack)
    }

    private fun loadStarterHabitsForPack(pack: StarterPackType) {
        _starterHabits.value = when (pack) {
            StarterPackType.MINDFUL_LIVING -> listOf(
                StarterHabitItem("Morning Sunlight & Breath", "wb_sunny", "#4E6542", TimeOfDay.MORNING, EnergyLevel.HIGH, "After I wake up"),
                StarterHabitItem("Deep Hydration & Tea", "local_cafe", "#3F5E78", TimeOfDay.MORNING, EnergyLevel.MEDIUM, "After morning sunlight"),
                StarterHabitItem("Evening Peace Reflection", "spa", "#785848", TimeOfDay.EVENING, EnergyLevel.LOW, "Before going to sleep")
            )
            StarterPackType.DEEP_WORK -> listOf(
                StarterHabitItem("90-Minute Focus Block", "terminal", "#4E6542", TimeOfDay.MORNING, EnergyLevel.HIGH, "After planning my day"),
                StarterHabitItem("Code Review & Refactor", "code", "#3F5E78", TimeOfDay.AFTERNOON, EnergyLevel.MEDIUM, "After lunch"),
                StarterHabitItem("Afternoon Walk & Recharge", "directions_walk", "#785848", TimeOfDay.AFTERNOON, EnergyLevel.LOW, "After focus block")
            )
            StarterPackType.HEALTH_VITALITY -> listOf(
                StarterHabitItem("Morning Mobility & Stretch", "fitness_center", "#4E6542", TimeOfDay.MORNING, EnergyLevel.HIGH, "After getting out of bed"),
                StarterHabitItem("Drink 2L Fresh Water", "water_drop", "#3F5E78", TimeOfDay.ANYTIME, EnergyLevel.MEDIUM, "Throughout the day"),
                StarterHabitItem("8 Hours Restful Sleep", "bedtime", "#785848", TimeOfDay.EVENING, EnergyLevel.LOW, "At 10:30 PM")
            )
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

    fun proceedToGreeting() {
        _step.value = OnboardingStep.GREETING
    }

    fun completeOnboarding(onFinish: () -> Unit) {
        viewModelScope.launch {
            val finalName = _name.value.trim().ifBlank { "Friend" }
            preferencesRepository.setUserName(finalName)
            preferencesRepository.setOnboardingCompleted(true)

            // Seed selected habits into database
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
                    repeatDays = setOf(1, 2, 3, 4, 5, 6, 7),
                    isIndefinite = true
                )
                habitRepository.insertHabit(habit)
            }

            onFinish()
        }
    }
}

