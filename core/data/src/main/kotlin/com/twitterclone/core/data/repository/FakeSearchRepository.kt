package com.twitterclone.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.twitterclone.core.common.di.ApplicationScope
import com.twitterclone.core.data.fake.simulateLatency
import com.twitterclone.core.data.mapper.toModel
import com.twitterclone.core.database.dao.FollowDao
import com.twitterclone.core.database.dao.TweetDao
import com.twitterclone.core.database.dao.UserDao
import com.twitterclone.core.datastore.SessionManager
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.model.User
import com.twitterclone.core.model.repository.SearchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake search backed by Room LIKE queries. Tweet search and trending hydrate
 * viewer state (likedByMe) for the session user; people search decorates each
 * result with followedByMe.
 */
@Singleton
class FakeSearchRepository
    @Inject
    constructor(
        private val tweetDao: TweetDao,
        private val userDao: UserDao,
        private val followDao: FollowDao,
        private val sessionManager: SessionManager,
        @ApplicationScope private val applicationScope: CoroutineScope,
    ) : SearchRepository {
        private val pagingConfig = PagingConfig(pageSize = 20, initialLoadSize = 40, enablePlaceholders = false)

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun searchTweets(query: String): Flow<PagingData<Tweet>> =
            sessionManager.currentUserId.flatMapLatest { viewerId ->
                if (viewerId == null || query.isBlank()) {
                    flowOf(PagingData.empty())
                } else {
                    Pager(pagingConfig) { tweetDao.search(query.trim(), viewerId) }
                        .flow
                        .map { pagingData -> pagingData.map { it.toModel() } }
                        .cachedIn(applicationScope)
                }
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun searchUsers(query: String): Flow<PagingData<User>> =
            sessionManager.currentUserId.flatMapLatest { viewerId ->
                if (viewerId == null || query.isBlank()) {
                    flowOf(PagingData.empty())
                } else {
                    Pager(pagingConfig) { userDao.search(query.trim()) }
                        .flow
                        .map { pagingData ->
                            pagingData.map { it.toModel(followedByMe = followDao.exists(viewerId, it.id)) }
                        }.cachedIn(applicationScope)
                }
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun trending(limit: Int): Flow<List<Tweet>> =
            sessionManager.currentUserId.flatMapLatest { viewerId ->
                if (viewerId == null) {
                    flowOf(emptyList())
                } else {
                    flow {
                        simulateLatency()
                        emitAll(tweetDao.trending(viewerId, limit))
                    }.map { hydrated -> hydrated.map { it.toModel() } }
                }
            }
    }
