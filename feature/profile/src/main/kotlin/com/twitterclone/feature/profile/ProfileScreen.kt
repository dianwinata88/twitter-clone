package com.twitterclone.feature.profile

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.twitterclone.core.model.Routes
import com.twitterclone.core.model.User
import com.twitterclone.core.ui.component.TweetCard
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onTweetClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onFollowListClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tweets = viewModel.tweets.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.onSnackbarShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(uiState.user?.displayName ?: "Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                uiState.userNotFound || uiState.user == null ->
                    Text(
                        text = "User not found",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center),
                    )

                else -> {
                    val user = uiState.user!!
                    LazyColumn(Modifier.fillMaxSize()) {
                        item {
                            ProfileHeader(
                                user = user,
                                isSelf = uiState.isSelf,
                                onFollowClick = viewModel::onFollowClick,
                                onEditClick = { showEditDialog = true },
                                onFollowListClick = onFollowListClick,
                            )
                            HorizontalDivider()
                        }
                        if (tweets.itemCount == 0 &&
                            tweets.loadState.refresh !is LoadState.Loading
                        ) {
                            item {
                                Text(
                                    text = "No tweets yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp),
                                )
                            }
                        }
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
    }

    val editingUser = uiState.user
    if (showEditDialog && editingUser != null) {
        EditProfileDialog(
            initialDisplayName = editingUser.displayName,
            initialBio = editingUser.bio,
            isSaving = uiState.isUpdatingProfile,
            onDismiss = { showEditDialog = false },
            onSave = { name, bio ->
                viewModel.updateProfile(name, bio)
                showEditDialog = false
            },
        )
    }
}

@Composable
private fun ProfileHeader(
    user: User,
    isSelf: Boolean,
    onFollowClick: () -> Unit,
    onEditClick: () -> Unit,
    onFollowListClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(180.dp)) {
            AsyncImage(
                model = user.headerUrl,
                contentDescription = "${user.displayName} header",
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = "${user.displayName} avatar",
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp)
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Spacer(Modifier.weight(1f))
            if (isSelf) {
                OutlinedButton(onClick = onEditClick) { Text("Edit profile") }
            } else if (user.followedByMe) {
                OutlinedButton(onClick = onFollowClick) { Text("Following") }
            } else {
                Button(onClick = onFollowClick) { Text("Follow") }
            }
        }
        Column(Modifier.padding(horizontal = 16.dp)) {
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
                    Icons.Outlined.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = user.createdAt.joinedLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(8.dp))
            Row {
                CountLink(
                    count = user.followingCount,
                    label = "Following",
                    onClick = { onFollowListClick(user.id, Routes.TAB_FOLLOWING) },
                )
                Spacer(Modifier.width(16.dp))
                CountLink(
                    count = user.followerCount,
                    label = "Followers",
                    onClick = { onFollowListClick(user.id, Routes.TAB_FOLLOWERS) },
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CountLink(
    count: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.clickable(onClick = onClick)) {
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

private fun Instant.joinedLabel(): String {
    val date = toLocalDateTime(TimeZone.UTC)
    val month =
        date.month.name
            .lowercase()
            .replaceFirstChar { it.titlecase() }
    return "Joined $month ${date.year}"
}
