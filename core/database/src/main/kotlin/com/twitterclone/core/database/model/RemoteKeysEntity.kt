package com.twitterclone.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Reserved for a future [androidx.paging.RemoteMediator] once a real backend exists. */
@Entity(tableName = "remote_keys")
data class RemoteKeysEntity(
    @PrimaryKey val tweetId: String,
    val prevKey: String?,
    val nextKey: String?,
)
