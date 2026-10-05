package com.twitterclone.feature.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twitterclone.core.model.Routes
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.model.repository.TweetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TweetDetailUiState {
    data object Loading : TweetDetailUiState

    data class Loaded(
        val tweet: Tweet,
        val snackbarMessage: String? = null,
    ) : TweetDetailUiState

    data object NotFound : TweetDetailUiState

    data class Error(
        val message: String
    ) : TweetDetailUiState
}

@HiltViewModel
class TweetDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val tweetRepository: TweetRepository,
    ) : ViewModel() {
        private val tweetId: String = checkNotNull(savedStateHandle[Routes.ARG_TWEET_ID])

        private val _uiState = MutableStateFlow<TweetDetailUiState>(TweetDetailUiState.Loading)
        val uiState: StateFlow<TweetDetailUiState> = _uiState.asStateFlow()

        init {
            loadTweet()
        }

        fun retry() = loadTweet()

        fun onLikeClick() {
            viewModelScope.launch {
                tweetRepository
                    .toggleLike(tweetId)
                    .onSuccess { refreshTweet() }
                    .onFailure {
                        _uiState.update { state ->
                            if (state is TweetDetailUiState.Loaded) {
                                state.copy(snackbarMessage = "Couldn't update like. Try again.")
                            } else {
                                state
                            }
                        }
                    }
            }
        }

        fun onSnackbarShown() {
            _uiState.update { state ->
                if (state is TweetDetailUiState.Loaded) state.copy(snackbarMessage = null) else state
            }
        }

        private fun loadTweet() {
            viewModelScope.launch {
                _uiState.value = TweetDetailUiState.Loading
                _uiState.value =
                    try {
                        tweetRepository
                            .getTweet(tweetId)
                            ?.let { TweetDetailUiState.Loaded(it) }
                            ?: TweetDetailUiState.NotFound
                    } catch (t: Throwable) {
                        if (t is CancellationException) throw t
                        TweetDetailUiState.Error(t.message ?: "Something went wrong")
                    }
            }
        }

        /** Re-fetches the tweet after a successful like so counts stay correct. */
        private suspend fun refreshTweet() {
            tweetRepository.getTweet(tweetId)?.let { tweet ->
                _uiState.update { state ->
                    if (state is TweetDetailUiState.Loaded) {
                        state.copy(tweet = tweet)
                    } else {
                        TweetDetailUiState.Loaded(tweet)
                    }
                }
            }
        }
    }
