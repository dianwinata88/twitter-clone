package com.twitterclone.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.twitterclone.core.database.model.RemoteKeysEntity

@Dao
interface RemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(keys: List<RemoteKeysEntity>)

    @Query("SELECT * FROM remote_keys WHERE tweetId = :tweetId")
    suspend fun byTweetId(tweetId: String): RemoteKeysEntity?

    @Query("DELETE FROM remote_keys")
    suspend fun clear()
}
