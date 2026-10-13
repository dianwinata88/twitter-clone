package com.twitterclone.feature.feed

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FeedViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var timelineRepository: FakeTimelineRepository
    private lateinit var tweetRepository: FakeTweetRepository
    private lateinit var viewModel: FeedViewModel

    @Before
    fun setUp() {
        timelineRepository = FakeTimelineRepository(timelineTweets = listOf(fakeTweet("t1")))
        tweetRepository = FakeTweetRepository()
        viewModel = FeedViewModel(timelineRepository, tweetRepository)
    }

    @Test
    fun `timeline flow comes from repository`() {
        assertEquals(1, timelineRepository.homeTimelineCalls)
    }

    @Test
    fun `refresh sets isRefreshing while in flight then clears`() =
        runTest {
            timelineRepository.refreshGate = CompletableDeferred()

            viewModel.refresh()
            assertTrue(viewModel.uiState.value.isRefreshing)
            assertEquals(1, timelineRepository.refreshCalls)

            timelineRepository.refreshGate!!.complete(Unit)
            assertFalse(viewModel.uiState.value.isRefreshing)
        }

    @Test
    fun `second refresh while in flight is ignored`() =
        runTest {
            timelineRepository.refreshGate = CompletableDeferred()

            viewModel.refresh()
            viewModel.refresh()
            assertEquals(1, timelineRepository.refreshCalls)

            timelineRepository.refreshGate!!.complete(Unit)
        }

    @Test
    fun `toggleLike delegates to tweet repository`() =
        runTest {
            tweetRepository.tweets["t1"] = fakeTweet("t1")

            viewModel.toggleLike("t1")

            assertEquals(listOf("t1"), tweetRepository.likeCalls)
            assertFalse(viewModel.uiState.value.likeError)
        }

    @Test
    fun `toggleLike failure surfaces likeError until consumed`() =
        runTest {
            tweetRepository.likeFailure = RuntimeException("backend down")

            viewModel.toggleLike("t1")
            assertTrue(viewModel.uiState.value.likeError)

            viewModel.consumeLikeError()
            assertFalse(viewModel.uiState.value.likeError)
        }
}
