package com.twitterclone.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.twitterclone.core.database.model.HydratedTweet
import com.twitterclone.core.database.model.TweetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TweetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tweets: List<TweetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tweet: TweetEntity)

    @Query("SELECT COUNT(*) FROM tweets")
    suspend fun count(): Int

    /**
     * Home timeline: tweets authored by the viewer or by anyone the viewer
     * follows, reverse-chronological. Returns a [PagingSource] of hydrated
     * tweets (author, media, viewer-scoped like/repost state).
     */
    @Transaction
    @Query(
        """
        SELECT t.*,
            EXISTS(SELECT 1 FROM likes l WHERE l.tweetId = t.id AND l.userId = :viewerId) AS likedByMe,
            0 AS repostedByMe
        FROM tweets t
        WHERE t.authorId = :viewerId
           OR t.authorId IN (SELECT followeeId FROM follows WHERE followerId = :viewerId)
        ORDER BY t.createdAtEpochMillis DESC
        """,
    )
    fun homeTimeline(viewerId: String): PagingSource<Int, HydratedTweet>

    /** All tweets by one author, reverse-chronological. */
    @Transaction
    @Query(
        """
        SELECT t.*,
            EXISTS(SELECT 1 FROM likes l WHERE l.tweetId = t.id AND l.userId = :viewerId) AS likedByMe,
            0 AS repostedByMe
        FROM tweets t
        WHERE t.authorId = :authorId
        ORDER BY t.createdAtEpochMillis DESC
        """,
    )
    fun userTweets(
        authorId: String,
        viewerId: String
    ): PagingSource<Int, HydratedTweet>

    @Transaction
    @Query(
        """
        SELECT t.*,
            EXISTS(SELECT 1 FROM likes l WHERE l.tweetId = t.id AND l.userId = :viewerId) AS likedByMe,
            0 AS repostedByMe
        FROM tweets t
        WHERE t.id = :tweetId
        """,
    )
    suspend fun hydratedById(
        tweetId: String,
        viewerId: String
    ): HydratedTweet?

    @Transaction
    @Query(
        """
        SELECT t.*,
            EXISTS(SELECT 1 FROM likes l WHERE l.tweetId = t.id AND l.userId = :viewerId) AS likedByMe,
            0 AS repostedByMe
        FROM tweets t
        WHERE t.id = :tweetId
        """,
    )
    fun observeHydratedById(
        tweetId: String,
        viewerId: String
    ): Flow<HydratedTweet?>

    @Query("UPDATE tweets SET likeCount = likeCount + :delta WHERE id = :tweetId")
    suspend fun incrementLikeCount(
        tweetId: String,
        delta: Int
    )

    @Query("UPDATE tweets SET syncState = :syncState WHERE id = :tweetId")
    suspend fun updateSyncState(
        tweetId: String,
        syncState: String
    )
}
