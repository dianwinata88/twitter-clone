package com.twitterclone.feature.auth

import com.twitterclone.core.model.User
import com.twitterclone.core.model.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAuthRepository

    private val demoUser =
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

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeAuthRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `session restore emits the persisted user`() =
        runTest {
            repository.setUser(demoUser)
            val viewModel = AuthViewModel(repository)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.loading)
            assertTrue(state.loggedIn)
            assertEquals(demoUser, state.user)
        }

    @Test
    fun `logged out session resolves to not logged in`() =
        runTest {
            repository.setUser(null)
            val viewModel = AuthViewModel(repository)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.loading)
            assertFalse(state.loggedIn)
            assertNull(state.user)
        }

    @Test
    fun `login success stores the user and clears the busy flag`() =
        runTest {
            val viewModel = AuthViewModel(repository)
            advanceUntilIdle()

            viewModel.login("demo", "demo")
            assertTrue(viewModel.uiState.value.submitting)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.submitting)
            assertTrue(state.loggedIn)
            assertEquals(demoUser, state.user)
            assertNull(state.error)
            assertTrue(state.authActionSucceeded)
        }

    @Test
    fun `login failure surfaces an error and stays logged out`() =
        runTest {
            repository.shouldFail = true
            val viewModel = AuthViewModel(repository)
            advanceUntilIdle()

            viewModel.login("demo", "wrong")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.submitting)
            assertFalse(state.loggedIn)
            assertEquals("Invalid credentials", state.error)

            viewModel.onErrorShown()
            assertNull(viewModel.uiState.value.error)
        }

    @Test
    fun `logout clears the user`() =
        runTest {
            repository.setUser(demoUser)
            val viewModel = AuthViewModel(repository)
            advanceUntilIdle()

            viewModel.logout()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.loggedIn)
            assertNull(viewModel.uiState.value.user)
        }

    private class FakeAuthRepository : AuthRepository {
        private val user = MutableStateFlow<User?>(null)

        var shouldFail = false

        override val currentUser: Flow<User?> = user

        fun setUser(value: User?) {
            user.value = value
        }

        override suspend fun login(
            username: String,
            password: String
        ): Result<User> =
            if (shouldFail) {
                Result.failure(Exception("Invalid credentials"))
            } else {
                val loggedIn =
                    User(
                        id = "u-1",
                        username = username,
                        displayName = "Demo User",
                        bio = "",
                        avatarUrl = null,
                        headerUrl = null,
                        followerCount = 0,
                        followingCount = 0,
                        tweetCount = 0,
                        createdAt = Instant.fromEpochMilliseconds(0),
                    )
                user.value = loggedIn
                Result.success(loggedIn)
            }

        override suspend fun register(
            username: String,
            displayName: String,
            password: String
        ): Result<User> = login(username, password)

        override suspend fun logout() {
            user.value = null
        }
    }
}
