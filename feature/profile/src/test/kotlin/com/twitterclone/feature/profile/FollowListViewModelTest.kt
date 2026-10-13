package com.twitterclone.feature.profile

import androidx.lifecycle.SavedStateHandle
import com.twitterclone.core.model.Routes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FollowListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val me = testUser(id = "me", username = "me")
    private val other = testUser(id = "u2", username = "alice")

    private fun viewModel(
        userId: String,
        users: FakeUserRepository,
        auth: FakeAuthRepository = FakeAuthRepository(me),
    ): FollowListViewModel =
        FollowListViewModel(
            SavedStateHandle().apply { set(Routes.ARG_USER_ID, userId) },
            users,
            auth,
        )

    @Test
    fun `exposes followers and following flows for the route userId`() =
        runTest {
            val users = FakeUserRepository(listOf(me, other))
            val vm = viewModel(other.id, users)

            assertNotNull(vm.followers.first())
            assertNotNull(vm.following.first())
            assertEquals(other.id, users.followersFor)
            assertEquals(other.id, users.followingFor)
        }

    @Test
    fun `loads the list owner for the header`() =
        runTest {
            val users = FakeUserRepository(listOf(me, other))
            val vm = viewModel(other.id, users)

            assertEquals(other, vm.owner.value)
        }

    @Test
    fun `exposes the signed-in viewer`() =
        runTest {
            val vm = viewModel(other.id, FakeUserRepository(listOf(me, other)))

            assertEquals(me, vm.viewer.value)
        }

    @Test
    fun `toggleFollow calls repository and clears pending`() =
        runTest {
            val users = FakeUserRepository(listOf(me, other))
            val vm = viewModel(other.id, users)

            vm.toggleFollow("u9")

            assertEquals(listOf("u9"), users.toggledIds)
            assertTrue(vm.pendingFollowIds.value.isEmpty())
        }

    @Test
    fun `toggleFollow failure emits error`() =
        runTest {
            val users = FakeUserRepository(listOf(me, other))
            users.toggleResult = Result.failure(IllegalStateException("boom"))
            val vm = viewModel(other.id, users)
            vm.toggleFollow("u9")

            assertTrue(vm.events.first() is FollowListEvent.Error)
            assertTrue(vm.pendingFollowIds.value.isEmpty())
        }
}
