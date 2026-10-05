package com.twitterclone.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.twitterclone.core.database.model.FollowEntity

@Dao
interface FollowDao {
    /** @return the new row id, or -1 when the edge already existed (unique constraint). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(follow: FollowEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(follows: List<FollowEntity>)

    @Query("DELETE FROM follows WHERE followerId = :followerId AND followeeId = :followeeId")
    suspend fun delete(
        followerId: String,
        followeeId: String
    ): Int

    @Query("SELECT EXISTS(SELECT 1 FROM follows WHERE followerId = :followerId AND followeeId = :followeeId)")
    suspend fun exists(
        followerId: String,
        followeeId: String
    ): Boolean
}
