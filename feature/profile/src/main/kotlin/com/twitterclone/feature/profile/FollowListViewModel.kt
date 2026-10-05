package com.twitterclone.feature.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twitterclone.core.model.Routes
import com.twitterclone.core.model.User
import com.twitterclone.core.model.repository.AuthRepository
import com.twitterclone.core.model.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FollowListUiState(
    val isLoading: Boolean = true,
    val selectedTab: String = Routes.TAB_FOLLOWERS,
    val followers: List<User> = emptyList(),
    val following: List<User> = emptyList(),
    val selfId: String? = null,
    val snackbarMessage: String? = null,
) {
    val visibleUsers: List<User>
        get() =
            if (selectedTab == Routes.TAB_FOLLOWING) {
                following
            } else {
                followers
            }
}

@HiltViewModel
class FollowListViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val userRepository: UserRepository,
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        private val userId: String = checkNotNull(savedStateHandle[Routes.ARG_USER_ID])
        private val initialTab: String =
            savedStateHandle[Routes.ARG_TAB] ?: Routes.TAB_FOLLOWERS

        private val _uiState = MutableStateFlow(FollowListUiState(selectedTab = initialTab))
        val uiState: StateFlow<FollowListUiState> = _uiState.asStateFlow()

        init {
            load()
        }

        private fun load() {
            viewModelScope.launch {
                val me = authRepository.currentUser.first()
                val followers = userRepository.followers(userId)
                val following = userRepository.following(userId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        followers = followers,
                        following = following,
                        selfId = me?.id,
                    )
                }
            }
        }

        fun onTabSelected(tab: String) {
            _uiState.update { it.copy(selectedTab = tab) }
        }

        fun onFollowClick(user: User) {
            updateUser(user.id) { it.copy(followedByMe = !it.followedByMe) }
            viewModelScope.launch {
                userRepository
                    .toggleFollow(user.id)
                    .onSuccess { newValue ->
                        updateUser(user.id) { it.copy(followedByMe = newValue) }
                    }.onFailure {
                        updateUser(user.id) { it.copy(followedByMe = user.followedByMe) }
                        _uiState.update {
                            it.copy(snackbarMessage = "Couldn't update follow status")
                        }
                    }
            }
        }

        fun onSnackbarShown() {
            _uiState.update { it.copy(snackbarMessage = null) }
        }

        private fun updateUser(
            targetId: String,
            transform: (User) -> User
        ) {
            _uiState.update { state ->
                state.copy(
                    followers =
                        state.followers.map {
                            if (it.id == targetId) transform(it) else it
                        },
                    following =
                        state.following.map {
                            if (it.id == targetId) transform(it) else it
                        },
                )
            }
        }
    }
