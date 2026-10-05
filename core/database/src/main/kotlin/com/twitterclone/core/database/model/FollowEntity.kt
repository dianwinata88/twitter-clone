package com.twitterclone.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Composite PK guarantees at most one follow edge per (followerId, followeeId).
 * `followerId` follows `followeeId`.
 */
@Entity(
    tableName = "follows",
    primaryKeys = ["followerId", "followeeId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["followerId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["followeeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("followerId"), Index("followeeId")],
)
data class FollowEntity(
    val followerId: String,
    val followeeId: String,
    val createdAtEpochMillis: Long,
)
