package com.twitterclone.feature.composetweet

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.model.User
import com.twitterclone.core.model.repository.TweetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class ComposerViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(repository: TweetRepository = FakeTweetRepository()) = ComposerViewModel(repository, SavedStateHandle())

    @Test
    fun `post is disabled for empty draft and enabled once there is content`() =
        runTest {
            val vm = viewModel()
            assertFalse(vm.uiState.value.canPost)

            vm.onTextChange("hello")
            assertTrue(vm.uiState.value.canPost)

            vm.onTextChange("   ")
            assertFalse(vm.uiState.value.canPost)
        }

    @Test
    fun `post is enabled with media alone and disabled over the limit`() =
        runTest {
            val vm = viewModel()

            vm.onMediaPicked(listOf("content://m/1"))
            assertTrue(vm.uiState.value.canPost)

            vm.onTextChange("a".repeat(TweetLengthCounter.MAX_LENGTH + 1))
            assertTrue(vm.uiState.value.isOverLimit)
            assertFalse(vm.uiState.value.canPost)

            vm.onTextChange("ok")
            assertTrue(vm.uiState.value.canPost)
        }

    @Test
    fun `media picks are deduped and capped at four`() =
        runTest {
            val vm = viewModel()
            vm.onMediaPicked(listOf("u1", "u2", "u2", "u3", "u4", "u5"))
            assertEquals(listOf("u1", "u2", "u3", "u4"), vm.uiState.value.mediaUris)

            vm.onMediaRemoved("u2")
            assertEquals(listOf("u1", "u3", "u4"), vm.uiState.value.mediaUris)
        }

    @Test
    fun `successful post marks state posted and calls repository with trimmed draft`() =
        runTest {
            val repo = FakeTweetRepository()
            val vm = viewModel(repo)

            vm.onTextChange("  gm  ")
            vm.onMediaPicked(listOf("u1"))
            vm.post()

            assertTrue(vm.uiState.value.posted)
            assertFalse(vm.uiState.value.posting)
            assertEquals("gm", repo.lastText)
            assertEquals(listOf("u1"), repo.lastMediaUris)
        }

    @Test
    fun `failed post surfaces error and keeps the draft`() =
        runTest {
            val vm = viewModel(FakeTweetRepository(fail = true))

            vm.onTextChange("draft survives")
            vm.post()

            val state = vm.uiState.value
            assertFalse(state.posted)
            assertFalse(state.posting)
            assertEquals("draft survives", state.text)
            assertEquals("backend says no", state.error)

            vm.onErrorShown()
            assertNull(vm.uiState.value.error)
        }

    @Test
    fun `restored saved state repopulates the draft`() =
        runTest {
            val handle =
                SavedStateHandle(
                    mapOf(
                        "composer_text" to "restored draft",
                        "composer_media" to arrayListOf("u1"),
                    ),
                )
            val vm = ComposerViewModel(FakeTweetRepository(), handle)

            vm.uiState.test {
                val state = awaitItem()
                assertEquals("restored draft", state.text)
                assertEquals(listOf("u1"), state.mediaUris)
                cancelAndIgnoreRemainingEvents()
            }
        }

    private class FakeTweetRepository(
        private val fail: Boolean = false,
    ) : TweetRepository {
        var lastText: String? = null
        var lastMediaUris: List<String>? = null

        override suspend fun getTweet(id: String): Tweet? = null

        override suspend fun postTweet(
            text: String,
            mediaLocalUris: List<String>
        ): Result<Tweet> {
            lastText = text
            lastMediaUris = mediaLocalUris
            return if (fail) {
                Result.failure(RuntimeException("backend says no"))
            } else {
                Result.success(tweet(text))
            }
        }

        override suspend fun toggleLike(tweetId: String): Result<Boolean> = Result.success(true)

        private fun tweet(text: String) =
            Tweet(
                id = "t-1",
                author =
                    User(
                        id = "u-1",
                        username = "demo",
                        displayName = "Demo",
                        bio = "",
                        avatarUrl = null,
                        headerUrl = null,
                        followerCount = 0,
                        followingCount = 0,
                        tweetCount = 0,
                        createdAt = Instant.fromEpochMilliseconds(0),
                    ),
                text = text,
                media = emptyList(),
                createdAt = Instant.fromEpochMilliseconds(0),
                replyCount = 0,
                repostCount = 0,
                likeCount = 0,
                likedByMe = false,
            )
    }
}
