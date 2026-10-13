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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val isOwnProfile: Boolean = false,
    val signedOut: Boolean = false,
    val loadFailed: Boolean = false,
    val followBusy: Boolean = false,
    val editSaving: Boolean = false,
)

/** One-shot UI events: navigation triggers and transient errors (snackbars). */
sealed interface ProfileEvent {
    data object LoggedOut : ProfileEvent

    data object ProfileSaved : ProfileEvent

    data class Error(
        val message: String?
    ) : ProfileEvent
}

/**
 * Backs both `PROFILE/{userId}` and the `PROFILE_ME` alias: when no
 * `{userId}` nav arg is present the signed-in user from [AuthRepository]
 * is shown instead. Follow toggles are optimistic — the UI flips
 * immediately and is reconciled (or reverted) when the repository returns.
 */
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
        private val requestedUserId: String? = savedStateHandle[Routes.ARG_USER_ID]
        private val resolvedUserId = MutableStateFlow<String?>(null)

        private val _uiState = MutableStateFlow(ProfileUiState())
        val uiState = _uiState.asStateFlow()

        private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
        val events = _events.receiveAsFlow()

        @OptIn(ExperimentalCoroutinesApi::class)
        val tweets: Flow<PagingData<Tweet>> =
            resolvedUserId
                .filterNotNull()
                .flatMapLatest { timelineRepository.userTweets(it) }
                .cachedIn(viewModelScope)

        init {
            reload()
        }

        fun reload() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                val viewer = authRepository.currentUser.first()
                val userId = requestedUserId ?: viewer?.id
                if (userId == null) {
                    _uiState.value = ProfileUiState(isLoading = false, signedOut = true)
                    return@launch
                }
                val user = userRepository.getUser(userId)
                _uiState.value =
                    ProfileUiState(
                        isLoading = false,
                        user = user,
                        isOwnProfile = viewer != null && viewer.id == user?.id,
                        loadFailed = user == null,
                    )
                if (user != null) resolvedUserId.value = user.id
            }
        }

        /** Optimistic follow/unfollow; reverts and reports on failure. */
        fun toggleFollow() {
            val snapshot = _uiState.value
            val user = snapshot.user ?: return
            if (snapshot.isOwnProfile || snapshot.followBusy) return
            val optimistic =
                user.copy(
                    followedByMe = !user.followedByMe,
                    followerCount = user.followerCount + if (user.followedByMe) -1 else 1,
                )
            _uiState.update { it.copy(followBusy = true, user = optimistic) }
            viewModelScope.launch {
                userRepository
                    .toggleFollow(user.id)
                    .onSuccess { nowFollowing ->
                        _uiState.update {
                            it.copy(
                                followBusy = false,
                                user = it.user?.copy(followedByMe = nowFollowing),
                            )
                        }
                    }.onFailure { t ->
                        _uiState.update { it.copy(followBusy = false, user = user) }
                        _events.send(ProfileEvent.Error(t.message))
                    }
            }
        }

        fun updateProfile(
            displayName: String,
            bio: String
        ) {
            if (_uiState.value.editSaving) return
            _uiState.update { it.copy(editSaving = true) }
            viewModelScope.launch {
                userRepository
                    .updateProfile(displayName.trim(), bio.trim())
                    .onSuccess { updated ->
                        _uiState.update {
                            it.copy(editSaving = false, user = updated, isOwnProfile = true)
                        }
                        _events.send(ProfileEvent.ProfileSaved)
                    }.onFailure { t ->
                        _uiState.update { it.copy(editSaving = false) }
                        _events.send(ProfileEvent.Error(t.message))
                    }
            }
        }

        fun toggleLike(tweetId: String) {
            viewModelScope.launch {
                tweetRepository
                    .toggleLike(tweetId)
                    .onFailure { t -> _events.send(ProfileEvent.Error(t.message)) }
            }
        }

        fun logout() {
            viewModelScope.launch {
                authRepository.logout()
                _events.send(ProfileEvent.LoggedOut)
            }
        }
    }
