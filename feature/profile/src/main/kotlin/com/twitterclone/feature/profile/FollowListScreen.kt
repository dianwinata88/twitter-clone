package com.twitterclone.feature.profile

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.twitterclone.core.model.User
import com.twitterclone.feature.profile.component.FollowButton

/** Which follow list a `FollowListRoute` renders. */
enum class FollowListType {
    Followers,
    Following,
}

/**
 * Followers/following destination: back arrow + owner name header, then a
 * paged list of user rows each with a follow toggle (hidden on your own row).
 */
@Composable
fun FollowListRoute(
    listType: FollowListType,
    onBackClick: () -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: FollowListViewModel = hiltViewModel(),
) {
    val owner by viewModel.owner.collectAsStateWithLifecycle()
    val viewer by viewModel.viewer.collectAsStateWithLifecycle()
    val pendingFollowIds by viewModel.pendingFollowIds.collectAsStateWithLifecycle()
    val users =
        when (listType) {
            FollowListType.Followers -> viewModel.followers
            FollowListType.Following -> viewModel.following
        }.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    val actionFailed = stringResource(R.string.profile_action_failed)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is FollowListEvent.Error ->
                    snackbarHostState.showSnackbar(event.message ?: actionFailed)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
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
                        text =
                            owner?.displayName
                                ?: stringResource(
                                    when (listType) {
                                        FollowListType.Followers -> R.string.follow_list_followers_title
                                        FollowListType.Following -> R.string.follow_list_following_title
                                    },
                                ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    owner?.let {
                        Text(
                            text = "@${it.username}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            LazyColumn(Modifier.fillMaxSize()) {
                items(
                    count = users.itemCount,
                    key = users.itemKey { it.id },
                ) { index ->
                    users[index]?.let { user ->
                        UserRow(
                            user = user,
                            isViewer = user.id == viewer?.id,
                            followPending = user.id in pendingFollowIds,
                            onClick = { onUserClick(user.id) },
                            onFollowClick = { viewModel.toggleFollow(user.id) },
                        )
                    }
                }

                item(key = "paging_state") {
                    when {
                        users.loadState.refresh is LoadState.Loading ||
                            users.loadState.append is LoadState.Loading ->
                            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(24.dp))
                            }

                        users.loadState.refresh is LoadState.Error ||
                            users.loadState.append is LoadState.Error ->
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                TextButton(onClick = users::retry) {
                                    Text(stringResource(R.string.profile_retry))
                                }
                            }

                        users.itemCount == 0 ->
                            Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = stringResource(R.string.follow_list_empty),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                    }
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
private fun UserRow(
    user: User,
    isViewer: Boolean,
    followPending: Boolean,
    onClick: () -> Unit,
    onFollowClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = user.avatarUrl,
            contentDescription = "${user.displayName} avatar",
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(44.dp)
                    .clip(CircleShape),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (user.bio.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (!isViewer) {
            Spacer(Modifier.width(12.dp))
            FollowButton(
                followedByMe = user.followedByMe,
                enabled = !followPending,
                onClick = onFollowClick,
            )
        }
    }
}
