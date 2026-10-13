package com.twitterclone.feature.composetweet

import com.twitterclone.core.model.Media
import com.twitterclone.core.model.MediaType
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.model.User
import com.twitterclone.core.model.repository.AuthRepository
import com.twitterclone.core.model.repository.TweetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private val demoUser =
    User(
        id = "u-demo",
        username = "demo",
        displayName = "Demo User",
        bio = "",
        avatarUrl = "https://example.com/avatar.png",
        headerUrl = null,
        followerCount = 0,
        followingCount = 0,
        tweetCount = 0,
        createdAt = Instant.fromEpochMilliseconds(0),
    )

private class FakeAuthRepository : AuthRepository {
    private val user = MutableStateFlow<User?>(demoUser)
    override val currentUser: Flow<User?> = user

    override suspend fun login(
        username: String,
        password: String,
    ): Result<User> = Result.success(demoUser)

    override suspend fun register(
        username: String,
        displayName: String,
        password: String,
    ): Result<User> = Result.success(demoUser)

    override suspend fun logout() = Unit
}

private class FakeTweetRepository : TweetRepository {
    var shouldFail = false
    val posted = mutableListOf<Pair<String, List<String>>>()

    override suspend fun getTweet(id: String): Tweet? = null

    override suspend fun postTweet(
        text: String,
        mediaLocalUris: List<String>,
    ): Result<Tweet> {
        posted += text to mediaLocalUris
        return if (shouldFail) {
            Result.failure(IllegalStateException("simulated failure"))
        } else {
            Result.success(
                Tweet(
                    id = "t-1",
                    author = demoUser,
                    text = text,
                    media =
                        mediaLocalUris.mapIndexed { index, uri ->
                            Media(
                                id = "m-$index",
                                type = MediaType.IMAGE,
                                url = uri,
                                width = 0,
                                height = 0,
                            )
                        },
                    createdAt = Instant.fromEpochMilliseconds(0),
                    replyCount = 0,
                    repostCount = 0,
                    likeCount = 0,
                    likedByMe = false,
                ),
            )
        }
    }

    override suspend fun toggleLike(tweetId: String): Result<Boolean> = Result.success(true)
}

@OptIn(ExperimentalCoroutinesApi::class)
class ComposeTweetViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var tweetRepository: FakeTweetRepository
    private lateinit var viewModel: ComposeTweetViewModel

    @Before
    fun setUp() {
        tweetRepository = FakeTweetRepository()
        viewModel = ComposeTweetViewModel(tweetRepository, FakeAuthRepository())
    }

    @Test
    fun `initial state cannot tweet`() {
        assertFalse(viewModel.uiState.value.canTweet)
    }

    @Test
    fun `avatar comes from the logged-in user`() =
        runTest {
            assertEquals(demoUser.avatarUrl, viewModel.uiState.first { it.avatarUrl != null }.avatarUrl)
        }

    @Test
    fun `blank text stays disabled`() {
        viewModel.onTextChange("   ")
        assertFalse(viewModel.uiState.value.canTweet)
    }

    @Test
    fun `valid text enables tweeting`() {
        viewModel.onTextChange("hello world")
        assertTrue(viewModel.uiState.value.canTweet)
        assertEquals(MAX_TWEET_LENGTH - 11, viewModel.uiState.value.charactersRemaining)
    }

    @Test
    fun `over-limit text disables tweeting`() {
        viewModel.onTextChange("x".repeat(MAX_TWEET_LENGTH + 1))
        assertTrue(viewModel.uiState.value.isOverLimit)
        assertFalse(viewModel.uiState.value.canTweet)
    }

    @Test
    fun `media selection is capped at four`() {
        viewModel.onMediaSelected(listOf("a", "b", "c", "d", "e"))
        assertEquals(listOf("a", "b", "c", "d"), viewModel.uiState.value.mediaUris)
        assertFalse(viewModel.uiState.value.canAddMedia)
    }

    @Test
    fun `removing media frees a slot`() {
        viewModel.onMediaSelected(listOf("a", "b", "c", "d"))
        viewModel.onRemoveMedia("b")
        assertEquals(listOf("a", "c", "d"), viewModel.uiState.value.mediaUris)
        assertTrue(viewModel.uiState.value.canAddMedia)
    }

    @Test
    fun `successful post passes text and media to the repository and emits TweetPosted`() =
        runTest {
            val events = mutableListOf<ComposeTweetEvent>()
            val collectJob = launch { viewModel.events.collect { events += it } }

            viewModel.onTextChange("shipping it")
            viewModel.onMediaSelected(listOf("content://img/1"))
            viewModel.onTweetClicked()
            runCurrent()

            assertEquals(listOf("shipping it" to listOf("content://img/1")), tweetRepository.posted)
            assertEquals(listOf(ComposeTweetEvent.TweetPosted), events)
            collectJob.cancel()
        }

    @Test
    fun `failed post emits PostFailed and re-enables the composer`() =
        runTest {
            tweetRepository.shouldFail = true
            val events = mutableListOf<ComposeTweetEvent>()
            val collectJob = launch { viewModel.events.collect { events += it } }

            viewModel.onTextChange("this will fail")
            viewModel.onTweetClicked()
            runCurrent()

            assertEquals(listOf(ComposeTweetEvent.PostFailed), events)
            assertFalse(viewModel.uiState.value.isPosting)
            assertTrue(viewModel.uiState.value.canTweet)
            collectJob.cancel()
        }

    @Test
    fun `tweeting with empty text does not call the repository`() =
        runTest {
            viewModel.onTweetClicked()
            assertTrue(tweetRepository.posted.isEmpty())
        }
}
