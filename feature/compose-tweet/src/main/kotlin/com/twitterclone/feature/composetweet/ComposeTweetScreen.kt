package com.twitterclone.feature.composetweet

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage

/** Characters remaining at which X starts showing the numeric counter. */
private const val COUNTER_VISIBLE_THRESHOLD = 20

/**
 * Modal-style composer for [com.twitterclone.core.model.Routes.COMPOSE].
 * Top bar: close + Post. Body: avatar + free-form text, attached-image strip,
 * and a bottom action row (photo picker + 280-char counter ring).
 */
@Composable
fun ComposeTweetScreen(
    navController: NavController,
    viewModel: ComposeTweetViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val sendFailedMessage = stringResource(R.string.composetweet_send_failed)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ComposeTweetEvent.TweetPosted -> navController.popBackStack()
                ComposeTweetEvent.PostFailed -> snackbarHostState.showSnackbar(sendFailedMessage)
            }
        }
    }

    val pickMedia =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(MAX_MEDIA_ITEMS),
        ) { uris ->
            viewModel.onMediaSelected(uris.map { it.toString() })
        }

    ComposeTweetContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onClose = { navController.popBackStack() },
        onTextChange = viewModel::onTextChange,
        onPickMedia = {
            pickMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        onRemoveMedia = viewModel::onRemoveMedia,
        onTweet = viewModel::onTweetClicked,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeTweetContent(
    uiState: ComposeTweetUiState,
    snackbarHostState: SnackbarHostState,
    onClose: () -> Unit,
    onTextChange: (String) -> Unit,
    onPickMedia: () -> Unit,
    onRemoveMedia: (String) -> Unit,
    onTweet: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.composetweet_close),
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = onTweet,
                        enabled = uiState.canTweet,
                        shape = RoundedCornerShape(percent = 50),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            ),
                        modifier =
                            Modifier
                                .padding(end = 12.dp)
                                .height(36.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.composetweet_post),
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
        bottomBar = {
            ComposerActionBar(
                uiState = uiState,
                onPickMedia = onPickMedia,
            )
        },
    ) { innerPadding ->
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            AsyncImage(
                model = uiState.avatarUrl,
                contentDescription = stringResource(R.string.composetweet_avatar),
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                TextField(
                    value = uiState.text,
                    onValueChange = onTextChange,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.composetweet_placeholder),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    textStyle = MaterialTheme.typography.titleLarge,
                    colors =
                        TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (uiState.mediaUris.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    MediaThumbnailRow(
                        mediaUris = uiState.mediaUris,
                        onRemoveMedia = onRemoveMedia,
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaThumbnailRow(
    mediaUris: List<String>,
    onRemoveMedia: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(mediaUris, key = { it }) { uri ->
            Box {
                AsyncImage(
                    model = uri,
                    contentDescription = stringResource(R.string.composetweet_attached_media),
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(12.dp)),
                )
                IconButton(
                    onClick = { onRemoveMedia(uri) },
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f)),
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.composetweet_remove_media),
                        tint = Color.White,
                        modifier =
                            Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .padding(2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ComposerActionBar(
    uiState: ComposeTweetUiState,
    onPickMedia: () -> Unit,
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPickMedia, enabled = uiState.canAddMedia) {
                Icon(
                    Icons.Outlined.Image,
                    contentDescription = stringResource(R.string.composetweet_add_media),
                    tint =
                        if (uiState.canAddMedia) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        },
                )
            }
            Spacer(Modifier.weight(1f))
            CharacterCounter(uiState = uiState)
        }
    }
}

@Composable
private fun CharacterCounter(uiState: ComposeTweetUiState) {
    val hasText = uiState.text.isNotEmpty()
    if (!hasText) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (uiState.charactersRemaining <= COUNTER_VISIBLE_THRESHOLD) {
            Text(
                text = uiState.charactersRemaining.toString(),
                style = MaterialTheme.typography.labelMedium,
                color =
                    if (uiState.isOverLimit) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
            Spacer(Modifier.width(8.dp))
        }
        CircularProgressIndicator(
            progress = { (uiState.text.length.toFloat() / MAX_TWEET_LENGTH).coerceAtMost(1f) },
            modifier = Modifier.size(24.dp),
            color =
                if (uiState.isOverLimit) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 2.5.dp,
        )
        Spacer(Modifier.width(8.dp))
    }
}
