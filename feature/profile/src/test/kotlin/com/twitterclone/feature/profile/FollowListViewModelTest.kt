package com.twitterclone.feature.profile

import androidx.lifecycle.SavedStateHandle
import com.twitterclone.core.model.Routes
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FollowListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val me = testUser(id = "me")
    private val u1 = testUser(id = "u1")
    private val u2 = testUser(id = "u2", followedByMe = true)
    private val u3 = testUser(id = "u3")

    private fun viewModel(
        tab: String = Routes.TAB_FOLLOWERS,
        userRepository: FakeUserRepository = FakeUserRepository(),
    ) = FollowListViewModel(
        savedStateHandle =
            SavedStateHandle(
                mapOf(
                    Routes.ARG_USER_ID to me.id,
                    Routes.ARG_TAB to tab,
                ),
            ),
        userRepository = userRepository,
        authRepository = FakeAuthRepository(me),
    )

    @Test
    fun `followers tab maps repository followers into state`() =
        runTest {
            val repo =
                FakeUserRepository().apply {
                    followersResult = listOf(u1, u2)
                    followingResult = listOf(u3)
                }
            val vm = viewModel(userRepository = repo)

            val state = vm.uiState.value
            assertFalse(state.isLoading)
            assertEquals(listOf(u1, u2), state.followers)
            assertEquals(listOf(u1, u2), state.visibleUsers)
            assertEquals(me.id, state.selfId)
        }

    @Test
    fun `initial tab arg selects the visible list`() =
        runTest {
            val repo =
                FakeUserRepository().apply {
                    followersResult = listOf(u1)
                    followingResult = listOf(u3)
                }
            val vm = viewModel(tab = Routes.TAB_FOLLOWING, userRepository = repo)

            assertEquals(listOf(u3), vm.uiState.value.visibleUsers)
        }

    @Test
    fun `tab switch swaps visible list`() =
        runTest {
            val repo =
                FakeUserRepository().apply {
                    followersResult = listOf(u1)
                    followingResult = listOf(u2, u3)
                }
            val vm = viewModel(userRepository = repo)

            vm.onTabSelected(Routes.TAB_FOLLOWING)
            assertEquals(listOf(u2, u3), vm.uiState.value.visibleUsers)
        }

    @Test
    fun `follow button state updates from followedByMe`() =
        runTest {
            val repo =
                FakeUserRepository().apply {
                    followersResult = listOf(u1)
                    toggleFollowResult = Result.success(true)
                }
            val vm = viewModel(userRepository = repo)

            vm.onFollowClick(u1)

            val follower = vm.uiState.value.followers[0]
            assertTrue(follower.followedByMe)
        }

    @Test
    fun `failed toggleFollow reverts row and shows snackbar`() =
        runTest {
            val repo =
                FakeUserRepository().apply {
                    followersResult = listOf(u2)
                    toggleFollowResult = Result.failure(RuntimeException("boom"))
                }
            val vm = viewModel(userRepository = repo)

            vm.onFollowClick(u2)

            val state = vm.uiState.value
            val follower = state.followers[0]
            assertTrue(follower.followedByMe)
            assertEquals("Couldn't update follow status", state.snackbarMessage)
        }
}
