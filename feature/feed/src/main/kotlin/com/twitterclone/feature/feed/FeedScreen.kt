package com.twitterclone.feature.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.twitterclone.core.ui.component.TweetCard

/**
 * Placeholder feed wired to the fake timeline so the scaffold runs
 * end-to-end. The feed feature agent replaces this screen's body.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onTweetClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onComposeClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val tweets = viewModel.tweets.collectAsLazyPagingItems()

    Scaffold(
        modifier = modifier,
        topBar = { CenterAlignedTopAppBar(title = { Text("Home") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onComposeClick) {
                Icon(Icons.Filled.Edit, contentDescription = "Compose tweet")
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (tweets.itemCount == 0 && tweets.loadState.refresh !is LoadState.Loading) {
                Text(
                    text = "No tweets yet",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            LazyColumn(Modifier.fillMaxSize()) {
                items(
                    count = tweets.itemCount,
                    key = tweets.itemKey { it.id },
                ) { index ->
                    val tweet = tweets[index]
                    if (tweet != null) {
                        TweetCard(
                            tweet = tweet,
                            onLikeClick = { viewModel.onLikeClick(tweet.id) },
                            onTweetClick = { onTweetClick(tweet.id) },
                            onProfileClick = onProfileClick,
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
