package com.twitterclone.feature.auth

import com.twitterclone.core.model.User
import com.twitterclone.core.model.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Instant

/** Scriptable in-memory [AuthRepository] for ViewModel tests. */
class FakeAuthRepository : AuthRepository {
    private val user = MutableStateFlow<User?>(null)

    override val currentUser: Flow<User?> = user

    var loginResult: Result<User> = Result.success(TEST_USER)
    var registerResult: Result<User> = Result.success(TEST_USER)

    var loginCalls = 0
        private set
    var registerCalls = 0
        private set
    var logoutCalls = 0
        private set

    fun setLoggedInUser(value: User?) {
        user.update { value }
    }

    override suspend fun login(
        username: String,
        password: String,
    ): Result<User> {
        loginCalls++
        return loginResult.onSuccess { user.update { it } }
    }

    override suspend fun register(
        username: String,
        displayName: String,
        password: String,
    ): Result<User> {
        registerCalls++
        return registerResult.onSuccess { user.update { it } }
    }

    override suspend fun logout() {
        logoutCalls++
        user.update { null }
    }

    companion object {
        val TEST_USER =
            User(
                id = "u-1",
                username = "demo",
                displayName = "Demo User",
                bio = "",
                avatarUrl = null,
                headerUrl = null,
                followerCount = 0,
                followingCount = 0,
                tweetCount = 0,
                createdAt = Instant.fromEpochMilliseconds(0),
            )
    }
}
