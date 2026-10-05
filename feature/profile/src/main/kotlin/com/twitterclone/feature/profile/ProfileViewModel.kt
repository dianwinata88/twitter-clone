package com.twitterclone.feature.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.twitterclone.core.model.Routes
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.model.User
import com.twitterclone.core.model.repository.AuthRepository
import com.twitterclone.core.model.repository.TimelineRepository
import com.twitterclone.core.model.repository.TweetRepository
import com.twitterclone.core.model.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val isSelf: Boolean = false,
    val userNotFound: Boolean = false,
    val isUpdatingProfile: Boolean = false,
    val snackbarMessage: String? = null,
)

@HiltViewModel
class ProfileViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val userRepository: UserRepository,
        private val timelineRepository: TimelineRepository,
        private val tweetRepository: TweetRepository,
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        /** Null on the `profile/me` route, which resolves to the current user. */
        private val requestedUserId: String? = savedStateHandle[Routes.ARG_USER_ID]
        private val resolvedUserId = MutableStateFlow<String?>(null)

        private val _uiState = MutableStateFlow(ProfileUiState())
        val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

        @OptIn(ExperimentalCoroutinesApi::class)
        val tweets: Flow<PagingData<Tweet>> =
            resolvedUserId
                .filterNotNull()
                .flatMapLatest { userId -> timelineRepository.userTweets(userId) }
                .cachedIn(viewModelScope)

        init {
            load()
        }

        private fun load() {
            viewModelScope.launch {
                val me = authRepository.currentUser.first()
                val targetId = requestedUserId ?: me?.id
                if (targetId == null) {
                    _uiState.update { it.copy(isLoading = false, userNotFound = true) }
                    return@launch
                }
                resolvedUserId.value = targetId
                val user = userRepository.getUser(targetId)
                _uiState.update { state ->
                    if (user == null) {
                        state.copy(isLoading = false, userNotFound = true)
                    } else {
                        state.copy(isLoading = false, user = user, isSelf = user.id == me?.id)
                    }
                }
            }
        }

        fun onFollowClick() {
            val current = _uiState.value.user ?: return
            val optimistic =
                current.copy(
                    followedByMe = !current.followedByMe,
                    followerCount =
                        (current.followerCount + if (current.followedByMe) -1 else 1)
                            .coerceAtLeast(0),
                )
            _uiState.update { it.copy(user = optimistic) }
            viewModelScope.launch {
                userRepository
                    .toggleFollow(current.id)
                    .onSuccess { newValue ->
                        _uiState.update {
                            it.copy(user = optimistic.copy(followedByMe = newValue))
                        }
                    }.onFailure {
                        _uiState.update {
                            it.copy(
                                user = current,
                                snackbarMessage = "Couldn't update follow status",
                            )
                        }
                    }
            }
        }

        fun onLikeClick(tweetId: String) {
            viewModelScope.launch {
                tweetRepository.toggleLike(tweetId)
            }
        }

        fun updateProfile(
            displayName: String,
            bio: String
        ) {
            viewModelScope.launch {
                _uiState.update { it.copy(isUpdatingProfile = true) }
                userRepository
                    .updateProfile(displayName, bio)
                    .onSuccess { updated ->
                        _uiState.update { it.copy(user = updated, isUpdatingProfile = false) }
                    }.onFailure {
                        _uiState.update {
                            it.copy(
                                isUpdatingProfile = false,
                                snackbarMessage = "Couldn't update profile",
                            )
                        }
                    }
            }
        }

        fun onSnackbarShown() {
            _uiState.update { it.copy(snackbarMessage = null) }
        }
    }
