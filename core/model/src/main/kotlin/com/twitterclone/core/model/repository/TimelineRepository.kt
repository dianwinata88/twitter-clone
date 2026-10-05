package com.twitterclone.core.model.repository

import androidx.paging.PagingData
import com.twitterclone.core.model.Tweet
import kotlinx.coroutines.flow.Flow

interface TimelineRepository {
    /** Tweets from followed users + self, reverse-chronological. */
    fun homeTimeline(): Flow<PagingData<Tweet>>

    fun userTweets(userId: String): Flow<PagingData<Tweet>>

    suspend fun refreshHome()
}
