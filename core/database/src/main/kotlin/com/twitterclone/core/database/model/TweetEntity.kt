package com.twitterclone.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tweets",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("authorId"), Index("createdAtEpochMillis"), Index("conversationId")],
)
data class TweetEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val text: String,
    val createdAtEpochMillis: Long,
    val replyCount: Int,
    val repostCount: Int,
    val likeCount: Int,
    val viewCount: Int,
    val type: String,
    val inReplyToTweetId: String?,
    val conversationId: String?,
    val syncState: String,
)
