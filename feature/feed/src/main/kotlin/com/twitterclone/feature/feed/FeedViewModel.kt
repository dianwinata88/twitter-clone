package com.twitterclone.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.model.repository.TimelineRepository
import com.twitterclone.core.model.repository.TweetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Non-paging feed state. [snackbarMessage] is a consumable message: the UI
 * shows it once, then calls [FeedViewModel.onSnackbarShown] to clear it.
 */
data class FeedUiState(
    val isRefreshing: Boolean = false,
    val snackbarMessage: String? = null,
)

@HiltViewModel
class FeedViewModel
    @Inject
    constructor(
        private val timelineRepository: TimelineRepository,
        private val tweetRepository: TweetRepository,
    ) : ViewModel() {
        val tweets: Flow<PagingData<Tweet>> = timelineRepository.homeTimeline().cachedIn(viewModelScope)

        private val _uiState = MutableStateFlow(FeedUiState())
        val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

        /**
         * Like writes are optimistic inside the repository (Room emits the new
         * state through the paging flow); a failure leaves the timeline
         * untouched, so the only UI work is surfacing the error.
         */
        fun onLikeClick(tweetId: String) {
            viewModelScope.launch {
                tweetRepository.toggleLike(tweetId).onFailure {
                    _uiState.update { state ->
                        state.copy(snackbarMessage = "Couldn't update like. Pull to refresh and try again.")
                    }
                }
            }
        }

        fun onRefresh() {
            if (_uiState.value.isRefreshing) return
            viewModelScope.launch {
                _uiState.update { it.copy(isRefreshing = true) }
                try {
                    timelineRepository.refreshHome()
                } finally {
                    _uiState.update { it.copy(isRefreshing = false) }
                }
            }
        }

        fun onSnackbarShown() {
            _uiState.update { it.copy(snackbarMessage = null) }
        }
    }
