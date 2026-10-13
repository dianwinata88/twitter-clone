package com.twitterclone.feature.profile

import androidx.lifecycle.SavedStateHandle
import com.twitterclone.core.model.Routes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val me = testUser(id = "me", username = "me")
    private val other = testUser(id = "u2", username = "alice")

    private fun viewModel(
        savedStateHandle: SavedStateHandle,
        users: FakeUserRepository,
        timeline: FakeTimelineRepository = FakeTimelineRepository(),
        tweets: FakeTweetRepository = FakeTweetRepository(),
        auth: FakeAuthRepository = FakeAuthRepository(me),
    ): ProfileViewModel = ProfileViewModel(savedStateHandle, users, timeline, tweets, auth)

    private fun savedState(userId: String? = null): SavedStateHandle =
        SavedStateHandle().apply { userId?.let { set(Routes.ARG_USER_ID, it) } }

    @Test
    fun `loads explicit userId and marks it as not own profile`() =
        runTest {
            val users = FakeUserRepository(listOf(me, other))
            val vm = viewModel(savedState(other.id), users)

            val state = vm.uiState.value
            assertFalse(state.isLoading)
            assertEquals(other, state.user)
            assertFalse(state.isOwnProfile)
        }

    @Test
    fun `me route resolves the signed-in user`() =
        runTest {
            val users = FakeUserRepository(listOf(me))
            val vm = viewModel(savedState(), users, auth = FakeAuthRepository(me))

            val state = vm.uiState.value
            assertEquals(me, state.user)
            assertTrue(state.isOwnProfile)
            assertFalse(state.signedOut)
        }

    @Test
    fun `me route without a session reports signed out`() =
        runTest {
            val vm =
                viewModel(
                    savedState(),
                    FakeUserRepository(),
                    auth = FakeAuthRepository(null),
                )

            assertTrue(vm.uiState.value.signedOut)
        }

    @Test
    fun `missing user reports load failure`() =
        runTest {
            val vm = viewModel(savedState("ghost"), FakeUserRepository(listOf(me)))

            assertTrue(vm.uiState.value.loadFailed)
            assertEquals(null, vm.uiState.value.user)
        }

    @Test
    fun `tweets flow pages the resolved user timeline`() =
        runTest {
            val timeline = FakeTimelineRepository()
            val users = FakeUserRepository(listOf(me, other))
            val vm = viewModel(savedState(other.id), users, timeline = timeline)

            assertNotNull(vm.tweets.first())
            assertEquals(other.id, timeline.userTweetsFor)
        }

    @Test
    fun `toggleFollow updates followedByMe and follower count`() =
        runTest {
            val notFollowed = other.copy(followedByMe = false, followerCount = 10)
            val users = FakeUserRepository(listOf(me, notFollowed))
            users.toggleResult = Result.success(true)
            val vm = viewModel(savedState(other.id), users)

            vm.toggleFollow()

            val state = vm.uiState.value
            assertEquals(listOf(other.id), users.toggledIds)
            assertTrue(state.user!!.followedByMe)
            assertEquals(11, state.user.followerCount)
            assertFalse(state.followBusy)
        }

    @Test
    fun `toggleFollow failure reverts optimistic state and emits error`() =
        runTest {
            val notFollowed = other.copy(followedByMe = false, followerCount = 10)
            val users = FakeUserRepository(listOf(me, notFollowed))
            users.toggleResult = Result.failure(IllegalStateException("boom"))
            val vm = viewModel(savedState(other.id), users)
            vm.toggleFollow()

            val state = vm.uiState.value
            assertFalse(state.user!!.followedByMe)
            assertEquals(10, state.user.followerCount)
            assertFalse(state.followBusy)
            assertTrue(vm.events.first() is ProfileEvent.Error)
        }

    @Test
    fun `updateProfile success stores returned user and emits ProfileSaved`() =
        runTest {
            val updated = me.copy(displayName = "New Name", bio = "New bio")
            val users = FakeUserRepository(listOf(me))
            users.updateResult = Result.success(updated)
            val vm = viewModel(savedState(), users)
            vm.updateProfile("New Name", "New bio")

            assertEquals(
                "New Name",
                vm.uiState.value.user!!
                    .displayName
            )
            assertEquals(
                "New bio",
                vm.uiState.value.user!!
                    .bio
            )
            assertFalse(vm.uiState.value.editSaving)
            assertTrue(vm.events.first() is ProfileEvent.ProfileSaved)
        }

    @Test
    fun `updateProfile failure emits error`() =
        runTest {
            val users = FakeUserRepository(listOf(me))
            users.updateResult = Result.failure(IllegalStateException("nope"))
            val vm = viewModel(savedState(), users)
            vm.updateProfile("New Name", "New bio")

            assertFalse(vm.uiState.value.editSaving)
            assertTrue(vm.events.first() is ProfileEvent.Error)
        }

    @Test
    fun `logout calls repository and emits LoggedOut`() =
        runTest {
            val auth = FakeAuthRepository(me)
            val vm = viewModel(savedState(), FakeUserRepository(listOf(me)), auth = auth)
            vm.logout()

            assertTrue(auth.loggedOut)
            assertTrue(vm.events.first() is ProfileEvent.LoggedOut)
        }

    @Test
    fun `toggleLike delegates to tweet repository`() =
        runTest {
            val tweets = FakeTweetRepository()
            val vm = viewModel(savedState(me.id), FakeUserRepository(listOf(me)), tweets = tweets)

            vm.toggleLike("t1")

            assertEquals(listOf("t1"), tweets.likedIds)
        }
}
