package com.forma.app.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forma.app.data.auth.GoogleAuthHelper
import com.forma.app.domain.model.AuthState
import com.forma.app.domain.usecase.auth.GetAuthStateUseCase
import com.forma.app.domain.usecase.auth.SendPasswordResetUseCase
import com.forma.app.domain.usecase.auth.SignInWithEmailUseCase
import com.forma.app.domain.usecase.auth.SignInWithGoogleUseCase
import com.forma.app.domain.usecase.auth.SignOutUseCase
import com.forma.app.domain.usecase.auth.SignUpWithEmailUseCase
import com.forma.app.domain.usecase.sync.SyncSanctuaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    getAuthStateUseCase: GetAuthStateUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
    private val sendPasswordResetUseCase: SendPasswordResetUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val syncSanctuaryUseCase: SyncSanctuaryUseCase,
    private val googleAuthHelper: GoogleAuthHelper
) : ViewModel() {

    val authState: StateFlow<AuthState> = getAuthStateUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthState.Unauthenticated)

    val isSyncing: StateFlow<Boolean> = syncSanctuaryUseCase.isSyncing
    val lastSyncedAt: StateFlow<Long?> = syncSanctuaryUseCase.lastSyncedAt

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun launchGoogleSignIn(activityContext: Context, webClientId: String = "") {
        viewModelScope.launch {
            if (webClientId.isBlank()) {
                _uiMessage.value = "Google Sign-In requires Web Client ID configuration in google-services.json."
                return@launch
            }
            _isLoading.value = true
            val tokenResult = googleAuthHelper.getGoogleIdToken(activityContext, webClientId)
            tokenResult.onSuccess { idToken ->
                val authResult = signInWithGoogleUseCase(idToken)
                authResult.onSuccess {
                    _uiMessage.value = "Welcome back, ${it.displayName ?: "Friend"}!"
                    syncNow()
                }.onFailure { err ->
                    _uiMessage.value = err.localizedMessage ?: "Google Sign-In failed."
                }
            }.onFailure { err ->
                _uiMessage.value = err.localizedMessage ?: "Google Sign-In cancelled."
            }
            _isLoading.value = false
        }
    }

    fun signInWithEmail(email: String, pass: String, onSuccess: () -> Unit = {}) {
        if (email.isBlank() || pass.isBlank()) {
            _uiMessage.value = "Please enter both email and password."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            signInWithEmailUseCase(email, pass)
                .onSuccess { user ->
                    _uiMessage.value = "Welcome, ${user.displayName ?: user.email}!"
                    syncNow()
                    onSuccess()
                }
                .onFailure { err ->
                    _uiMessage.value = err.localizedMessage ?: "Sign in failed."
                }
            _isLoading.value = false
        }
    }

    fun signUpWithEmail(email: String, pass: String, name: String?, onSuccess: () -> Unit = {}) {
        if (email.isBlank() || pass.length < 6) {
            _uiMessage.value = "Password must be at least 6 characters."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            signUpWithEmailUseCase(email, pass, name)
                .onSuccess { user ->
                    _uiMessage.value = "Sanctuary account created for ${user.displayName ?: user.email}!"
                    syncNow()
                    onSuccess()
                }
                .onFailure { err ->
                    _uiMessage.value = err.localizedMessage ?: "Registration failed."
                }
            _isLoading.value = false
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiMessage.value = "Please enter your email address."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            sendPasswordResetUseCase(email)
                .onSuccess {
                    _uiMessage.value = "Password reset link sent to $email."
                }
                .onFailure { err ->
                    _uiMessage.value = err.localizedMessage ?: "Failed to send reset link."
                }
            _isLoading.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
            _uiMessage.value = "Signed out of sanctuary."
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            syncSanctuaryUseCase()
                .onSuccess {
                    _uiMessage.value = "Sanctuary synced seamlessly."
                }
                .onFailure { err ->
                    _uiMessage.value = "Sync note: ${err.localizedMessage ?: "Sync completed with offline cache"}"
                }
        }
    }

    fun clearMessage() {
        _uiMessage.value = null
    }
}
