package com.twitterclone.feature.auth

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        repository = FakeAuthRepository()
        viewModel = AuthViewModel(repository)
    }

    @Test
    fun `session state is unknown until the repository emits`() {
        assertEquals(SessionState.Unknown, viewModel.sessionState.value)
    }

    @Test
    fun `session state resolves to logged out without a user`() =
        runTest {
            assertEquals(
                SessionState.LoggedOut,
                viewModel.sessionState.first { it != SessionState.Unknown },
            )
        }

    @Test
    fun `session state resolves to logged in when a user exists`() =
        runTest {
            repository.setLoggedInUser(FakeAuthRepository.TEST_USER)

            assertEquals(
                SessionState.LoggedIn,
                viewModel.sessionState.first { it != SessionState.Unknown },
            )
        }

    @Test
    fun `login success sets success state`() =
        runTest {
            viewModel.login("demo", "demo")

            val state = viewModel.uiState.value
            assertTrue(state.success)
            assertFalse(state.loading)
            assertNull(state.error)
            assertEquals(1, repository.loginCalls)
        }

    @Test
    fun `login failure exposes the error and not success`() =
        runTest {
            repository.loginResult = Result.failure(Exception("Invalid credentials"))

            viewModel.login("demo", "wrong")

            val state = viewModel.uiState.value
            assertFalse(state.success)
            assertFalse(state.loading)
            assertEquals("Invalid credentials", state.error)
        }

    @Test
    fun `register success sets success state`() =
        runTest {
            viewModel.register("newuser", "New User", "pass1234")

            val state = viewModel.uiState.value
            assertTrue(state.success)
            assertFalse(state.loading)
            assertEquals(1, repository.registerCalls)
        }

    @Test
    fun `register failure exposes the error`() =
        runTest {
            repository.registerResult =
                Result.failure(Exception("Username already taken: demo"))

            viewModel.register("demo", "Demo Two", "pass1234")

            val state = viewModel.uiState.value
            assertFalse(state.success)
            assertEquals("Username already taken: demo", state.error)
        }

    @Test
    fun `submitting again clears the previous error`() =
        runTest {
            repository.loginResult = Result.failure(Exception("Invalid credentials"))
            viewModel.login("demo", "wrong")
            assertEquals("Invalid credentials", viewModel.uiState.value.error)

            repository.loginResult = Result.failure(Exception("Still broken"))
            viewModel.login("demo", "wrong")

            assertEquals("Still broken", viewModel.uiState.value.error)
        }

    @Test
    fun `clearError resets the error`() =
        runTest {
            repository.loginResult = Result.failure(Exception("Invalid credentials"))
            viewModel.login("demo", "wrong")

            viewModel.clearError()

            assertNull(viewModel.uiState.value.error)
        }

    @Test
    fun `logout delegates to the repository`() =
        runTest {
            viewModel.logout()

            assertEquals(1, repository.logoutCalls)
        }

    @Test
    fun `validation accepts the demo credentials shape`() {
        assertTrue(AuthValidation.isUsernameValid("demo"))
        assertTrue(AuthValidation.isPasswordValid("demo"))
        assertTrue(AuthValidation.isDisplayNameValid("Demo User"))
    }

    @Test
    fun `validation rejects malformed input`() {
        assertFalse(AuthValidation.isUsernameValid("ab"))
        assertFalse(AuthValidation.isUsernameValid("has space"))
        assertFalse(AuthValidation.isPasswordValid("abc"))
        assertFalse(AuthValidation.isDisplayNameValid("  "))
    }
}
