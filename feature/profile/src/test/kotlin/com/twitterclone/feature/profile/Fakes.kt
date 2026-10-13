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
    username: String = "user-$id",
    displayName: String = "User $id",
    followedByMe: Boolean = false,
    followerCount: Int = 10,
    followingCount: Int = 5,
    bio: String = "bio of $id",
): User =
    User(
        id = id,
        username = username,
        displayName = displayName,
        bio = bio,
        avatarUrl = "https://example.com/$id.png",
        headerUrl = null,
        followerCount = followerCount,
        followingCount = followingCount,
        tweetCount = 3,
        createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        followedByMe = followedByMe,
    )

internal fun testTweet(
    id: String,
    author: User,
): Tweet =
    Tweet(
        id = id,
        author = author,
        text = "tweet $id",
        media = emptyList(),
        createdAt = Instant.parse("2025-01-02T00:00:00Z"),
        replyCount = 0,
        repostCount = 0,
        likeCount = 0,
        likedByMe = false,
    )

internal class FakeUserRepository(
    users: List<User> = emptyList(),
) : UserRepository {
    private val usersById = users.associateBy { it.id }
    var followersPage: List<User> = emptyList()
    var followingPage: List<User> = emptyList()
    var toggleResult: Result<Boolean> = Result.success(true)
    var updateResult: Result<User>? = null
    var followersFor: String? = null
    var followingFor: String? = null
    val toggledIds = mutableListOf<String>()

    override suspend fun getUser(id: String): User? = usersById[id]

    override suspend fun getUserByHandle(username: String): User? = usersById.values.firstOrNull { it.username == username }

    override suspend fun toggleFollow(userId: String): Result<Boolean> {
        toggledIds += userId
        return toggleResult
    }

    override fun followers(userId: String): Flow<PagingData<User>> {
        followersFor = userId
        return flowOf(PagingData.from(followersPage))
    }

    override fun following(userId: String): Flow<PagingData<User>> {
        followingFor = userId
        return flowOf(PagingData.from(followingPage))
    }

    override suspend fun updateProfile(
        displayName: String,
        bio: String,
    ): Result<User> = checkNotNull(updateResult)
}

internal class FakeTimelineRepository : TimelineRepository {
    var tweetsPage: List<Tweet> = emptyList()
    var userTweetsFor: String? = null

    override fun homeTimeline(): Flow<PagingData<Tweet>> = flowOf(PagingData.from(tweetsPage))

    override fun userTweets(userId: String): Flow<PagingData<Tweet>> {
        userTweetsFor = userId
        return flowOf(PagingData.from(tweetsPage))
    }

    override suspend fun refreshHome() = Unit
}

internal class FakeTweetRepository : TweetRepository {
    var likeResult: Result<Boolean> = Result.success(true)
    val likedIds = mutableListOf<String>()

    override suspend fun getTweet(id: String): Tweet? = null

    override suspend fun postTweet(
        text: String,
        mediaLocalUris: List<String>,
    ): Result<Tweet> = error("unused")

    override suspend fun toggleLike(tweetId: String): Result<Boolean> {
        likedIds += tweetId
        return likeResult
    }
}

internal class FakeAuthRepository(
    user: User?,
) : AuthRepository {
    val current = MutableStateFlow(user)
    var loggedOut = false

    override val currentUser: Flow<User?> = current

    override suspend fun login(
        username: String,
        password: String,
    ): Result<User> = error("unused")

    override suspend fun register(
        username: String,
        displayName: String,
        password: String,
    ): Result<User> = error("unused")

    override suspend fun logout() {
        loggedOut = true
        current.value = null
    }
}
