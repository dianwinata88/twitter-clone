package com.twitterclone.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.twitterclone.core.common.result.Async
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.model.User
import com.twitterclone.core.ui.component.TweetCard

/**
 * X Dark search screen: a search field pinned on top, the trending section
 * while the query is blank, and Posts/People result tabs once text is
 * entered.
 */
@Composable
fun SearchScreen(
    onTweetClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val trending by viewModel.trending.collectAsStateWithLifecycle()
    val followOverrides by viewModel.followOverrides.collectAsStateWithLifecycle()
    val tweets = viewModel.tweets.collectAsLazyPagingItems()
    val users = viewModel.users.collectAsLazyPagingItems()

    Column(modifier = modifier.fillMaxSize()) {
        SearchField(
            query = query,
            onQueryChange = viewModel::onQueryChange,
        )
        if (query.isBlank()) {
            TrendingContent(
                trending = trending,
                onTweetClick = onTweetClick,
                onProfileClick = onProfileClick,
                onLikeClick = viewModel::onLikeClick,
            )
        } else {
            SearchTabRow(
                selectedTab = selectedTab,
                onTabSelected = viewModel::onTabSelected,
            )
            when (selectedTab) {
                SearchTab.POSTS ->
                    PostResults(
                        tweets = tweets,
                        query = query.trim(),
                        onTweetClick = onTweetClick,
                        onProfileClick = onProfileClick,
                        onLikeClick = viewModel::onLikeClick,
                    )

                SearchTab.PEOPLE ->
                    PeopleResults(
                        users = users,
                        query = query.trim(),
                        followOverrides = followOverrides,
                        onProfileClick = onProfileClick,
                        onFollowClick = viewModel::onFollowClick,
                    )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.search_clear),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        singleLine = true,
        shape = CircleShape,
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
    )
}

@Composable
private fun TrendingContent(
    trending: Async<List<Tweet>>,
    onTweetClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.search_trending_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        when (trending) {
            is Async.Loading -> LoadingContent()
            is Async.Error ->
                CenteredMessage(
                    text = stringResource(R.string.search_trending_error),
                )

            is Async.Success -> {
                if (trending.data.isEmpty()) {
                    CenteredMessage(
                        text = stringResource(R.string.search_trending_empty),
                    )
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(
                            count = trending.data.size,
                            key = { index -> trending.data[index].id },
                        ) { index ->
                            val tweet = trending.data[index]
                            TweetCard(
                                tweet = tweet,
                                onLikeClick = { onLikeClick(tweet.id) },
                                onTweetClick = { onTweetClick(tweet.id) },
                                onProfileClick = onProfileClick,
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchTabRow(
    selectedTab: SearchTab,
    onTabSelected: (SearchTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    PrimaryTabRow(
        selectedTabIndex = selectedTab.ordinal,
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        Tab(
            selected = selectedTab == SearchTab.POSTS,
            onClick = { onTabSelected(SearchTab.POSTS) },
            text = {
                Text(
                    text = stringResource(R.string.search_tab_posts),
                    fontWeight = if (selectedTab == SearchTab.POSTS) FontWeight.Bold else FontWeight.Normal,
                )
            },
        )
        Tab(
            selected = selectedTab == SearchTab.PEOPLE,
            onClick = { onTabSelected(SearchTab.PEOPLE) },
            text = {
                Text(
                    text = stringResource(R.string.search_tab_people),
                    fontWeight = if (selectedTab == SearchTab.PEOPLE) FontWeight.Bold else FontWeight.Normal,
                )
            },
        )
    }
}

@Composable
private fun PostResults(
    tweets: LazyPagingItems<Tweet>,
    query: String,
    onTweetClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            tweets.loadState.refresh is LoadState.Loading -> LoadingContent()
            tweets.loadState.refresh is LoadState.Error ->
                CenteredMessage(text = stringResource(R.string.search_results_error))

            tweets.itemCount == 0 ->
                CenteredMessage(
                    text = stringResource(R.string.search_posts_empty, query),
                )

            else ->
                LazyColumn(Modifier.fillMaxSize()) {
                    items(
                        count = tweets.itemCount,
                        key = tweets.itemKey { it.id },
                    ) { index ->
                        tweets[index]?.let { tweet ->
                            TweetCard(
                                tweet = tweet,
                                onLikeClick = { onLikeClick(tweet.id) },
                                onTweetClick = { onTweetClick(tweet.id) },
                                onProfileClick = onProfileClick,
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                    if (tweets.loadState.append is LoadState.Loading) {
                        item { AppendLoadingRow() }
                    }
                }
        }
    }
}

@Composable
private fun PeopleResults(
    users: LazyPagingItems<User>,
    query: String,
    followOverrides: Map<String, Boolean>,
    onProfileClick: (String) -> Unit,
    onFollowClick: (User) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            users.loadState.refresh is LoadState.Loading -> LoadingContent()
            users.loadState.refresh is LoadState.Error ->
                CenteredMessage(text = stringResource(R.string.search_results_error))

            users.itemCount == 0 ->
                CenteredMessage(
                    text = stringResource(R.string.search_people_empty, query),
                )

            else ->
                LazyColumn(Modifier.fillMaxSize()) {
                    items(
                        count = users.itemCount,
                        key = users.itemKey { it.id },
                    ) { index ->
                        users[index]?.let { user ->
                            UserRow(
                                user = user,
                                followedByMe = followOverrides[user.id] ?: user.followedByMe,
                                onClick = { onProfileClick(user.id) },
                                onFollowClick = { onFollowClick(user) },
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                    if (users.loadState.append is LoadState.Loading) {
                        item { AppendLoadingRow() }
                    }
                }
        }
    }
}

@Composable
private fun UserRow(
    user: User,
    followedByMe: Boolean,
    onClick: () -> Unit,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = user.avatarUrl,
            contentDescription = stringResource(R.string.search_user_avatar, user.displayName),
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(44.dp)
                    .clip(CircleShape),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (user.bio.isNotBlank()) {
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        FollowButton(followedByMe = followedByMe, onClick = onFollowClick)
    }
}

@Composable
private fun FollowButton(
    followedByMe: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
    if (followedByMe) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            shape = CircleShape,
            contentPadding = contentPadding,
        ) {
            Text(
                text = stringResource(R.string.search_following),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = CircleShape,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                ),
            contentPadding = contentPadding,
        ) {
            Text(
                text = stringResource(R.string.search_follow),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun AppendLoadingRow(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun CenteredMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
