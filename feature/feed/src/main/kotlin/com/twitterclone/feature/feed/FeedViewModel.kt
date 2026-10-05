package com.twitterclone.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.model.repository.TimelineRepository
import com.twitterclone.core.model.repository.TweetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel
    @Inject
    constructor(
        timelineRepository: TimelineRepository,
        private val tweetRepository: TweetRepository,
    ) : ViewModel() {
        val tweets: Flow<PagingData<Tweet>> = timelineRepository.homeTimeline()

        fun onLikeClick(tweetId: String) {
            viewModelScope.launch {
                tweetRepository.toggleLike(tweetId)
            }
        }
    }
