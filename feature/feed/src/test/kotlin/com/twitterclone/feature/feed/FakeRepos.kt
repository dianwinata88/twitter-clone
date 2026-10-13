package com.twitterclone.feature.feed

import androidx.paging.PagingData
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.model.User
import com.twitterclone.core.model.repository.TimelineRepository
import com.twitterclone.core.model.repository.TweetRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant

fun fakeUser(id: String = "u1"): User =
    User(
        id = id,
        username = "user$id",
        displayName = "User $id",
        bio = "bio",
        avatarUrl = null,
        headerUrl = null,
        followerCount = 0,
        followingCount = 0,
        tweetCount = 0,
        createdAt = Instant.fromEpochSeconds(1_700_000_000),
    )

fun fakeTweet(
    id: String,
    likedByMe: Boolean = false,
    likeCount: Int = 0,
): Tweet =
    Tweet(
        id = id,
        author = fakeUser(),
        text = "tweet $id",
        media = emptyList(),
        createdAt = Instant.fromEpochSeconds(1_700_000_000),
        replyCount = 1,
        repostCount = 2,
        likeCount = likeCount,
        viewCount = 10,
        likedByMe = likedByMe,
    )

/** In-memory [TimelineRepository] that records interactions. */
class FakeTimelineRepository(
    private val timelineTweets: List<Tweet> = emptyList(),
) : TimelineRepository {
    var homeTimelineCalls = 0
        private set
    var refreshCalls = 0
        private set

    /** When set, [refreshHome] suspends until this is completed. */
    var refreshGate: CompletableDeferred<Unit>? = null

    override fun homeTimeline(): Flow<PagingData<Tweet>> {
        homeTimelineCalls++
        return flowOf(PagingData.from(timelineTweets))
    }

    override fun userTweets(userId: String): Flow<PagingData<Tweet>> = flowOf(PagingData.empty())

    override suspend fun refreshHome() {
        refreshCalls++
        refreshGate?.await()
    }
}

/** In-memory [TweetRepository] that records interactions. */
class FakeTweetRepository : TweetRepository {
    val tweets = mutableMapOf<String, Tweet>()
    val likeCalls = mutableListOf<String>()

    /** When set, [toggleLike] returns this failure instead of toggling. */
    var likeFailure: Throwable? = null
    var getTweetFailure: Throwable? = null

    override suspend fun getTweet(id: String): Tweet? {
        getTweetFailure?.let { throw it }
        return tweets[id]
    }

    override suspend fun postTweet(
        text: String,
        mediaLocalUris: List<String>,
    ): Result<Tweet> = Result.failure(UnsupportedOperationException())

    override suspend fun toggleLike(tweetId: String): Result<Boolean> {
        likeCalls += tweetId
        likeFailure?.let { return Result.failure(it) }
        val tweet = tweets[tweetId] ?: return Result.failure(NoSuchElementException(tweetId))
        val nowLiked = !tweet.likedByMe
        tweets[tweetId] =
            tweet.copy(
                likedByMe = nowLiked,
                likeCount = tweet.likeCount + if (nowLiked) 1 else -1,
            )
        return Result.success(nowLiked)
    }
}
