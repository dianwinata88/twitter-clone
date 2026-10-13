package com.twitterclone.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
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
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.model.User
import com.twitterclone.core.ui.component.TweetCard
import com.twitterclone.feature.profile.component.FollowButton
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val HeaderHeight = 140.dp
private val AvatarSize = 80.dp

/**
 * Profile destination content. Used by both `PROFILE/{userId}` and the
 * `PROFILE_ME` alias — [showBackButton] is false only for the bottom-nav
 * "me" tab, where the app shell already supplies top chrome.
 */
@Composable
fun ProfileRoute(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onTweetClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onFollowersClick: (String) -> Unit,
    onFollowingClick: (String) -> Unit,
    onLoginClick: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tweets = viewModel.tweets.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    val actionFailed = stringResource(R.string.profile_action_failed)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.LoggedOut -> onLoggedOut()
                is ProfileEvent.ProfileSaved -> showEditDialog = false
                is ProfileEvent.Error ->
                    snackbarHostState.showSnackbar(event.message ?: actionFailed)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        when {
            uiState.isLoading ->
                CircularProgressIndicator(Modifier.align(Alignment.Center))

            uiState.signedOut ->
                SignedOutContent(onLoginClick = onLoginClick, modifier = Modifier.align(Alignment.Center))

            uiState.loadFailed || uiState.user == null ->
                LoadFailedContent(
                    onBackClick = onBackClick,
                    onRetryClick = viewModel::reload,
                    showBackButton = showBackButton,
                )

            else ->
                ProfileContent(
                    user = uiState.user!!,
                    isOwnProfile = uiState.isOwnProfile,
                    followBusy = uiState.followBusy,
                    tweets = tweets,
                    showBackButton = showBackButton,
                    onBackClick = onBackClick,
                    onEditClick = { showEditDialog = true },
                    onLogoutClick = viewModel::logout,
                    onFollowClick = viewModel::toggleFollow,
                    onFollowersClick = onFollowersClick,
                    onFollowingClick = onFollowingClick,
                    onTweetClick = onTweetClick,
                    onProfileClick = onProfileClick,
                    onLikeClick = viewModel::toggleLike,
                )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (showEditDialog) {
        EditProfileDialog(
            initialDisplayName = uiState.user?.displayName.orEmpty(),
            initialBio = uiState.user?.bio.orEmpty(),
            saving = uiState.editSaving,
            onDismiss = { showEditDialog = false },
            onSave = viewModel::updateProfile,
        )
    }
}

@Composable
private fun SignedOutContent(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.profile_signed_out),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onLoginClick) {
            Text(stringResource(R.string.profile_log_in))
        }
    }
}

@Composable
private fun LoadFailedContent(
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    showBackButton: Boolean,
) {
    Column(Modifier.fillMaxSize()) {
        if (showBackButton) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.profile_back),
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.profile_load_failed),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onRetryClick) {
                Text(stringResource(R.string.profile_retry))
            }
        }
    }
}

@Composable
private fun ProfileContent(
    user: User,
    isOwnProfile: Boolean,
    followBusy: Boolean,
    tweets: LazyPagingItems<Tweet>,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onFollowClick: () -> Unit,
    onFollowersClick: (String) -> Unit,
    onFollowingClick: (String) -> Unit,
    onTweetClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize()) {
        if (showBackButton) {
            item(key = "top_bar") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.profile_back),
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = user.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text =
                                pluralStringResource(
                                    R.plurals.profile_posts_count,
                                    user.tweetCount,
                                    user.tweetCount,
                                ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        item(key = "header") {
            ProfileHeader(
                user = user,
                isOwnProfile = isOwnProfile,
                followBusy = followBusy,
                onEditClick = onEditClick,
                onLogoutClick = onLogoutClick,
                onFollowClick = onFollowClick,
            )
        }

        item(key = "info") {
            ProfileInfo(
                user = user,
                onFollowersClick = onFollowersClick,
                onFollowingClick = onFollowingClick,
            )
        }

        item(key = "posts_tab") {
            Column {
                Text(
                    text = stringResource(R.string.profile_posts_tab),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        }

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
            }
        }

        item(key = "paging_footer") {
            PagingFooter(tweets = tweets)
        }
    }
}

@Composable
private fun ProfileHeader(
    user: User,
    isOwnProfile: Boolean,
    followBusy: Boolean,
    onEditClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onFollowClick: () -> Unit,
) {
    // The avatar's bottom half overflows the header image into the info
    // section, X-style; the action button aligns with its bottom edge.
    Box(Modifier.fillMaxWidth().height(HeaderHeight + AvatarSize / 2)) {
        if (user.headerUrl != null) {
            AsyncImage(
                model = user.headerUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(HeaderHeight)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        } else {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(HeaderHeight)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
        AsyncImage(
            model = user.avatarUrl,
            contentDescription = "${user.displayName} avatar",
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp)
                    .size(AvatarSize)
                    .clip(CircleShape)
                    .border(3.dp, Color.Black, CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Row(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isOwnProfile) {
                OutlinedAction(text = stringResource(R.string.profile_edit), onClick = onEditClick)
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onLogoutClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = stringResource(R.string.profile_log_out),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                FollowButton(
                    followedByMe = user.followedByMe,
                    enabled = !followBusy,
                    onClick = onFollowClick,
                )
            }
        }
    }
}

@Composable
private fun OutlinedAction(
    text: String,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier =
            Modifier
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ProfileInfo(
    user: User,
    onFollowersClick: (String) -> Unit,
    onFollowingClick: (String) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = user.displayName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "@${user.username}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (user.bio.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(text = user.bio, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.profile_joined, user.createdAt.joinMonth(), user.createdAt.joinYear()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            CountLabel(
                count = user.followingCount,
                label = stringResource(R.string.profile_following_label),
                onClick = { onFollowingClick(user.id) },
            )
            Spacer(Modifier.width(16.dp))
            CountLabel(
                count = user.followerCount,
                label = stringResource(R.string.profile_followers_label),
                onClick = { onFollowersClick(user.id) },
            )
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun CountLabel(
    count: Int,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PagingFooter(tweets: LazyPagingItems<Tweet>) {
    when {
        tweets.loadState.refresh is LoadState.Loading ->
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(24.dp))
            }

        tweets.loadState.append is LoadState.Loading ->
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(24.dp))
            }

        tweets.loadState.refresh is LoadState.Error || tweets.loadState.append is LoadState.Error ->
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                TextButton(onClick = tweets::retry) {
                    Text(stringResource(R.string.profile_retry))
                }
            }
    }
}

private fun Instant.joinMonth(): String =
    toLocalDateTime(TimeZone.currentSystemDefault())
        .month.name
        .lowercase()
        .replaceFirstChar { it.uppercase() }

private fun Instant.joinYear(): Int = toLocalDateTime(TimeZone.currentSystemDefault()).year
