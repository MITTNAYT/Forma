package com.forma.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forma.app.domain.model.Chronotype
import com.forma.app.domain.model.EnergyLevel
import com.forma.app.domain.model.Habit
import com.forma.app.domain.model.SubscriptionTier
import com.forma.app.domain.model.Subtask
import com.forma.app.domain.model.TimeOfDay
import com.forma.app.domain.repository.BillingRepository
import com.forma.app.domain.repository.HabitRepository
import com.forma.app.domain.repository.PaletteFamily
import com.forma.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class OnboardingStep {
    WELCOME,
    ENTER_NAME,
    CHOOSE_THEME,
    CHOOSE_STARTER_PACK,
    CALIBRATION,
    PRO_PAYWALL,
    GREETING
}

data class StarterHabitItem(
    val name: String,
    val icon: String,
    val colorTag: String,
    val timeOfDay: TimeOfDay,
    val energyLevel: EnergyLevel,
    val cueText: String? = null,
    val reminderTimeMinutes: Int? = null,
    val subtasks: List<Subtask> = emptyList(),
    val isSelected: Boolean = true
)

enum class StarterPackType(val title: String, val subtitle: String, val iconName: String) {
    MINDFUL_LIVING("Mindful Living", "Calm mornings, natural sunlight & deep evening rest", "spa"),
    DEEP_WORK("Coders & Builders", "Deep coding blocks, technical reading & git hygiene", "terminal"),
    CREATIVE_STUDIO("Creators & Designers", "Visual moodboard, sharing WIP & studio workspace reset", "sparkles"),
    ACADEMIC_STUDY("Students & Scholars", "Active recall, Feynman concept synthesis & desk reset", "book")
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val habitRepository: HabitRepository,
    private val billingRepository: BillingRepository
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

    val currentTier: StateFlow<SubscriptionTier> = billingRepository.currentTier
        .stateIn(viewModelScope, SharingStarted.Eagerly, SubscriptionTier.FREE)

