package com.twitterclone.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.twitterclone.core.common.di.ApplicationScope
import com.twitterclone.core.common.di.Dispatcher
import com.twitterclone.core.common.di.TcDispatchers
import com.twitterclone.core.common.result.safeCall
import com.twitterclone.core.data.fake.FailureInjector
import com.twitterclone.core.data.fake.SimulatedBackendException
import com.twitterclone.core.data.fake.simulateLatency
import com.twitterclone.core.data.mapper.toModel
import com.twitterclone.core.database.dao.FollowDao
import com.twitterclone.core.database.dao.UserDao
import com.twitterclone.core.database.model.FollowEntity
import com.twitterclone.core.datastore.SessionManager
import com.twitterclone.core.model.User
import com.twitterclone.core.model.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Fake user store backed by Room; follow edges live in the `follows` table. */
@Singleton
class FakeUserRepository
    @Inject
    constructor(
        private val userDao: UserDao,
        private val followDao: FollowDao,
        private val sessionManager: SessionManager,
        private val failureInjector: FailureInjector,
        @ApplicationScope private val applicationScope: CoroutineScope,
        @Dispatcher(TcDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    ) : UserRepository {
        private val pagingConfig = PagingConfig(pageSize = 20, initialLoadSize = 40, enablePlaceholders = false)

        override suspend fun getUser(id: String): User? =
            withContext(ioDispatcher) {
                val viewerId = sessionManager.currentUserId.first()
                userDao.getById(id)?.toModel(
                    followedByMe = viewerId != null && followDao.exists(viewerId, id),
                )
            }

        override suspend fun getUserByHandle(username: String): User? =
            withContext(ioDispatcher) {
                val viewerId = sessionManager.currentUserId.first()
                userDao.getByUsername(username)?.let {
                    it.toModel(followedByMe = viewerId != null && followDao.exists(viewerId, it.id))
                }
            }

        /**
         * Optimistic flip of the follow edge, then recomputes both users' counts.
         * `INSERT OR IGNORE` on the (followerId, followeeId) PK keeps it
         * constraint-safe.
         */
        override suspend fun toggleFollow(userId: String): Result<Boolean> =
            withContext(ioDispatcher) {
                simulateLatency()
                safeCall {
                    failureInjector.throwIfEnabled()
                    val viewerId =
                        sessionManager.currentUserId.first()
                            ?: throw SimulatedBackendException()
                    val nowFollowing =
                        if (followDao.exists(viewerId, userId)) {
                            followDao.delete(viewerId, userId)
                            false
                        } else {
                            followDao.insertIgnore(
                                FollowEntity(viewerId, userId, System.currentTimeMillis()),
                            )
                            true
                        }
                    userDao.refreshFollowCounts(userId)
                    userDao.refreshFollowCounts(viewerId)
                    nowFollowing
                }
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun followers(userId: String): Flow<PagingData<User>> =
            sessionManager.currentUserId.flatMapLatest { viewerId ->
                if (viewerId == null) {
                    flowOf(PagingData.empty())
                } else {
                    Pager(pagingConfig) { userDao.followers(userId) }
                        .flow
                        .map { pagingData ->
                            pagingData.map { it.toModel(followedByMe = followDao.exists(viewerId, it.id)) }
                        }.cachedIn(applicationScope)
                }
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun following(userId: String): Flow<PagingData<User>> =
            sessionManager.currentUserId.flatMapLatest { viewerId ->
                if (viewerId == null) {
                    flowOf(PagingData.empty())
                } else {
                    Pager(pagingConfig) { userDao.following(userId) }
                        .flow
                        .map { pagingData ->
                            pagingData.map { it.toModel(followedByMe = followDao.exists(viewerId, it.id)) }
                        }.cachedIn(applicationScope)
                }
            }

        override suspend fun updateProfile(
            displayName: String,
            bio: String
        ): Result<User> =
            withContext(ioDispatcher) {
                simulateLatency()
                safeCall {
                    failureInjector.throwIfEnabled()
                    val viewerId =
                        sessionManager.currentUserId.first()
                            ?: throw SimulatedBackendException()
                    userDao.updateProfile(viewerId, displayName, bio)
                    userDao.getById(viewerId)?.toModel()
                        ?: throw IllegalStateException("Current user vanished: $viewerId")
                }
            }
    }
