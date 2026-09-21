package com.habitflow.app.ui.stats

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.core.util.DateUtils
import com.habitflow.app.domain.model.DailyReflection
import com.habitflow.app.domain.model.Habit
import com.habitflow.app.domain.model.HabitCompletion
import com.habitflow.app.domain.model.HabitStreakInfo
import com.habitflow.app.domain.model.OverallHabitStats
import com.habitflow.app.domain.model.TimelineItem
import com.habitflow.app.domain.repository.BillingRepository
import com.habitflow.app.domain.repository.DailyReflectionRepository
import com.habitflow.app.domain.repository.FocusItemSummary
import com.habitflow.app.domain.repository.FocusTimeStats
import com.habitflow.app.domain.repository.FocusTrackerRepository
import com.habitflow.app.domain.repository.HabitRepository
import com.habitflow.app.domain.repository.TimelineRepository
import com.habitflow.app.domain.repository.UserPreferencesRepository
import com.habitflow.app.domain.usecase.CalculateHabitCorrelationsUseCase
import com.habitflow.app.domain.usecase.CalculateInsightsUseCase
import com.habitflow.app.domain.usecase.GenerateWeeklyRetroUseCase
import com.habitflow.app.domain.usecase.GetHabitStatsUseCase
import com.habitflow.app.domain.usecase.HabitCorrelationInsight
import com.habitflow.app.domain.usecase.MindfulInsightsReport
import com.habitflow.app.domain.usecase.WeeklyZenRetro
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class DayFocusItem(
    val dateIso: String,
    val dayLetter: String,
    val dayNumber: Int,
    val focusMinutes: Int,
    val ratio: Float,
    val isToday: Boolean
)

@Immutable
data class DayRitualDetail(
    val habitId: String,
    val habitName: String,
    val habitIcon: String,
    val isCompleted: Boolean
)