    val isPro: StateFlow<Boolean> = billingRepository.isPro
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

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
            _step.value = OnboardingStep.CHOOSE_THEME
        }
    }

    fun selectChronotype(chronotype: Chronotype) {
        _selectedChronotype.value = chronotype
        viewModelScope.launch {
            preferencesRepository.setChronotype(chronotype)
        }
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
                StarterHabitItem(
                    name = "Morning Sunlight & Hydration",
                    icon = "spa",
                    colorTag = "#D4AF37",
                    timeOfDay = TimeOfDay.MORNING,
                    energyLevel = EnergyLevel.LOW,
                    cueText = "Immediately after stepping out of bed",
                    reminderTimeMinutes = 7 * 60,
                    subtasks = listOf(
                        Subtask(title = "Drink 500ml water with sea salt", completed = false),
                        Subtask(title = "10 minutes natural daylight outside", completed = false)
                    )
                ),
                StarterHabitItem(
                    name = "Midday Box Breathing (4-4-4-4)",
                    icon = "spa",
                    colorTag = "#4E6542",
                    timeOfDay = TimeOfDay.AFTERNOON,
                    energyLevel = EnergyLevel.LOW,
                    cueText = "Before opening lunch",
                    reminderTimeMinutes = 13 * 60,
                    subtasks = listOf(
                        Subtask(title = "Inhale 4s, Hold 4s, Exhale 4s, Hold 4s", completed = false),
                        Subtask(title = "Complete 4 rounds of calm breath", completed = false)
                    )
                ),
                StarterHabitItem(
                    name = "Evening Digital Sunset",
                    icon = "moon",
                    colorTag = "#5E548E",
                    timeOfDay = TimeOfDay.EVENING,
                    energyLevel = EnergyLevel.LOW,
                    cueText = "At 9:30 PM reminder chime",
                    reminderTimeMinutes = 21 * 60 + 30,
                    subtasks = listOf(
                        Subtask(title = "Dock laptop & phone in living room", completed = false),
                        Subtask(title = "Dim bedroom lights to warm glow", completed = false)
                    )
                )
            )
            StarterPackType.DEEP_WORK -> listOf(
                StarterHabitItem(
                    name = "Deep Coding Block (90m)",
                    icon = "zap",
                    colorTag = "#2C221E",
                    timeOfDay = TimeOfDay.MORNING,
                    energyLevel = EnergyLevel.HIGH,
                    cueText = "After morning espresso & daily goal check",
                    reminderTimeMinutes = 10 * 60 + 30,
                    subtasks = listOf(
                        Subtask(title = "Close Slack, email & browser notifications", completed = false),
                        Subtask(title = "Complete primary pull-request commit", completed = false),
                        Subtask(title = "Write unit tests for new logic", completed = false)
                    )
                ),
                StarterHabitItem(
                    name = "Daily Architecture & Tech Reading",
                    icon = "book",
                    colorTag = "#D4AF37",
                    timeOfDay = TimeOfDay.MORNING,
                    energyLevel = EnergyLevel.LOW,
                    cueText = "Before writing the first line of code",
                    reminderTimeMinutes = 8 * 60 + 45,
                    subtasks = listOf(
                        Subtask(title = "Read 1 engineering post or release note", completed = false),
                        Subtask(title = "Note 1 design pattern to adopt", completed = false)
                    )
                ),
                StarterHabitItem(
                    name = "Git Hygiene & Staging Clean Up",
                    icon = "sparkles",
                    colorTag = "#5E548E",
                    timeOfDay = TimeOfDay.EVENING,
                    energyLevel = EnergyLevel.LOW,
                    cueText = "Before closing terminal and IDE",
                    reminderTimeMinutes = 17 * 60 + 45,
                    subtasks = listOf(
                        Subtask(title = "Commit cleanly formatted changes", completed = false),
                        Subtask(title = "Push branch to remote", completed = false),
                        Subtask(title = "Write 1 TODO comment where to resume", completed = false)
                    )
                )
            )
            StarterPackType.CREATIVE_STUDIO -> listOf(
                StarterHabitItem(
                    name = "Visual Observation & Moodboard",
                    icon = "sparkles",
                    colorTag = "#8D5B4C",
                    timeOfDay = TimeOfDay.MORNING,
                    energyLevel = EnergyLevel.HIGH,
                    cueText = "After opening workspace canvas",
                    reminderTimeMinutes = 11 * 60,
                    subtasks = listOf(
                        Subtask(title = "Save 3 exceptional reference pieces", completed = false),
                        Subtask(title = "Analyze typography, lighting, and palette", completed = false)
                    )
                ),
                StarterHabitItem(
                    name = "Share Work-in-Progress",
                    icon = "feather",
                    colorTag = "#D4AF37",
                    timeOfDay = TimeOfDay.AFTERNOON,
                    energyLevel = EnergyLevel.MEDIUM,
                    reminderTimeMinutes = 17 * 60 + 30,
                    cueText = "After concluding studio sprint",
                    subtasks = listOf(
                        Subtask(title = "Select 1 clean work-in-progress visual", completed = false),
                        Subtask(title = "Write 2 sentences explaining design intent", completed = false)
                    )
                ),
                StarterHabitItem(
                    name = "Studio Reset & File Hygiene",
                    icon = "spa",
                    colorTag = "#4E6542",
                    timeOfDay = TimeOfDay.EVENING,
                    energyLevel = EnergyLevel.LOW,
                    reminderTimeMinutes = 18 * 60 + 30,
                    cueText = "Before stepping away from studio desk",
                    subtasks = listOf(
                        Subtask(title = "Name and group all open layers", completed = false),
                        Subtask(title = "Wipe desk surface clean", completed = false)
                    )
                )
            )
            StarterPackType.ACADEMIC_STUDY -> listOf(
                StarterHabitItem(
                    name = "Active Recall & Flashcards",
                    icon = "book",
                    colorTag = "#4E6542",
                    timeOfDay = TimeOfDay.AFTERNOON,
                    energyLevel = EnergyLevel.HIGH,
                    cueText = "After afternoon study lecture",
                    reminderTimeMinutes = 16 * 60 + 30,
                    subtasks = listOf(
                        Subtask(title = "Review 25 spaced repetition flashcards", completed = false),
                        Subtask(title = "Write out 3 difficult definitions from memory", completed = false)
                    )
                ),
                StarterHabitItem(
                    name = "Feynman Concept Synthesis",
                    icon = "feather",
                    colorTag = "#2C221E",
                    timeOfDay = TimeOfDay.AFTERNOON,
                    energyLevel = EnergyLevel.HIGH,
                    cueText = "At the start of core study block",
                    reminderTimeMinutes = 14 * 60 + 30,
                    subtasks = listOf(
                        Subtask(title = "Pick 1 complex theorem or framework", completed = false),
                        Subtask(title = "Explain it plainly on paper without jargon", completed = false)
                    )
                ),
                StarterHabitItem(
                    name = "Study Desk Reset & Pack Bag",
                    icon = "spa",
                    colorTag = "#5E548E",
                    timeOfDay = TimeOfDay.EVENING,
                    energyLevel = EnergyLevel.LOW,
                    cueText = "Before shutting study room door",
                    reminderTimeMinutes = 21 * 60,
                    subtasks = listOf(
                        Subtask(title = "File lecture handouts in binders", completed = false),
                        Subtask(title = "Plug in tablet and laptop to charge", completed = false)
                    )
                )
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

    fun proceedToCalibration() {
        _step.value = OnboardingStep.CALIBRATION
    }

    fun proceedToPaywall() {
        _step.value = OnboardingStep.PRO_PAYWALL
    }

    fun continueAsFreeExplorer() {
        _step.value = OnboardingStep.GREETING
    }

    fun purchaseTier(tier: SubscriptionTier, onComplete: () -> Unit) {
        viewModelScope.launch {
            if (tier == SubscriptionTier.LIFETIME_FOUNDER) {
                billingRepository.purchaseLifetimeFounder()
            } else if (tier == SubscriptionTier.MONTHLY_PRO) {
                billingRepository.purchaseMonthlyPro()
            }
            _step.value = OnboardingStep.GREETING
            onComplete()
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

            // Seed selected habits with rich micro-steps into database
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
                    isIndefinite = true,
                    subtasks = starter.subtasks
                )
                habitRepository.insertHabit(habit)
            }

            onFinish()
        }
    }
}
