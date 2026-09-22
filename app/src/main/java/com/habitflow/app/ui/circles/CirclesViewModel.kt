package com.habitflow.app.ui.circles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.domain.model.AuthState
import com.habitflow.app.domain.model.Circle
import com.habitflow.app.domain.model.CircleCheckIn
import com.habitflow.app.domain.model.CircleMember
import com.habitflow.app.domain.model.CircleReaction
import com.habitflow.app.domain.model.CircleReactionType
import com.habitflow.app.domain.usecase.auth.GetAuthStateUseCase
import com.habitflow.app.domain.usecase.circle.CreateCircleUseCase
import com.habitflow.app.domain.usecase.circle.JoinCircleUseCase
import com.habitflow.app.domain.usecase.circle.LeaveCircleUseCase
import com.habitflow.app.domain.usecase.circle.ObserveCircleCheckInsUseCase
import com.habitflow.app.domain.usecase.circle.ObserveCircleMembersUseCase
import com.habitflow.app.domain.usecase.circle.ObserveCircleReactionsUseCase
import com.habitflow.app.domain.usecase.circle.ObserveUserCirclesUseCase
import com.habitflow.app.domain.usecase.circle.PostCircleCheckInUseCase
import com.habitflow.app.domain.usecase.circle.SendCircleReactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CirclesViewModel @Inject constructor(
    getAuthStateUseCase: GetAuthStateUseCase,
    observeUserCirclesUseCase: ObserveUserCirclesUseCase,
    private val observeCircleMembersUseCase: ObserveCircleMembersUseCase,
    private val observeCircleCheckInsUseCase: ObserveCircleCheckInsUseCase,
    private val observeCircleReactionsUseCase: ObserveCircleReactionsUseCase,
    private val createCircleUseCase: CreateCircleUseCase,
    private val joinCircleUseCase: JoinCircleUseCase,
    private val leaveCircleUseCase: LeaveCircleUseCase,
    private val postCircleCheckInUseCase: PostCircleCheckInUseCase,
    private val sendCircleReactionUseCase: SendCircleReactionUseCase
) : ViewModel() {

    val authState: StateFlow<AuthState> = getAuthStateUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthState.Unauthenticated)

    val circles: StateFlow<List<Circle>> = observeUserCirclesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCircleId = MutableStateFlow<String?>(null)
    val selectedCircleId: StateFlow<String?> = _selectedCircleId.asStateFlow()

    val selectedCircleMembers: StateFlow<List<CircleMember>> = _selectedCircleId
        .flatMapLatest { circleId ->
            if (circleId == null) flowOf(emptyList()) else observeCircleMembersUseCase(circleId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedCircleCheckIns: StateFlow<List<CircleCheckIn>> = _selectedCircleId
        .flatMapLatest { circleId ->
            if (circleId == null) flowOf(emptyList()) else observeCircleCheckInsUseCase(circleId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedCircleReactions: StateFlow<List<CircleReaction>> = _selectedCircleId
        .flatMapLatest { circleId ->
            if (circleId == null) flowOf(emptyList()) else observeCircleReactionsUseCase(circleId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun selectCircle(circleId: String?) {
        _selectedCircleId.value = circleId
    }

    fun createCircle(name: String, description: String, themes: List<String>, onSuccess: (Circle) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            createCircleUseCase(name, description, themes)
                .onSuccess { circle ->
                    _uiMessage.value = "Circle '${circle.name}' formed!"
                    _selectedCircleId.value = circle.id
                    onSuccess(circle)
                }
                .onFailure { err ->
                    _uiMessage.value = err.localizedMessage ?: "Failed to create circle."
                }
            _isLoading.value = false
        }
    }

    fun joinCircle(inviteCode: String, onSuccess: (Circle) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            joinCircleUseCase(inviteCode)
                .onSuccess { circle ->
                    _uiMessage.value = "Joined '${circle.name}'!"
                    _selectedCircleId.value = circle.id
                    onSuccess(circle)
                }
                .onFailure { err ->
                    _uiMessage.value = err.localizedMessage ?: "Invalid invite code."
                }
            _isLoading.value = false
        }
    }

    fun leaveCircle(circleId: String) {
        viewModelScope.launch {
            leaveCircleUseCase(circleId)
                .onSuccess {
                    if (_selectedCircleId.value == circleId) {
                        _selectedCircleId.value = null
                    }
                    _uiMessage.value = "Left circle."
                }
                .onFailure { err ->
                    _uiMessage.value = err.localizedMessage ?: "Could not leave circle."
                }
        }
    }

    fun postCheckIn(circleId: String, habitName: String, note: String = "") {
        viewModelScope.launch {
            postCircleCheckInUseCase(circleId, habitName, note)
                .onSuccess {
                    _uiMessage.value = "Presence logged in circle!"
                }
                .onFailure { err ->
                    _uiMessage.value = err.localizedMessage ?: "Failed to log check-in."
                }
        }
    }

    fun sendReaction(circleId: String, recipientId: String, type: CircleReactionType) {
        viewModelScope.launch {
            sendCircleReactionUseCase(circleId, recipientId, type)
                .onSuccess {
                    _uiMessage.value = "${type.emoji} Sent ${type.label}!"
                }
                .onFailure { err ->
                    _uiMessage.value = err.localizedMessage ?: "Reaction failed."
                }
        }
    }

    fun clearMessage() {
        _uiMessage.value = null
    }
}
