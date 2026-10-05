package com.twitterclone.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.twitterclone.core.database.model.MediaEntity

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(media: List<MediaEntity>)

    @Query("SELECT * FROM media WHERE tweetId = :tweetId")
    suspend fun forTweet(tweetId: String): List<MediaEntity>
}
