package com.twitterclone.core.data.mapper

import com.twitterclone.core.database.model.HydratedTweet
import com.twitterclone.core.database.model.MediaEntity
import com.twitterclone.core.database.model.UserEntity
import com.twitterclone.core.model.Media
import com.twitterclone.core.model.MediaType
import com.twitterclone.core.model.SyncState
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.model.TweetType
import com.twitterclone.core.model.User
import kotlinx.datetime.Instant

internal fun UserEntity.toModel(followedByMe: Boolean = false): User =
    User(
        id = id,
        username = username,
        displayName = displayName,
        bio = bio,
        avatarUrl = avatarUrl,
        headerUrl = headerUrl,
        followerCount = followerCount,
        followingCount = followingCount,
        tweetCount = tweetCount,
        createdAt = Instant.fromEpochMilliseconds(createdAtEpochMillis),
        followedByMe = followedByMe,
    )

internal fun MediaEntity.toModel(): Media =
    Media(
        id = id,
        type = MediaType.valueOf(type),
        url = url,
        localUri = localUri,
        width = width,
        height = height,
        altText = altText,
    )

internal fun HydratedTweet.toModel(): Tweet =
    Tweet(
        id = tweet.id,
        author = author.toModel(),
        text = tweet.text,
        media = media.map { it.toModel() },
        createdAt = Instant.fromEpochMilliseconds(tweet.createdAtEpochMillis),
        replyCount = tweet.replyCount,
        repostCount = tweet.repostCount,
        likeCount = tweet.likeCount,
        viewCount = tweet.viewCount,
        likedByMe = likedByMe,
        repostedByMe = repostedByMe,
        type = TweetType.valueOf(tweet.type),
        inReplyToTweetId = tweet.inReplyToTweetId,
        conversationId = tweet.conversationId,
        syncState = SyncState.valueOf(tweet.syncState),
    )
