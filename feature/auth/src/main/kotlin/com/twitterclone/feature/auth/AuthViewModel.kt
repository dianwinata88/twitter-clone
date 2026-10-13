package com.twitterclone.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twitterclone.core.model.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Auth gate state derived from [AuthRepository.currentUser]; drives the splash route. */
enum class SessionState { Unknown, LoggedOut, LoggedIn }

/**
 * UDF state for the login/register forms: one-shot submit progress.
 * `success` is consumed by the destination to trigger navigation.
 */
data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

@HiltViewModel
class AuthViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        val sessionState: StateFlow<SessionState> =
            authRepository.currentUser
                .map { user -> if (user == null) SessionState.LoggedOut else SessionState.LoggedIn }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = SessionState.Unknown,
                )

        private val _uiState = MutableStateFlow(AuthUiState())
        val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

        fun login(
            username: String,
            password: String,
        ) {
            if (_uiState.value.loading) return
            viewModelScope.launch {
                _uiState.update { it.copy(loading = true, error = null) }
                authRepository
                    .login(username.trim(), password)
                    .onSuccess { _uiState.update { it.copy(loading = false, success = true) } }
                    .onFailure { e ->
                        _uiState.update { it.copy(loading = false, error = e.message.orEmpty()) }
                    }
            }
        }

        fun register(
            username: String,
            displayName: String,
            password: String,
        ) {
            if (_uiState.value.loading) return
            viewModelScope.launch {
                _uiState.update { it.copy(loading = true, error = null) }
                authRepository
                    .register(username.trim(), displayName.trim(), password)
                    .onSuccess { _uiState.update { it.copy(loading = false, success = true) } }
                    .onFailure { e ->
                        _uiState.update { it.copy(loading = false, error = e.message.orEmpty()) }
                    }
            }
        }

        fun logout() {
            viewModelScope.launch { authRepository.logout() }
        }

        fun clearError() {
            _uiState.update { it.copy(error = null) }
        }
    }
