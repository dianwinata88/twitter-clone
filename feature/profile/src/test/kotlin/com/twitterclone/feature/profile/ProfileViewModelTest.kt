package com.twitterclone.feature.profile

import androidx.lifecycle.SavedStateHandle
import com.twitterclone.core.model.Routes
import com.twitterclone.core.model.User
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val me = testUser(id = "me", followerCount = 10)
    private val other = testUser(id = "u2", followerCount = 5, followedByMe = false)

    private fun viewModel(
        userId: String?,
        users: Map<String, User> = mapOf(me.id to me, other.id to other),
        userRepository: FakeUserRepository = FakeUserRepository(users),
    ) = ProfileViewModel(
        savedStateHandle =
            SavedStateHandle(
                if (userId == null) emptyMap() else mapOf(Routes.ARG_USER_ID to userId),
            ),
        userRepository = userRepository,
        timelineRepository = FakeTimelineRepository(),
        tweetRepository = FakeTweetRepository(),
        authRepository = FakeAuthRepository(me),
    )

    @Test
    fun `loads other user profile`() =
        runTest {
            val vm = viewModel(userId = other.id)

            val state = vm.uiState.value
            assertFalse(state.isLoading)
            assertEquals(other, state.user)
            assertFalse(state.isSelf)
            assertFalse(state.userNotFound)
        }

    @Test
    fun `missing userId arg resolves to current user`() =
        runTest {
            val vm = viewModel(userId = null)

            val state = vm.uiState.value
            assertEquals(me, state.user)
            assertTrue(state.isSelf)
        }

    @Test
    fun `unknown user sets userNotFound`() =
        runTest {
            val vm = viewModel(userId = "ghost")

            val state = vm.uiState.value
            assertTrue(state.userNotFound)
            assertNull(state.user)
        }

    @Test
    fun `toggleFollow applies optimistic update`() =
        runTest {
            val repo = FakeUserRepository(mapOf(other.id to other))
            repo.toggleFollowResult = Result.success(true)
            val vm = viewModel(userId = other.id, userRepository = repo)

            vm.onFollowClick()

            val user = vm.uiState.value.user!!
            assertTrue(user.followedByMe)
            assertEquals(6, user.followerCount)
        }

    @Test
    fun `toggleFollow rolls back and shows snackbar on failure`() =
        runTest {
            val repo = FakeUserRepository(mapOf(other.id to other))
            repo.toggleFollowResult = Result.failure(RuntimeException("boom"))
            val vm = viewModel(userId = other.id, userRepository = repo)

            vm.onFollowClick()

            val state = vm.uiState.value
            assertEquals(other, state.user)
            assertFalse(state.user!!.followedByMe)
            assertEquals(5, state.user.followerCount)
            assertEquals("Couldn't update follow status", state.snackbarMessage)
        }

    @Test
    fun `updateProfile applies returned user`() =
        runTest {
            val vm = viewModel(userId = null)

            vm.updateProfile("New Name", "New bio")

            val state = vm.uiState.value
            assertEquals("New Name", state.user!!.displayName)
            assertEquals("New bio", state.user.bio)
            assertFalse(state.isUpdatingProfile)
        }
}
