package com.twitterclone.feature.feed

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

/** Stub tweet detail — the feed feature agent fills this in. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TweetDetailScreen(
    tweetId: String,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { CenterAlignedTopAppBar(title = { Text("Tweet") }) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = "Tweet detail stub — tweetId: $tweetId",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
