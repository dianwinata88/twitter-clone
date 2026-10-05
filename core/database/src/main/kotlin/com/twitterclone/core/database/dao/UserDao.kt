package com.twitterclone.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.twitterclone.core.database.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(users: List<UserEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observeById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getByUsername(username: String): UserEntity?

    @Query(
        """
        SELECT u.* FROM users u
        INNER JOIN follows f ON u.id = f.followerId
        WHERE f.followeeId = :userId
        ORDER BY u.username
        """,
    )
    suspend fun followers(userId: String): List<UserEntity>

    @Query(
        """
        SELECT u.* FROM users u
        INNER JOIN follows f ON u.id = f.followeeId
        WHERE f.followerId = :userId
        ORDER BY u.username
        """,
    )
    suspend fun following(userId: String): List<UserEntity>

    @Query("UPDATE users SET displayName = :displayName, bio = :bio WHERE id = :id")
    suspend fun updateProfile(
        id: String,
        displayName: String,
        bio: String
    )

    @Query(
        """
        UPDATE users SET
            followerCount = (SELECT COUNT(*) FROM follows WHERE followeeId = :userId),
            followingCount = (SELECT COUNT(*) FROM follows WHERE followerId = :userId)
        WHERE id = :userId
        """,
    )
    suspend fun refreshFollowCounts(userId: String)

    @Query("UPDATE users SET tweetCount = (SELECT COUNT(*) FROM tweets WHERE authorId = :userId) WHERE id = :userId")
    suspend fun refreshTweetCount(userId: String)
}
