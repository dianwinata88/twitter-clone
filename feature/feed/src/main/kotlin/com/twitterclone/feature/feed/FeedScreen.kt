package com.twitterclone.feature.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.ui.component.TweetCard

/**
 * Home timeline: paged [TweetCard]s with pull-to-refresh, append/prepend load
 * states, and a snackbar for failed like writes. The X wordmark top bar and
 * compose FAB live in the app shell ([com.twitterclone] MainActivity), so this
 * screen only renders the list body.
 */
@Composable
fun FeedScreen(
    onTweetClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val tweets = viewModel.tweets.collectAsLazyPagingItems()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val likeErrorMessage = stringResource(R.string.feed_like_error)
    LaunchedEffect(uiState.likeError) {
        if (uiState.likeError) {
            snackbarHostState.showSnackbar(likeErrorMessage)
            viewModel.consumeLikeError()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = {
                viewModel.refresh()
                tweets.refresh()
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                when (val refresh = tweets.loadState.refresh) {
                    is LoadState.Loading ->
                        if (tweets.itemCount == 0) {
                            item(key = "refresh-loading") { LoadingRow() }
                        }
                    is LoadState.Error ->
                        if (tweets.itemCount == 0) {
                            item(key = "refresh-error") {
                                ErrorRow(
                                    message =
                                        refresh.error.message
                                            ?: stringResource(R.string.feed_load_error),
                                    onRetry = tweets::retry,
                                )
                            }
                        }
                    is LoadState.NotLoading ->
                        if (tweets.itemCount == 0) {
                            item(key = "empty") { EmptyRow() }
                        }
                }

                when (val prepend = tweets.loadState.prepend) {
                    is LoadState.Loading -> item(key = "prepend-loading") { LoadingRow() }
                    is LoadState.Error ->
                        item(key = "prepend-error") {
                            ErrorRow(
                                message =
                                    prepend.error.message
                                        ?: stringResource(R.string.feed_load_error),
                                onRetry = tweets::retry,
                            )
                        }
                    is LoadState.NotLoading -> Unit
                }

                items(
                    count = tweets.itemCount,
                    key = tweets.itemKey { it.id },
                    contentType = tweets.itemContentType { "tweet" },
                ) { index ->
                    tweets[index]?.let { tweet ->
                        TimelineTweet(
                            tweet = tweet,
                            onTweetClick = onTweetClick,
                            onProfileClick = onProfileClick,
                            onLikeClick = { viewModel.toggleLike(tweet.id) },
                        )
                    }
                }

                when (val append = tweets.loadState.append) {
                    is LoadState.Loading -> item(key = "append-loading") { LoadingRow() }
                    is LoadState.Error ->
                        item(key = "append-error") {
                            ErrorRow(
                                message =
                                    append.error.message
                                        ?: stringResource(R.string.feed_load_error),
                                onRetry = tweets::retry,
                            )
                        }
                    is LoadState.NotLoading -> Unit
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun TimelineTweet(
    tweet: Tweet,
    onTweetClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onLikeClick: () -> Unit,
) {
    Column {
        TweetCard(
            tweet = tweet,
            onLikeClick = onLikeClick,
            onTweetClick = { onTweetClick(tweet.id) },
            onProfileClick = onProfileClick,
        )
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun LoadingRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorRow(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.feed_retry))
        }
    }
}

@Composable
private fun EmptyRow() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.feed_empty_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.feed_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
