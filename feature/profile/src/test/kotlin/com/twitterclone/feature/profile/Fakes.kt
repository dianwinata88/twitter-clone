package com.twitterclone.feature.profile

import androidx.paging.PagingData
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.model.User
import com.twitterclone.core.model.repository.AuthRepository
import com.twitterclone.core.model.repository.TimelineRepository
import com.twitterclone.core.model.repository.TweetRepository
import com.twitterclone.core.model.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant

internal fun testUser(
    id: String,
    username: String = id,
    followedByMe: Boolean = false,
    followerCount: Int = 0,
) = User(
    id = id,
    username = username,
    displayName = "Name $id",
    bio = "bio of $id",
    avatarUrl = null,
    headerUrl = null,
    followerCount = followerCount,
    followingCount = 0,
    tweetCount = 0,
    createdAt = Instant.fromEpochSeconds(1_700_000_000),
    followedByMe = followedByMe,
)

internal class FakeUserRepository(
    private val users: Map<String, User> = emptyMap(),
) : UserRepository {
    var toggleFollowResult: Result<Boolean> = Result.success(true)
    var followersResult: List<User> = emptyList()
    var followingResult: List<User> = emptyList()

    override suspend fun getUser(id: String): User? = users[id]

    override suspend fun getUserByHandle(username: String): User? = users.values.firstOrNull { it.username == username }

    override suspend fun toggleFollow(userId: String): Result<Boolean> = toggleFollowResult

    override suspend fun followers(userId: String): List<User> = followersResult

    override suspend fun following(userId: String): List<User> = followingResult

    override suspend fun updateProfile(
        displayName: String,
        bio: String
    ): Result<User> {
        val current = checkNotNull(users.values.firstOrNull())
        return Result.success(current.copy(displayName = displayName, bio = bio))
    }
}

internal class FakeAuthRepository(
    me: User?,
) : AuthRepository {
    override val currentUser: Flow<User?> = MutableStateFlow(me)

    override suspend fun login(
        username: String,
        password: String
    ): Result<User> = error("unused")

    override suspend fun register(
        username: String,
        displayName: String,
        password: String
    ): Result<User> = error("unused")

    override suspend fun logout() = error("unused")
}

internal class FakeTweetRepository : TweetRepository {
    override suspend fun getTweet(id: String): Tweet? = null

    override suspend fun postTweet(
        text: String,
        mediaLocalUris: List<String>
    ): Result<Tweet> = error("unused")

    override suspend fun toggleLike(tweetId: String): Result<Boolean> = Result.success(true)
}

internal class FakeTimelineRepository : TimelineRepository {
    override fun homeTimeline(): Flow<PagingData<Tweet>> = flowOf(PagingData.empty())

    override fun userTweets(userId: String): Flow<PagingData<Tweet>> = flowOf(PagingData.empty())

    override suspend fun refreshHome() = Unit
}
