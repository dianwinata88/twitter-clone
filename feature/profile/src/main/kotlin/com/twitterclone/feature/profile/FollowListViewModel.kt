package com.twitterclone.feature.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.twitterclone.core.model.Routes
import com.twitterclone.core.model.User
import com.twitterclone.core.model.repository.AuthRepository
import com.twitterclone.core.model.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One-shot UI events for the followers/following screens. */
sealed interface FollowListEvent {
    data class Error(
        val message: String?
    ) : FollowListEvent
}

/**
 * Shared ViewModel for `FOLLOWERS/{userId}` and `FOLLOWING/{userId}`. Each
 * screen collects the paging flow matching its destination; both are backed
 * by Room so a [toggleFollow] write re-emits updated rows automatically.
 */
@HiltViewModel
class FollowListViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val userRepository: UserRepository,
        authRepository: AuthRepository,
    ) : ViewModel() {
        private val userId: String = checkNotNull(savedStateHandle[Routes.ARG_USER_ID])

        val followers: Flow<PagingData<User>> =
            userRepository.followers(userId).cachedIn(viewModelScope)

        val following: Flow<PagingData<User>> =
            userRepository.following(userId).cachedIn(viewModelScope)

        /** The signed-in viewer — used to hide the follow button on your own row. */
        val viewer: StateFlow<User?> =
            authRepository.currentUser.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null,
            )

        /** The user whose list this is, for the top-bar title. */
        private val _owner = MutableStateFlow<User?>(null)
        val owner = _owner.asStateFlow()

        /** User ids with a follow toggle in flight; their buttons stay disabled. */
        private val _pendingFollowIds = MutableStateFlow<Set<String>>(emptySet())
        val pendingFollowIds = _pendingFollowIds.asStateFlow()

        private val _events = Channel<FollowListEvent>(Channel.BUFFERED)
        val events = _events.receiveAsFlow()

        init {
            viewModelScope.launch {
                _owner.value = userRepository.getUser(userId)
            }
        }

        fun toggleFollow(targetUserId: String) {
            if (targetUserId in _pendingFollowIds.value) return
            _pendingFollowIds.update { it + targetUserId }
            viewModelScope.launch {
                userRepository
                    .toggleFollow(targetUserId)
                    .onFailure { t -> _events.send(FollowListEvent.Error(t.message)) }
                _pendingFollowIds.update { it - targetUserId }
            }
        }
    }
