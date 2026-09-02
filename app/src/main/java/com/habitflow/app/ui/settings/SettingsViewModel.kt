package com.habitflow.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.domain.repository.BillingRepository
import com.habitflow.app.domain.repository.DarkModeOption
import com.habitflow.app.domain.repository.PaletteFamily
import com.habitflow.app.domain.repository.ThemeMode
import com.habitflow.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val billingRepository: BillingRepository
) : ViewModel() {

    val userName: StateFlow<String> = preferencesRepository.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Alex")

    val userHeadline: StateFlow<String> = preferencesRepository.userHeadline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Architect of Daily Flow")

    val paletteFamily: StateFlow<PaletteFamily> = preferencesRepository.paletteFamily
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PaletteFamily.MATCHA_OAT)

    val darkModeOption: StateFlow<DarkModeOption> = preferencesRepository.darkModeOption
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DarkModeOption.LIGHT)

    val themeMode: StateFlow<ThemeMode> = preferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.MATCHA_OAT)

    val notificationsEnabled: StateFlow<Boolean> = preferencesRepository.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isPro: StateFlow<Boolean> = billingRepository.isPro
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setUserName(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            preferencesRepository.setUserName(name.trim())
        }
    }

    fun setUserHeadline(headline: String) {
        viewModelScope.launch {
            preferencesRepository.setUserHeadline(headline.trim())
        }
    }

    fun setPaletteFamily(family: PaletteFamily) {
        viewModelScope.launch {
            preferencesRepository.setPaletteFamily(family)
        }
    }

    fun setDarkModeOption(option: DarkModeOption) {
        viewModelScope.launch {
            preferencesRepository.setDarkModeOption(option)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationsEnabled(enabled)
        }
    }

    fun purchasePro() {
        viewModelScope.launch {
            billingRepository.purchasePro()
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            billingRepository.restorePurchases()
        }
    }

    fun toggleProStatus(isPro: Boolean) {
        viewModelScope.launch {
            billingRepository.setProStatus(isPro)
        }
    }
}
