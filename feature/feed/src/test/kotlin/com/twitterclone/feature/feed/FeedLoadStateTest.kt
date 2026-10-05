package com.twitterclone.feature.feed

import androidx.paging.LoadState
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class FeedLoadStateTest {
    @Test
    fun `empty list while refreshing maps to Loading`() {
        assertEquals(FeedContentState.Loading, feedContentState(itemCount = 0, refresh = LoadState.Loading))
    }

    @Test
    fun `empty list after refresh completes maps to Empty`() {
        assertEquals(
            FeedContentState.Empty,
            feedContentState(itemCount = 0, refresh = LoadState.NotLoading(endOfPaginationReached = true)),
        )
    }

    @Test
    fun `empty list after a refresh error maps to Error`() {
        assertEquals(
            FeedContentState.Error,
            feedContentState(itemCount = 0, refresh = LoadState.Error(IOException("boom"))),
        )
    }

    @Test
    fun `any items keep the screen in Content regardless of load state`() {
        assertEquals(FeedContentState.Content, feedContentState(itemCount = 5, refresh = LoadState.Loading))
        assertEquals(
            FeedContentState.Content,
            feedContentState(itemCount = 5, refresh = LoadState.Error(IOException("boom"))),
        )
    }
}
