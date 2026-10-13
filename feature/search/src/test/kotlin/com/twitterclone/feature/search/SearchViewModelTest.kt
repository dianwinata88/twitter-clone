package com.twitterclone.feature.search

import androidx.paging.PagingData
import com.twitterclone.core.common.result.Async
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.model.User
import com.twitterclone.core.model.repository.SearchRepository
import com.twitterclone.core.model.repository.TweetRepository
import com.twitterclone.core.model.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var searchRepository: FakeSearchRepository
    private lateinit var tweetRepository: FakeTweetRepository
    private lateinit var userRepository: FakeUserRepository
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        searchRepository = FakeSearchRepository()
        tweetRepository = FakeTweetRepository()
        userRepository = FakeUserRepository()
        viewModel = SearchViewModel(searchRepository, tweetRepository, userRepository)
    }

    @Test
    fun `trending emits loaded tweets`() =
        runTest {
            searchRepository.trendingResult = listOf(testTweet("t-1"), testTweet("t-2"))
            val vm = SearchViewModel(searchRepository, tweetRepository, userRepository)

            val loaded = vm.trending.first { it !is Async.Loading }
            assertEquals(Async.Success(listOf(testTweet("t-1"), testTweet("t-2"))), loaded)
        }

    @Test
    fun `blank query emits empty paging data without calling repositories`() =
        runTest {
            backgroundScope.launch { viewModel.tweets.collect {} }
            backgroundScope.launch { viewModel.users.collect {} }
            advanceTimeBy(500)

            assertTrue(searchRepository.tweetQueries.isEmpty())
            assertTrue(searchRepository.userQueries.isEmpty())
        }

    @Test
    fun `query is debounced before searching tweets`() =
        runTest {
            backgroundScope.launch { viewModel.tweets.collect {} }

            viewModel.onQueryChange("k")
            advanceTimeBy(100)
            viewModel.onQueryChange("kotlin")
            advanceTimeBy(299)
            assertTrue(searchRepository.tweetQueries.isEmpty())

            advanceTimeBy(2)
            assertEquals(listOf("kotlin"), searchRepository.tweetQueries)
        }

    @Test
    fun `query is trimmed and same trimmed query does not re-search`() =
        runTest {
            backgroundScope.launch { viewModel.tweets.collect {} }

            viewModel.onQueryChange(" kotlin ")
            advanceTimeBy(400)
            viewModel.onQueryChange("kotlin")
            advanceTimeBy(400)

            assertEquals(listOf("kotlin"), searchRepository.tweetQueries)
        }

    @Test
    fun `users flow receives the debounced trimmed query`() =
        runTest {
            backgroundScope.launch { viewModel.users.collect {} }

            viewModel.onQueryChange("  alice ")
            advanceTimeBy(400)

            assertEquals(listOf("alice"), searchRepository.userQueries)
        }

    @Test
    fun `clearing the query returns to trending without another search`() =
        runTest {
            backgroundScope.launch { viewModel.tweets.collect {} }

            viewModel.onQueryChange("kotlin")
            advanceTimeBy(400)
            viewModel.onQueryChange("")
            advanceTimeBy(400)

            assertEquals(listOf("kotlin"), searchRepository.tweetQueries)
        }

    @Test
    fun `onTabSelected updates selected tab`() {
        assertEquals(SearchTab.POSTS, viewModel.selectedTab.value)

        viewModel.onTabSelected(SearchTab.PEOPLE)
        assertEquals(SearchTab.PEOPLE, viewModel.selectedTab.value)
    }

    @Test
    fun `onLikeClick delegates to tweet repository`() =
        runTest {
            viewModel.onLikeClick("t-42")

            assertEquals(listOf("t-42"), tweetRepository.likedTweetIds)
        }

    @Test
    fun `onFollowClick applies optimistic override confirmed by repository`() =
        runTest {
            val user = testUser(followedByMe = false)
            userRepository.followResult = Result.success(true)

            viewModel.onFollowClick(user)

            assertEquals(listOf("u-1"), userRepository.followedUserIds)
            assertEquals(true, viewModel.followOverrides.value["u-1"])
        }

    @Test
    fun `onFollowClick reverts override when repository fails`() =
        runTest {
            val user = testUser(followedByMe = false)
            userRepository.followResult = Result.failure(RuntimeException("boom"))

            viewModel.onFollowClick(user)

            assertEquals(false, viewModel.followOverrides.value["u-1"])
        }

    private fun testUser(
        id: String = "u-1",
        followedByMe: Boolean = false,
    ) = User(
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
        followedByMe = followedByMe,
    )

    private fun testTweet(id: String) =
        Tweet(
            id = id,
            author = testUser(),
            text = "tweet $id",
            media = emptyList(),
            createdAt = Instant.fromEpochSeconds(1_700_000_000),
            replyCount = 0,
            repostCount = 0,
            likeCount = 0,
            likedByMe = false,
        )
}

private class FakeSearchRepository : SearchRepository {
    val tweetQueries = mutableListOf<String>()
    val userQueries = mutableListOf<String>()
    var trendingResult: List<Tweet> = emptyList()

    override fun searchTweets(query: String): Flow<PagingData<Tweet>> {
        tweetQueries += query
        return flowOf(PagingData.empty())
    }

    override fun searchUsers(query: String): Flow<PagingData<User>> {
        userQueries += query
        return flowOf(PagingData.empty())
    }

    override fun trending(limit: Int): Flow<List<Tweet>> = flowOf(trendingResult)
}

private class FakeTweetRepository : TweetRepository {
    val likedTweetIds = mutableListOf<String>()

    override suspend fun getTweet(id: String): Tweet? = null

    override suspend fun postTweet(
        text: String,
        mediaLocalUris: List<String>,
    ): Result<Tweet> = Result.failure(UnsupportedOperationException())

    override suspend fun toggleLike(tweetId: String): Result<Boolean> {
        likedTweetIds += tweetId
        return Result.success(true)
    }
}

private class FakeUserRepository : UserRepository {
    val followedUserIds = mutableListOf<String>()
    var followResult: Result<Boolean> = Result.success(true)

    override suspend fun getUser(id: String): User? = null

    override suspend fun getUserByHandle(username: String): User? = null

    override suspend fun toggleFollow(userId: String): Result<Boolean> {
        followedUserIds += userId
        return followResult
    }

    override fun followers(userId: String): Flow<PagingData<User>> = flowOf(PagingData.empty())

    override fun following(userId: String): Flow<PagingData<User>> = flowOf(PagingData.empty())

    override suspend fun updateProfile(
        displayName: String,
        bio: String,
    ): Result<User> = Result.failure(UnsupportedOperationException())
}
