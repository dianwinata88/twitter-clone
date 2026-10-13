package com.twitterclone.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.twitterclone.core.common.di.ApplicationScope
import com.twitterclone.core.common.di.Dispatcher
import com.twitterclone.core.common.di.TcDispatchers
import com.twitterclone.core.data.fake.simulateLatency
import com.twitterclone.core.data.mapper.toModel
import com.twitterclone.core.database.dao.TweetDao
import com.twitterclone.core.datastore.SessionManager
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.model.repository.TimelineRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Timeline backed by Room [androidx.paging.PagingSource]s, so DB writes
 * (likes, posts, seeds) invalidate and re-emit automatically.
 */
@Singleton
class FakeTimelineRepository
    @Inject
    constructor(
        private val tweetDao: TweetDao,
        private val sessionManager: SessionManager,
        @ApplicationScope private val applicationScope: CoroutineScope,
        @Dispatcher(TcDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    ) : TimelineRepository {
        private val pagingConfig = PagingConfig(pageSize = 20, initialLoadSize = 40, enablePlaceholders = false)

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun homeTimeline(): Flow<PagingData<Tweet>> =
            sessionManager.currentUserId.flatMapLatest { viewerId ->
                if (viewerId == null) {
                    flowOf(PagingData.empty())
                } else {
                    Pager(pagingConfig) { tweetDao.homeTimeline(viewerId) }
                        .flow
                        .map { pagingData -> pagingData.map { it.toModel() } }
                        .cachedIn(applicationScope)
                }
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun userTweets(userId: String): Flow<PagingData<Tweet>> =
            sessionManager.currentUserId.flatMapLatest { viewerId ->
                if (viewerId == null) {
                    flowOf(PagingData.empty())
                } else {
                    Pager(pagingConfig) { tweetDao.userTweets(userId, viewerId) }
                        .flow
                        .map { pagingData -> pagingData.map { it.toModel() } }
                        .cachedIn(applicationScope)
                }
            }

        /** No-op refresh: Room already streams every change. Kept for API parity. */
        override suspend fun refreshHome() =
            withContext(ioDispatcher) {
                simulateLatency()
            }
    }
