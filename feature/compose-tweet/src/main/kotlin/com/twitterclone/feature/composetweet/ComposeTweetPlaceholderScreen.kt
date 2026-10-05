package com.twitterclone.feature.composetweet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/** Stub compose screen — the compose-tweet feature agent fills this in. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeTweetScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = { CenterAlignedTopAppBar(title = { Text("Compose") }) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = "Compose stub — TweetRepository.postTweet(text, mediaLocalUris)",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
