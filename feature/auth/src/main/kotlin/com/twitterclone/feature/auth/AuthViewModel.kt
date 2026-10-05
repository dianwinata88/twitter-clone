package com.twitterclone.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twitterclone.core.model.User
import com.twitterclone.core.model.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Immutable UI state for the auth graph (MVVM + UDF). */
data class AuthUiState(
    /** True until the first [AuthRepository.currentUser] emission arrives. */
    val loading: Boolean = true,
    /** True while a login/register call is in flight. */
    val submitting: Boolean = false,
    /** Message to surface in a snackbar; cleared via [AuthViewModel.onErrorShown]. */
    val error: String? = null,
    /** The signed-in user, or `null` when logged out. */
    val user: User? = null,
    /**
     * One-shot flag set when an explicit login/register action succeeds. The
     * current screen consumes it (navigates to the feed) via
     * [AuthViewModel.onAuthActionConsumed]. Distinguishes a user-initiated
     * sign-in from the app's background demo login, which only updates [user].
     */
    val authActionSucceeded: Boolean = false,
) {
    val loggedIn: Boolean get() = user != null
}

@HiltViewModel
class AuthViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AuthUiState())
        val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                authRepository.currentUser.collect { user ->
                    _uiState.update { it.copy(loading = false, user = user) }
                }
            }
        }

        fun login(
            username: String,
            password: String
        ) {
            submit { authRepository.login(username.trim(), password) }
        }

        fun register(
            username: String,
            displayName: String,
            password: String
        ) {
            submit { authRepository.register(username.trim(), displayName.trim(), password) }
        }

        fun logout() {
            viewModelScope.launch { authRepository.logout() }
        }

        fun onErrorShown() {
            _uiState.update { it.copy(error = null) }
        }

        fun onAuthActionConsumed() {
            _uiState.update { it.copy(authActionSucceeded = false) }
        }

        private fun submit(action: suspend () -> Result<User>) {
            if (_uiState.value.submitting) return
            _uiState.update { it.copy(submitting = true, error = null) }
            viewModelScope.launch {
                val result = action()
                _uiState.update { state ->
                    result.fold(
                        onSuccess = { user ->
                            state.copy(submitting = false, error = null, user = user, authActionSucceeded = true)
                        },
                        onFailure = { error ->
                            state.copy(submitting = false, error = error.message ?: "Something went wrong")
                        },
                    )
                }
            }
        }
    }
