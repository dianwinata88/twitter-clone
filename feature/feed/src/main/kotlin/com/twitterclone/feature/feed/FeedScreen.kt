package com.twitterclone.feature.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.twitterclone.core.ui.component.TweetCard
import com.twitterclone.core.ui.theme.XBlack
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onTweetClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onComposeClick: () -> Unit,
    onMyProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val tweets = viewModel.tweets.collectAsLazyPagingItems()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val comingSoon: (String) -> Unit = { label ->
        scope.launch { snackbarHostState.showSnackbar("$label coming soon") }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onSnackbarShown()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = XBlack,
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "X",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    },
                    colors =
                        TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = XBlack,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                )
                PrimaryTabRow(
                    selectedTabIndex = 0,
                    containerColor = XBlack,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(0),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    divider = {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    },
                ) {
                    Tab(
                        selected = true,
                        onClick = {},
                        selectedContentColor = MaterialTheme.colorScheme.onSurface,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                "For you",
                                style = MaterialTheme.typography.titleSmall,
                            )
                        },
                    )
                    Tab(
                        selected = false,
                        onClick = {},
                        enabled = false,
                        selectedContentColor = MaterialTheme.colorScheme.onSurface,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                "Following",
                                style = MaterialTheme.typography.titleSmall,
                            )
                        },
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onComposeClick,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Compose tweet")
            }
        },
        bottomBar = {
            NavigationBar(containerColor = XBlack) {
                val itemColors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = Color.Transparent,
                    )
                NavigationBarItem(
                    selected = true,
                    onClick = { scope.launch { listState.animateScrollToItem(0) } },
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                    colors = itemColors,
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { comingSoon("Search") },
                    icon = { Icon(Icons.Outlined.Search, contentDescription = "Search") },
                    colors = itemColors,
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { comingSoon("Notifications") },
                    icon = { Icon(Icons.Outlined.Notifications, contentDescription = "Alerts") },
                    colors = itemColors,
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { comingSoon("Messages") },
                    icon = { Icon(Icons.Outlined.MailOutline, contentDescription = "Messages") },
                    colors = itemColors,
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onMyProfileClick,
                    icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
                    colors = itemColors,
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val isRefreshing =
            uiState.isRefreshing ||
                (tweets.loadState.refresh is LoadState.Loading && tweets.itemCount > 0)
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                viewModel.onRefresh()
                tweets.refresh()
            },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            when (feedContentState(tweets.itemCount, tweets.loadState.refresh)) {
                FeedContentState.Loading -> LoadingContent(Modifier.fillMaxSize())
                FeedContentState.Empty -> EmptyContent(Modifier.fillMaxSize())
                FeedContentState.Error ->
                    ErrorContent(
                        message = "Couldn't load your timeline.",
                        onRetry = { tweets.retry() },
                        modifier = Modifier.fillMaxSize(),
                    )
                FeedContentState.Content ->
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        items(
                            count = tweets.itemCount,
                            key = tweets.itemKey { it.id },
                            contentType =
                                tweets.itemContentType { tweet ->
                                    if (tweet.media.isEmpty()) "tweet" else "tweet_media"
                                },
                        ) { index ->
                            tweets[index]?.let { tweet ->
                                Column {
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
                        if (tweets.loadState.append is LoadState.Loading) {
                            item(key = "append_loading", contentType = "loading_indicator") {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        if (tweets.loadState.append is LoadState.Error) {
                            item(key = "append_error", contentType = "error_item") {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Couldn't load more tweets",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    TextButton(onClick = { tweets.retry() }) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyContent(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Text(
            text = "No tweets yet — follow someone",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp),
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
            Text("Retry")
        }
    }
}
