package com.twitterclone.feature.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.twitterclone.core.model.Routes
import com.twitterclone.core.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowListScreen(
    onBackClick: () -> Unit,
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FollowListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.onSnackbarShown()
        }
    }

    val tabs = listOf(Routes.TAB_FOLLOWERS, Routes.TAB_FOLLOWING)
    val selectedIndex = tabs.indexOf(uiState.selectedTab).coerceAtLeast(0)

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Connections") },
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
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedIndex) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = index == selectedIndex,
                        onClick = { viewModel.onTabSelected(tab) },
                        text = {
                            Text(
                                if (tab == Routes.TAB_FOLLOWING) {
                                    "Following"
                                } else {
                                    "Followers"
                                },
                            )
                        },
                    )
                }
            }
            Box(Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading ->
                        CircularProgressIndicator(Modifier.align(Alignment.Center))

                    uiState.visibleUsers.isEmpty() ->
                        Text(
                            text =
                                if (uiState.selectedTab == Routes.TAB_FOLLOWING) {
                                    "Not following anyone yet"
                                } else {
                                    "No followers yet"
                                },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.align(Alignment.Center),
                        )

                    else ->
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(
                                items = uiState.visibleUsers,
                                key = { it.id },
                            ) { user ->
                                FollowListRow(
                                    user = user,
                                    showFollowButton = user.id != uiState.selfId,
                                    onClick = { onUserClick(user.id) },
                                    onFollowClick = { viewModel.onFollowClick(user) },
                                )
                                HorizontalDivider()
                            }
                        }
                }
            }
        }
    }
}

@Composable
private fun FollowListRow(
    user: User,
    showFollowButton: Boolean,
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
            contentDescription = "${user.displayName} avatar",
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
        }
        if (showFollowButton) {
            Spacer(Modifier.width(8.dp))
            if (user.followedByMe) {
                OutlinedButton(onClick = onFollowClick) { Text("Following") }
            } else {
                Button(onClick = onFollowClick) { Text("Follow") }
            }
        }
    }
}
