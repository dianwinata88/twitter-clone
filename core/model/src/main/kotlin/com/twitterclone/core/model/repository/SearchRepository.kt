package com.twitterclone.core.model.repository

import androidx.paging.PagingData
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.model.User
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    /** Tweets whose text matches [query], reverse-chronological. */
    fun searchTweets(query: String): Flow<PagingData<Tweet>>

    /** Users whose username or display name matches [query]. */
    fun searchUsers(query: String): Flow<PagingData<User>>

    /** Most-engaged tweets, ordered by engagement descending. */
    fun trending(limit: Int = 10): Flow<List<Tweet>>
}
