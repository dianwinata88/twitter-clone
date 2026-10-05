package com.twitterclone.feature.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twitterclone.core.ui.component.TweetCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TweetDetailScreen(
    onBackClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TweetDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect((uiState as? TweetDetailUiState.Loaded)?.snackbarMessage) {
        (uiState as? TweetDetailUiState.Loaded)?.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onSnackbarShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Post") },
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
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val state = uiState) {
                TweetDetailUiState.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                TweetDetailUiState.NotFound ->
                    Text(
                        text = "This post was deleted or doesn't exist.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                    )
                is TweetDetailUiState.Error ->
                    Column(
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "Couldn't load this post.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                        Button(
                            onClick = { viewModel.retry() },
                            modifier = Modifier.padding(top = 16.dp),
                        ) {
                            Text("Retry")
                        }
                    }
                is TweetDetailUiState.Loaded ->
                    Column(Modifier.fillMaxSize()) {
                        TweetCard(
                            tweet = state.tweet,
                            onLikeClick = { viewModel.onLikeClick() },
                            onTweetClick = {},
                            onProfileClick = onProfileClick,
                        )
                        HorizontalDivider()
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Replies coming in phase 2",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
            }
        }
    }
}
