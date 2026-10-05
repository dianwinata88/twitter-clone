package com.twitterclone.feature.feed

import androidx.paging.LoadState

/** Whole-screen content state derived from the paging refresh load state. */
enum class FeedContentState { Loading, Empty, Error, Content }

/**
 * Maps `LazyPagingItems` state to the single [FeedContentState] the screen
 * should show. Once any items exist the list stays up and per-page
 * loading/error UI moves into the list instead.
 */
fun feedContentState(
    itemCount: Int,
    refresh: LoadState,
): FeedContentState =
    when {
        itemCount > 0 -> FeedContentState.Content
        refresh is LoadState.Loading -> FeedContentState.Loading
        refresh is LoadState.Error -> FeedContentState.Error
        else -> FeedContentState.Empty
    }
