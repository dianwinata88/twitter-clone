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

data class TweetDetailUiState(
    val isLoading: Boolean = true,
    val tweet: Tweet? = null,
    val notFound: Boolean = false,
    val loadError: Boolean = false,
    val likeError: Boolean = false,
)

@HiltViewModel
class TweetDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val tweetRepository: TweetRepository,
    ) : ViewModel() {
        private val tweetId: String = checkNotNull(savedStateHandle[Routes.ARG_TWEET_ID])

        private val _uiState = MutableStateFlow(TweetDetailUiState())
        val uiState: StateFlow<TweetDetailUiState> = _uiState.asStateFlow()

        init {
            reload()
        }

        fun reload() {
            _uiState.update { it.copy(isLoading = true, notFound = false, loadError = false) }
            viewModelScope.launch {
                try {
                    val tweet = tweetRepository.getTweet(tweetId)
                    _uiState.update {
                        if (tweet == null) {
                            it.copy(isLoading = false, notFound = true)
                        } else {
                            it.copy(isLoading = false, tweet = tweet)
                        }
                    }
                } catch (ce: CancellationException) {
                    throw ce
                } catch (_: Throwable) {
                    _uiState.update { it.copy(isLoading = false, loadError = true) }
                }
            }
        }

        /**
         * Optimistically flips the local like state, then reconciles with the
         * repository result. On failure the pre-call tweet is restored and
         * [TweetDetailUiState.likeError] is raised.
         */
        fun toggleLike() {
            val before = _uiState.value.tweet ?: return
            val optimistic =
                before.copy(
                    likedByMe = !before.likedByMe,
                    likeCount = (before.likeCount + if (before.likedByMe) -1 else 1).coerceAtLeast(0),
                )
            _uiState.update { it.copy(tweet = optimistic, likeError = false) }
            viewModelScope.launch {
                tweetRepository
                    .toggleLike(before.id)
                    .onSuccess { nowLiked ->
                        _uiState.update { state ->
                            state.tweet
                                ?.let { state.copy(tweet = it.copy(likedByMe = nowLiked)) }
                                ?: state
                        }
                    }.onFailure {
                        _uiState.update { it.copy(tweet = before, likeError = true) }
                    }
            }
        }

        fun consumeLikeError() {
            _uiState.update { it.copy(likeError = false) }
        }
    }
