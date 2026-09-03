package com.habitflow.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OnboardingStep {
    WELCOME,
    ENTER_NAME,
    GREETING
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _step = MutableStateFlow(OnboardingStep.WELCOME)
    val step: StateFlow<OnboardingStep> = _step.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    fun onNameChange(newName: String) {
        _name.value = newName
    }

    fun goToNameStep() {
        _step.value = OnboardingStep.ENTER_NAME
    }

    fun submitName() {
        if (_name.value.isNotBlank()) {
            _step.value = OnboardingStep.GREETING
        }
    }

    fun completeOnboarding(onFinish: () -> Unit) {
        viewModelScope.launch {
            val finalName = _name.value.trim().ifBlank { "Friend" }
            preferencesRepository.setUserName(finalName)
            preferencesRepository.setOnboardingCompleted(true)
            onFinish()
        }
    }
}
