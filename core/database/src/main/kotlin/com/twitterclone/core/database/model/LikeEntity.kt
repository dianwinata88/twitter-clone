package com.twitterclone.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Composite PK guarantees at most one like per (userId, tweetId). */
@Entity(
    tableName = "likes",
    primaryKeys = ["userId", "tweetId"],
    foreignKeys = [
        ForeignKey(
            entity = TweetEntity::class,
            parentColumns = ["id"],
            childColumns = ["tweetId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("tweetId")],
)
data class LikeEntity(
    val userId: String,
    val tweetId: String,
    val createdAtEpochMillis: Long,
)
