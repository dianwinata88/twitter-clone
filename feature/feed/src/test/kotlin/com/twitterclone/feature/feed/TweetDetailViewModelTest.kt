package com.twitterclone.feature.feed

import androidx.lifecycle.SavedStateHandle
import com.twitterclone.core.model.Routes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TweetDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        tweetId: String,
        repository: FakeTweetRepository,
    ) = TweetDetailViewModel(SavedStateHandle(mapOf(Routes.ARG_TWEET_ID to tweetId)), repository)

    @Test
    fun `loads the tweet into Loaded state`() {
        val repository = FakeTweetRepository { id -> fakeTweet(id).takeIf { it.id == "t1" } }

        val viewModel = viewModel("t1", repository)

        val state = viewModel.uiState.value
        assertTrue(state is TweetDetailUiState.Loaded)
        assertEquals("t1", (state as TweetDetailUiState.Loaded).tweet.id)
    }

    @Test
    fun `missing tweet maps to NotFound`() {
        val repository = FakeTweetRepository { null }

        val viewModel = viewModel("gone", repository)

        assertEquals(TweetDetailUiState.NotFound, viewModel.uiState.value)
    }

    @Test
    fun `like failure keeps the tweet and surfaces a snackbar`() {
        val repository =
            FakeTweetRepository { id -> fakeTweet(id, likedByMe = false, likeCount = 3) }
        repository.failLikes = true
        val viewModel = viewModel("t1", repository)

        viewModel.onLikeClick()

        val state = viewModel.uiState.value as TweetDetailUiState.Loaded
        assertEquals("t1", state.tweet.id)
        assertEquals("Couldn't update like. Try again.", state.snackbarMessage)

        viewModel.onSnackbarShown()

        val cleared = viewModel.uiState.value as TweetDetailUiState.Loaded
        assertNull(cleared.snackbarMessage)
    }

    @Test
    fun `like success re-fetches the tweet for updated counts`() {
        var liked = false
        val repository =
            FakeTweetRepository { id -> fakeTweet(id, likedByMe = liked, likeCount = if (liked) 4 else 3) }
        val viewModel = viewModel("t1", repository)
        liked = true // repository flips server-side on toggle

        viewModel.onLikeClick()

        val state = viewModel.uiState.value as TweetDetailUiState.Loaded
        assertTrue(state.tweet.likedByMe)
        assertEquals(4, state.tweet.likeCount)
    }
}
