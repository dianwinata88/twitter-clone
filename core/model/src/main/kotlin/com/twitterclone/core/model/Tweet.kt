package com.twitterclone.core.model

import kotlinx.datetime.Instant

enum class SyncState { PENDING, CONFIRMED, FAILED }

enum class TweetType { ORIGINAL, REPLY, REPOST, QUOTE } // MVP uses ORIGINAL only; reserved

data class Media(
    val id: String,
    val type: MediaType,
    val url: String,
    val localUri: String? = null,
    val width: Int,
    val height: Int,
    val altText: String? = null,
)

enum class MediaType { IMAGE, GIF, VIDEO } // MVP uses IMAGE

data class Tweet(
    val id: String,
    val author: User,
    val text: String,
    val media: List<Media>,
    val createdAt: Instant,
    val replyCount: Int,
    val repostCount: Int,
    val likeCount: Int,
    val viewCount: Int = 0,
    val likedByMe: Boolean,
    val repostedByMe: Boolean = false,
    val type: TweetType = TweetType.ORIGINAL,
    val inReplyToTweetId: String? = null,
    val conversationId: String? = null,
    val syncState: SyncState = SyncState.CONFIRMED,
)
