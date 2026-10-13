package com.twitterclone.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "media",
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
data class MediaEntity(
    @PrimaryKey val id: String,
    val tweetId: String,
    val type: String,
    val url: String,
    val localUri: String?,
    val width: Int,
    val height: Int,
    val altText: String?,
)
