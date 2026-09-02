package com.habitflow.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.domain.model.OverallHabitStats
import com.habitflow.app.domain.repository.BillingRepository
import com.habitflow.app.domain.repository.FocusTimeStats
import com.habitflow.app.domain.repository.FocusTrackerRepository
import com.habitflow.app.domain.repository.UserPreferencesRepository
import com.habitflow.app.domain.usecase.GetHabitStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    getHabitStatsUseCase: GetHabitStatsUseCase,
    billingRepository: BillingRepository,
    preferencesRepository: UserPreferencesRepository,
    focusTrackerRepository: FocusTrackerRepository
) : ViewModel() {

    val userName: StateFlow<String> = preferencesRepository.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Mojammel")

    val stats: StateFlow<OverallHabitStats?> = getHabitStatsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val focusStats: StateFlow<FocusTimeStats?> = focusTrackerRepository.getFocusTimeStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isPro: StateFlow<Boolean> = billingRepository.isPro
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
}