@Immutable
data class DayDetailInfo(
    val dateIso: String,
    val formattedDate: String,
    val completedCount: Int,
    val totalScheduled: Int,
    val focusMinutes: Int,
    val rituals: List<DayRitualDetail>
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    getHabitStatsUseCase: GetHabitStatsUseCase,
    billingRepository: BillingRepository,
    preferencesRepository: UserPreferencesRepository,
    private val focusTrackerRepository: FocusTrackerRepository,
    private val habitRepository: HabitRepository,
    private val dailyReflectionRepository: DailyReflectionRepository,
    private val timelineRepository: TimelineRepository,
    calculateInsightsUseCase: CalculateInsightsUseCase,
    private val calculateHabitCorrelationsUseCase: CalculateHabitCorrelationsUseCase,
    private val generateWeeklyRetroUseCase: GenerateWeeklyRetroUseCase
) : ViewModel() {

    val userName: StateFlow<String> = preferencesRepository.userName
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Alex")

    val mindfulInsights: StateFlow<MindfulInsightsReport?> = calculateInsightsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val stats: StateFlow<OverallHabitStats?> = getHabitStatsUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val focusStats: StateFlow<FocusTimeStats?> = focusTrackerRepository.getFocusTimeStats()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isPro: StateFlow<Boolean> = billingRepository.isPro
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // 365-Day Parchment Heatmap Data
    val yearlyCompletions: StateFlow<Map<String, Int>> = habitRepository.getAllCompletions()
        .combine(MutableStateFlow(Unit)) { completions, _ ->
            completions.groupBy { it.date }.mapValues { it.value.size }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Habit Correlation Insights
    val correlations: StateFlow<List<HabitCorrelationInsight>> = combine(
        habitRepository.getAllHabits(includeArchived = false),
        habitRepository.getAllCompletions(),
        dailyReflectionRepository.getRecentReflections(),
        timelineRepository.getAllTimelineItems()
    ) { habits: List<Habit>, completions: List<HabitCompletion>, reflections: List<DailyReflection>, timelineItems: List<TimelineItem> ->
        calculateHabitCorrelationsUseCase(habits, completions, reflections, timelineItems)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Weekly Zen Retro
    val weeklyRetro: StateFlow<WeeklyZenRetro?> = combine(
        habitRepository.getAllHabits(includeArchived = false),
        habitRepository.getAllCompletions(),
        dailyReflectionRepository.getRecentReflections(),
        timelineRepository.getAllTimelineItems()
    ) { habits: List<Habit>, completions: List<HabitCompletion>, reflections: List<DailyReflection>, timelineItems: List<TimelineItem> ->
        generateWeeklyRetroUseCase(habits, completions, reflections, timelineItems)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Week offset for Focus Investment (0 = this week, -1 = last week, etc.)
    private val _weekOffset = MutableStateFlow(0)
    val weekOffset: StateFlow<Int> = _weekOffset.asStateFlow()

    fun previousWeek() {
        _weekOffset.value -= 1
    }

    fun nextWeek() {
        if (_weekOffset.value < 0) {
            _weekOffset.value += 1
        }
    }

    // Weekly Focus Days
    val weeklyFocusDays: StateFlow<List<DayFocusItem>> = _weekOffset.flatMapLatest { offset ->
        val today = LocalDate.now()
        val targetDate = today.plusWeeks(offset.toLong())
        val startOfWeek = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val dates = (0..6).map { startOfWeek.plusDays(it.toLong()) }
        val dateStrings = dates.map { DateUtils.formatDateIso(it) }

        focusTrackerRepository.getDailyFocusSecondsForDates(dateStrings).combine(
            MutableStateFlow(dates)
        ) { secondsMap, weekLocalDates ->
            val dayLetters = listOf("M", "T", "W", "T", "F", "S", "S")
            val maxSeconds = secondsMap.values.maxOrNull()?.coerceAtLeast(3600) ?: 3600

            weekLocalDates.mapIndexed { idx, localDate ->
                val iso = DateUtils.formatDateIso(localDate)
                val secs = secondsMap[iso] ?: 0
                val mins = secs / 60
                val ratio = if (maxSeconds > 0) (secs.toFloat() / maxSeconds.toFloat()).coerceIn(0.12f, 1f) else 0.12f

                DayFocusItem(
                    dateIso = iso,
                    dayLetter = dayLetters[idx],
                    dayNumber = localDate.dayOfMonth,
                    focusMinutes = mins,
                    ratio = ratio,
                    isToday = localDate == today
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Selected Day Focus Inspection
    private val _selectedFocusDay = MutableStateFlow<DayFocusItem?>(null)
    val selectedFocusDay: StateFlow<DayFocusItem?> = _selectedFocusDay.asStateFlow()

    fun selectFocusDay(day: DayFocusItem) {
        _selectedFocusDay.value = if (_selectedFocusDay.value?.dateIso == day.dateIso) null else day
    }

    // Detailed Inspection Bottom Sheet States
    private val _selectedMatrixDay = MutableStateFlow<DayDetailInfo?>(null)
    val selectedMatrixDay: StateFlow<DayDetailInfo?> = _selectedMatrixDay.asStateFlow()

    private val _selectedCommitment = MutableStateFlow<FocusItemSummary?>(null)
    val selectedCommitment: StateFlow<FocusItemSummary?> = _selectedCommitment.asStateFlow()

    private val _selectedHabitStreak = MutableStateFlow<HabitStreakInfo?>(null)
    val selectedHabitStreak: StateFlow<HabitStreakInfo?> = _selectedHabitStreak.asStateFlow()

    fun openCommitmentDetail(item: FocusItemSummary) {
        _selectedCommitment.value = item
    }

    fun closeCommitmentDetail() {
        _selectedCommitment.value = null
    }

    fun openHabitStreakDetail(streak: HabitStreakInfo) {
        _selectedHabitStreak.value = streak
    }

    fun closeHabitStreakDetail() {
        _selectedHabitStreak.value = null
    }

    fun inspectMatrixDay(dateIso: String) {
        viewModelScope.launch {
            val parsedDate = try {
                LocalDate.parse(dateIso)
            } catch (_: Exception) {
                LocalDate.now()
            }

            val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.US)
            val formattedDate = parsedDate.format(formatter)
            val dayOfWeek = DateUtils.getDayOfWeekInt(parsedDate)

            // Query habits and completions
            habitRepository.getAllHabits(includeArchived = false).combine(
                habitRepository.getCompletionsForDate(dateIso)
            ) { habits, completions ->
                val scheduled = habits.filter { (it.repeatDays.isEmpty() || it.repeatDays.contains(dayOfWeek)) && !it.isWintering }
                val completedHabitIds = completions.map { it.habitId }.toSet()

                val rituals = scheduled.map { habit ->
                    DayRitualDetail(
                        habitId = habit.id,
                        habitName = habit.name,
                        habitIcon = habit.icon,
                        isCompleted = completedHabitIds.contains(habit.id)
                    )
                }

                focusTrackerRepository.getTotalFocusTimeForDate(dateIso).combine(
                    MutableStateFlow(rituals)
                ) { focusSecs, rits ->
                    DayDetailInfo(
                        dateIso = dateIso,
                        formattedDate = formattedDate,
                        completedCount = completions.size,
                        totalScheduled = scheduled.size,
                        focusMinutes = focusSecs / 60,
                        rituals = rits
                    )
                }
            }.collect { detailFlow ->
                detailFlow.collect { detailInfo ->
                    _selectedMatrixDay.value = detailInfo
                }
            }
        }
    }

    fun closeMatrixDayDetail() {
        _selectedMatrixDay.value = null
    }
}
