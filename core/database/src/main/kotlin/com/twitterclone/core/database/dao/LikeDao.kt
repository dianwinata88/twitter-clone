package com.twitterclone.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.twitterclone.core.database.model.LikeEntity

@Dao
interface LikeDao {
    /** @return the new row id, or -1 when the row already existed (unique constraint). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(like: LikeEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(likes: List<LikeEntity>)

    @Query("DELETE FROM likes WHERE userId = :userId AND tweetId = :tweetId")
    suspend fun delete(
        userId: String,
        tweetId: String
    ): Int

    @Query("SELECT EXISTS(SELECT 1 FROM likes WHERE userId = :userId AND tweetId = :tweetId)")
    suspend fun exists(
        userId: String,
        tweetId: String
    ): Boolean
}
