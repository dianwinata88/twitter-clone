package com.twitterclone.feature.feed

import androidx.paging.PagingData
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.model.User
import com.twitterclone.core.model.repository.TimelineRepository
import com.twitterclone.core.model.repository.TweetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
import java.io.IOException

fun fakeTweet(
    id: String,
    likedByMe: Boolean = false,
    likeCount: Int = 0,
): Tweet =
    Tweet(
        id = id,
        author =
            User(
                id = "u1",
                username = "author",
                displayName = "Author",
                bio = "",
                avatarUrl = null,
                headerUrl = null,
                followerCount = 0,
                followingCount = 0,
                tweetCount = 0,
                createdAt = Instant.fromEpochMilliseconds(0),
            ),
        text = "tweet $id",
        media = emptyList(),
        createdAt = Instant.fromEpochMilliseconds(0),
        replyCount = 0,
        repostCount = 0,
        likeCount = likeCount,
        likedByMe = likedByMe,
    )

class FakeTimelineRepository : TimelineRepository {
    var refreshHomeCalls = 0

    override fun homeTimeline(): Flow<PagingData<Tweet>> = flowOf(PagingData.empty())

    override fun userTweets(userId: String): Flow<PagingData<Tweet>> = flowOf(PagingData.empty())

    override suspend fun refreshHome() {
        refreshHomeCalls++
    }
}

class FakeTweetRepository(
    private val tweetProvider: (String) -> Tweet? = { null },
) : TweetRepository {
    /** When true, every subsequent `toggleLike` fails — the FailureInjector analogue. */
    var failLikes = false
    val likedTweetIds = mutableListOf<String>()

    override suspend fun getTweet(id: String): Tweet? = tweetProvider(id)

    override suspend fun postTweet(
        text: String,
        mediaLocalUris: List<String>
    ): Result<Tweet> = Result.failure(UnsupportedOperationException())

    override suspend fun toggleLike(tweetId: String): Result<Boolean> {
        likedTweetIds += tweetId
        return if (failLikes) {
            Result.failure(IOException("simulated backend failure"))
        } else {
            Result.success(true)
        }
    }
}
