package com.twitterclone.feature.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.twitterclone.core.model.MediaType
import com.twitterclone.core.model.Tweet
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val LikePink = Color(0xFFF91880)

/** Full-tweet screen: larger body/media, stats row, action row, back nav. */
@Composable
fun TweetDetailScreen(
    onBack: () -> Unit,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TweetDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val likeErrorMessage = stringResource(R.string.feed_like_error)
    LaunchedEffect(uiState.likeError) {
        if (uiState.likeError) {
            snackbarHostState.showSnackbar(likeErrorMessage)
            viewModel.consumeLikeError()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tweet_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.tweet_detail_back),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when {
                uiState.isLoading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.tweet != null ->
                    TweetDetailContent(
                        tweet = uiState.tweet!!,
                        onProfileClick = onProfileClick,
                        onLikeClick = viewModel::toggleLike,
                    )
                else ->
                    TweetDetailError(
                        notFound = uiState.notFound,
                        onRetry = viewModel::reload,
                        modifier = Modifier.align(Alignment.Center),
                    )
            }
        }
    }
}

@Composable
private fun TweetDetailError(
    notFound: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text =
                stringResource(
                    if (notFound) {
                        R.string.tweet_detail_not_found
                    } else {
                        R.string.feed_load_error
                    },
                ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!notFound) {
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.feed_retry))
            }
        }
    }
}

@Composable
private fun TweetDetailContent(
    tweet: Tweet,
    onProfileClick: (String) -> Unit,
    onLikeClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { onProfileClick(tweet.author.id) }
                    .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = tweet.author.avatarUrl,
                contentDescription = "${tweet.author.displayName} avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(CircleShape),
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = tweet.author.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "@${tweet.author.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text = tweet.text,
            style = MaterialTheme.typography.titleLarge,
        )

        val images = tweet.media.filter { it.type == MediaType.IMAGE }
        if (images.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            images.forEach { media ->
                AsyncImage(
                    model = media.localUri ?: media.url,
                    contentDescription = media.altText,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(16.dp)),
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text =
                "${tweet.createdAt.fullTimestamp()} · " +
                    "${tweet.viewCount} ${stringResource(R.string.tweet_detail_stat_views)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Stat(tweet.replyCount, stringResource(R.string.tweet_detail_stat_replies))
            Stat(tweet.repostCount, stringResource(R.string.tweet_detail_stat_reposts))
            Stat(tweet.likeCount, stringResource(R.string.tweet_detail_stat_likes))
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { }) {
                Icon(
                    Icons.Outlined.ChatBubbleOutline,
                    contentDescription = stringResource(R.string.tweet_detail_replies),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { }) {
                Icon(
                    Icons.Outlined.Repeat,
                    contentDescription = stringResource(R.string.tweet_detail_reposts),
                    tint =
                        if (tweet.repostedByMe) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }
            IconButton(onClick = onLikeClick) {
                Icon(
                    imageVector =
                        if (tweet.likedByMe) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Outlined.FavoriteBorder
                        },
                    contentDescription =
                        stringResource(
                            if (tweet.likedByMe) {
                                R.string.tweet_detail_unlike
                            } else {
                                R.string.tweet_detail_like
                            },
                        ),
                    tint =
                        if (tweet.likedByMe) {
                            LikePink
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }
            IconButton(onClick = { }) {
                Icon(
                    Icons.Outlined.BookmarkBorder,
                    contentDescription = stringResource(R.string.tweet_detail_bookmark),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { }) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = stringResource(R.string.tweet_detail_share),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Stat(
    count: Int,
    label: String,
) {
    Row {
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

/** `h:mm a · MMM d, yyyy` in the local zone — the full stamp X shows on detail. */
private fun Instant.fullTimestamp(): String {
    val dt = toLocalDateTime(TimeZone.currentSystemDefault())
    val hour24 = dt.hour
    val amPm = if (hour24 < 12) "AM" else "PM"
    val hour12 = hour24 % 12.let { if (it == 0) 12 else it }
    val minute = dt.minute.toString().padStart(2, '0')
    val month = MONTHS[dt.monthNumber - 1]
    return "$hour12:$minute $amPm · $month ${dt.dayOfMonth}, ${dt.year}"
}

private val MONTHS =
    listOf(
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec",
    )
