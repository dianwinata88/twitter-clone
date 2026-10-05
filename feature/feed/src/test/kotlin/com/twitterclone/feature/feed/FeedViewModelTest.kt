package com.twitterclone.feature.feed

import com.twitterclone.core.model.repository.TimelineRepository
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FeedViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val timelineRepository = FakeTimelineRepository()
    private val tweetRepository = FakeTweetRepository()

    private fun viewModel() = FeedViewModel(timelineRepository, tweetRepository)

    @Test
    fun `like toggle delegates to repository without surfacing a snackbar`() {
        val viewModel = viewModel()

        viewModel.onLikeClick("t1")

        assertEquals(listOf("t1"), tweetRepository.likedTweetIds)
        assertNull(viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun `like toggle failure surfaces a snackbar message that is consumed once`() {
        tweetRepository.failLikes = true
        val viewModel = viewModel()

        viewModel.onLikeClick("t1")

        // The repo rolled back (nothing optimistic to undo in state); the UI
        // surfaces the failure for the user to see.
        assertNotNull(viewModel.uiState.value.snackbarMessage)

        viewModel.onSnackbarShown()

        assertNull(viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun `refresh calls refreshHome and clears isRefreshing`() {
        val viewModel = viewModel()

        viewModel.onRefresh()

        assertEquals(1, timelineRepository.refreshHomeCalls)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `a second refresh while one is in flight is ignored`() {
        val viewModel = viewModel()
        val gate = CompletableDeferred<Unit>()
        val gatedTimeline =
            object : TimelineRepository by timelineRepository {
                override suspend fun refreshHome() {
                    timelineRepository.refreshHome()
                    gate.await()
                }
            }
        val gatedViewModel = FeedViewModel(gatedTimeline, tweetRepository)

        gatedViewModel.onRefresh()
        assertTrue(gatedViewModel.uiState.value.isRefreshing)

        gatedViewModel.onRefresh()

        assertEquals(1, timelineRepository.refreshHomeCalls)
        gate.complete(Unit)
    }
}
