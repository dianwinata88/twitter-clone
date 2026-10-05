package com.twitterclone.core.data.repository

import com.twitterclone.core.common.di.Dispatcher
import com.twitterclone.core.common.di.TcDispatchers
import com.twitterclone.core.common.result.safeCall
import com.twitterclone.core.data.fake.FailureInjector
import com.twitterclone.core.data.fake.simulateLatency
import com.twitterclone.core.data.mapper.toModel
import com.twitterclone.core.database.dao.UserDao
import com.twitterclone.core.database.model.UserEntity
import com.twitterclone.core.database.seed.SeedData
import com.twitterclone.core.datastore.SessionManager
import com.twitterclone.core.model.User
import com.twitterclone.core.model.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake auth backed by the seeded Room users plus an in-memory credential map.
 * Accepts `demo`/`demo` and any user registered through [register]. The
 * session is persisted via [SessionManager] (DataStore).
 */
@Singleton
class FakeAuthRepository
    @Inject
    constructor(
        private val userDao: UserDao,
        private val sessionManager: SessionManager,
        private val failureInjector: FailureInjector,
        @Dispatcher(TcDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    ) : AuthRepository {
        // Registered (non-seeded) credentials, keyed by username.
        private val registeredPasswords = ConcurrentHashMap<String, String>()

        @OptIn(ExperimentalCoroutinesApi::class)
        override val currentUser: Flow<User?> =
            sessionManager.currentUserId.flatMapLatest { userId ->
                if (userId == null) {
                    flowOf(null)
                } else {
                    userDao.observeById(userId).map { it?.toModel() }
                }
            }

        override suspend fun login(
            username: String,
            password: String
        ): Result<User> =
            withContext(ioDispatcher) {
                simulateLatency()
                safeCall {
                    failureInjector.throwIfEnabled()
                    val entity =
                        userDao.getByUsername(username)
                            ?: throw AuthException("Unknown username: $username")
                    val valid =
                        (entity.username == SeedData.DEMO_USERNAME && password == SeedData.DEMO_PASSWORD) ||
                            registeredPasswords[username] == password
                    if (!valid) throw AuthException("Invalid credentials")
                    sessionManager.setSession(token = "fake-token-${entity.id}", userId = entity.id)
                    entity.toModel(followedByMe = false)
                }
            }

        override suspend fun register(
            username: String,
            displayName: String,
            password: String
        ): Result<User> =
            withContext(ioDispatcher) {
                simulateLatency()
                safeCall {
                    failureInjector.throwIfEnabled()
                    if (userDao.getByUsername(username) != null) {
                        throw AuthException("Username already taken: $username")
                    }
                    val now = System.currentTimeMillis()
                    val entity =
                        UserEntity(
                            id = "u-${java.util.UUID.randomUUID()}",
                            username = username,
                            displayName = displayName,
                            bio = "",
                            avatarUrl = "https://picsum.photos/seed/avatar-$username/200/200",
                            headerUrl = null,
                            followerCount = 0,
                            followingCount = 0,
                            tweetCount = 0,
                            createdAtEpochMillis = now,
                        )
                    userDao.upsert(entity)
                    registeredPasswords[username] = password
                    sessionManager.setSession(token = "fake-token-${entity.id}", userId = entity.id)
                    entity.toModel()
                }
            }

        override suspend fun logout() =
            withContext(ioDispatcher) {
                simulateLatency()
                sessionManager.clear()
            }

        private class AuthException(
            message: String
        ) : Exception(message)
    }
