package com.twitterclone.feature.composetweet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twitterclone.core.model.repository.AuthRepository
import com.twitterclone.core.model.repository.TweetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val MAX_TWEET_LENGTH = 280
const val MAX_MEDIA_ITEMS = 4

/**
 * UI state for the composer. Posting is one-shot: on success [ComposeTweetEvent.TweetPosted]
 * fires and the screen pops back; the new row (already written by the repository with a
 * PENDING→CONFIRMED sync transition) reaches the timeline via Room invalidation.
 */
data class ComposeTweetUiState(
    val text: String = "",
    val mediaUris: List<String> = emptyList(),
    val avatarUrl: String? = null,
    val isPosting: Boolean = false,
) {
    val charactersRemaining: Int get() = MAX_TWEET_LENGTH - text.length
    val isOverLimit: Boolean get() = charactersRemaining < 0
    val canTweet: Boolean get() = text.isNotBlank() && !isOverLimit && !isPosting
    val canAddMedia: Boolean get() = mediaUris.size < MAX_MEDIA_ITEMS && !isPosting
}

sealed interface ComposeTweetEvent {
    data object TweetPosted : ComposeTweetEvent

    data object PostFailed : ComposeTweetEvent
}

@HiltViewModel
class ComposeTweetViewModel
    @Inject
    constructor(
        private val tweetRepository: TweetRepository,
        authRepository: AuthRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ComposeTweetUiState())
        val uiState = _uiState.asStateFlow()

        private val _events = Channel<ComposeTweetEvent>(Channel.BUFFERED)
        val events = _events.receiveAsFlow()

        init {
            viewModelScope.launch {
                authRepository.currentUser.collect { user ->
                    _uiState.update { it.copy(avatarUrl = user?.avatarUrl) }
                }
            }
        }

        fun onTextChange(text: String) {
            _uiState.update { it.copy(text = text) }
        }

        fun onMediaSelected(uris: List<String>) {
            _uiState.update { state ->
                state.copy(mediaUris = (state.mediaUris + uris).take(MAX_MEDIA_ITEMS))
            }
        }

        fun onRemoveMedia(uri: String) {
            _uiState.update { state ->
                state.copy(mediaUris = state.mediaUris - uri)
            }
        }

        fun onTweetClicked() {
            val snapshot = _uiState.value
            if (!snapshot.canTweet) return
            _uiState.update { it.copy(isPosting = true) }
            viewModelScope.launch {
                tweetRepository
                    .postTweet(snapshot.text.trim(), snapshot.mediaUris)
                    .onSuccess { _events.send(ComposeTweetEvent.TweetPosted) }
                    .onFailure {
                        _uiState.update { it.copy(isPosting = false) }
                        _events.send(ComposeTweetEvent.PostFailed)
                    }
            }
        }
    }
