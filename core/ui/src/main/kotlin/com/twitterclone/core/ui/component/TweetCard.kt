package com.twitterclone.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.twitterclone.core.model.Media
import com.twitterclone.core.model.MediaType
import com.twitterclone.core.model.SyncState
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.ui.theme.XGreen
import com.twitterclone.core.ui.theme.XPink
import com.twitterclone.core.ui.util.relativeTime

/**
 * Shared timeline card. This is the contract feature agents should reuse for
 * tweet rendering; keep the signature stable.
 */
@Composable
fun TweetCard(
    tweet: Tweet,
    onLikeClick: () -> Unit,
    onTweetClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onTweetClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        AsyncImage(
            model = tweet.author.avatarUrl,
            contentDescription = "${tweet.author.displayName} avatar",
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick(tweet.author.id) },
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tweet.author.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { onProfileClick(tweet.author.id) },
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "@${tweet.author.username} · ${tweet.createdAt.relativeTime()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = tweet.text,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (tweet.syncState == SyncState.PENDING) {
                Text(
                    text = "Sending…",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (tweet.syncState == SyncState.FAILED) {
                Text(
                    text = "Failed to send",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            val images = tweet.media.filter { it.type == MediaType.IMAGE }
            if (images.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                MediaGrid(images)
            }
            Spacer(Modifier.height(4.dp))
            TweetActionBar(tweet = tweet, onLikeClick = onLikeClick)
        }
    }
}

@Composable
private fun MediaGrid(
    images: List<Media>,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    when (images.size) {
        1 ->
            AsyncImage(
                model = images[0].url,
                contentDescription = images[0].altText,
                contentScale = ContentScale.Crop,
                modifier =
                    modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 2f)
                        .clip(shape),
            )
        else -> {
            val cells = images.take(4)
            Column(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(shape),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                cells.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        rowItems.forEach { media ->
                            AsyncImage(
                                model = media.url,
                                contentDescription = media.altText,
                                contentScale = ContentScale.Crop,
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp)),
                            )
                        }
                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TweetActionBar(
    tweet: Tweet,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActionItem(
            icon = {
                Icon(
                    Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Replies",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            count = tweet.replyCount,
        )
        ActionItem(
            icon = {
                Icon(
                    Icons.Outlined.Repeat,
                    contentDescription = "Reposts",
                    modifier = Modifier.size(18.dp),
                    tint = if (tweet.repostedByMe) XGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            count = tweet.repostCount,
            tint = if (tweet.repostedByMe) XGreen else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onLikeClick, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = if (tweet.likedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (tweet.likedByMe) "Unlike" else "Like",
                    modifier = Modifier.size(18.dp),
                    tint = if (tweet.likedByMe) XPink else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            CountText(
                count = tweet.likeCount,
                tint = if (tweet.likedByMe) XPink else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ActionItem(
            icon = {
                Icon(
                    Icons.Outlined.BarChart,
                    contentDescription = "Views",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            count = tweet.viewCount,
        )
    }
}

@Composable
private fun ActionItem(
    icon: @Composable () -> Unit,
    count: Int,
    tint: Color = Color.Unspecified,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        CountText(count = count, tint = tint)
    }
}

@Composable
private fun CountText(
    count: Int,
    tint: Color = Color.Unspecified,
) {
    if (count > 0) {
        Spacer(Modifier.width(4.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = if (tint == Color.Unspecified) MaterialTheme.colorScheme.onSurfaceVariant else tint,
        )
    }
}
