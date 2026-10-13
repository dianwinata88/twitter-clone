package com.twitterclone.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.model.repository.TimelineRepository
import com.twitterclone.core.model.repository.TweetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Feed-screen state besides the paged list itself. The paging pipeline reports
 * its own load states through `LazyPagingItems.loadState`; this covers the
 * pull-to-refresh spinner and one-shot action errors.
 */
data class FeedUiState(
    val isRefreshing: Boolean = false,
    val likeError: Boolean = false,
)

@HiltViewModel
class FeedViewModel
    @Inject
    constructor(
        private val timelineRepository: TimelineRepository,
        private val tweetRepository: TweetRepository,
    ) : ViewModel() {
        /** Room streams the timeline; Room invalidation propagates writes. */
        val tweets: Flow<PagingData<Tweet>> =
            timelineRepository
                .homeTimeline()
                .cachedIn(viewModelScope)

        private val _uiState = MutableStateFlow(FeedUiState())
        val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

        fun refresh() {
            if (_uiState.value.isRefreshing) return
            _uiState.update { it.copy(isRefreshing = true) }
            viewModelScope.launch {
                try {
                    timelineRepository.refreshHome()
                } catch (ce: CancellationException) {
                    throw ce
                } catch (_: Throwable) {
                    _uiState.update { it.copy(likeError = true) }
                } finally {
                    _uiState.update { it.copy(isRefreshing = false) }
                }
            }
        }

        /**
         * Optimistic like: the write lands in Room, which invalidates the
         * timeline's PagingSource — the row re-emits with the new state. On
         * failure the UI surfaces [FeedUiState.likeError].
         */
        fun toggleLike(tweetId: String) {
            viewModelScope.launch {
                tweetRepository
                    .toggleLike(tweetId)
                    .onFailure { _uiState.update { it.copy(likeError = true) } }
            }
        }

        fun consumeLikeError() {
            _uiState.update { it.copy(likeError = false) }
        }
    }
