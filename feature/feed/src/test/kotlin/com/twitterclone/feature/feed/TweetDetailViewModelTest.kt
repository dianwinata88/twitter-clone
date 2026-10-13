package com.twitterclone.feature.feed

import androidx.lifecycle.SavedStateHandle
import com.twitterclone.core.model.Routes
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TweetDetailViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var tweetRepository: FakeTweetRepository

    private fun viewModel(tweetId: String = "t1") =
        TweetDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf(Routes.ARG_TWEET_ID to tweetId)),
            tweetRepository = tweetRepository,
        )

    @Before
    fun setUp() {
        tweetRepository = FakeTweetRepository()
    }

    @Test
    fun `loads tweet on init`() =
        runTest {
            val tweet = fakeTweet("t1")
            tweetRepository.tweets["t1"] = tweet

            val vm = viewModel()

            assertFalse(vm.uiState.value.isLoading)
            assertEquals(tweet, vm.uiState.value.tweet)
            assertFalse(vm.uiState.value.notFound)
        }

    @Test
    fun `missing tweet sets notFound`() =
        runTest {
            val vm = viewModel("missing")

            assertFalse(vm.uiState.value.isLoading)
            assertNull(vm.uiState.value.tweet)
            assertTrue(vm.uiState.value.notFound)
        }

    @Test
    fun `repository error sets loadError`() =
        runTest {
            tweetRepository.getTweetFailure = RuntimeException("boom")

            val vm = viewModel()

            assertFalse(vm.uiState.value.isLoading)
            assertTrue(vm.uiState.value.loadError)
        }

    @Test
    fun `toggleLike applies optimistic like then confirms`() =
        runTest {
            tweetRepository.tweets["t1"] = fakeTweet("t1", likedByMe = false, likeCount = 3)
            val vm = viewModel()

            vm.toggleLike()

            val tweet = vm.uiState.value.tweet!!
            assertTrue(tweet.likedByMe)
            assertEquals(4, tweet.likeCount)
            assertEquals(listOf("t1"), tweetRepository.likeCalls)
            assertFalse(vm.uiState.value.likeError)
        }

    @Test
    fun `toggleLike on liked tweet unlikes`() =
        runTest {
            tweetRepository.tweets["t1"] = fakeTweet("t1", likedByMe = true, likeCount = 3)
            val vm = viewModel()

            vm.toggleLike()

            val tweet = vm.uiState.value.tweet!!
            assertFalse(tweet.likedByMe)
            assertEquals(2, tweet.likeCount)
        }

    @Test
    fun `toggleLike failure reverts state and sets likeError`() =
        runTest {
            tweetRepository.tweets["t1"] = fakeTweet("t1", likedByMe = false, likeCount = 3)
            tweetRepository.likeFailure = RuntimeException("backend down")
            val vm = viewModel()

            vm.toggleLike()

            val state = vm.uiState.value
            assertEquals(false, state.tweet!!.likedByMe)
            assertEquals(3, state.tweet.likeCount)
            assertTrue(state.likeError)

            vm.consumeLikeError()
            assertFalse(vm.uiState.value.likeError)
        }
}
