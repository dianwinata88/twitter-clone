package com.twitterclone.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Tweet joined with its author, media, and viewer-scoped state
 * (`likedByMe`, `repostedByMe` come from SELECT expressions in the DAO query).
 */
data class HydratedTweet(
    @Embedded val tweet: TweetEntity,
    @Relation(parentColumn = "authorId", entityColumn = "id")
    val author: UserEntity,
    @Relation(parentColumn = "id", entityColumn = "tweetId")
    val media: List<MediaEntity>,
    val likedByMe: Boolean,
    val repostedByMe: Boolean,
)
