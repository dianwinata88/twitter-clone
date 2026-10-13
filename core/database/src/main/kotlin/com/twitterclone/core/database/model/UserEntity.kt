package com.twitterclone.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)],
)
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val bio: String,
    val avatarUrl: String?,
    val headerUrl: String?,
    val followerCount: Int,
    val followingCount: Int,
    val tweetCount: Int,
    val createdAtEpochMillis: Long,
)
