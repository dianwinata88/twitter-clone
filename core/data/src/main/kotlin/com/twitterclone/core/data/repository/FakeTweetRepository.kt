package com.twitterclone.core.data.repository

import com.twitterclone.core.common.di.Dispatcher
import com.twitterclone.core.common.di.TcDispatchers
import com.twitterclone.core.common.result.safeCall
import com.twitterclone.core.data.fake.FailureInjector
import com.twitterclone.core.data.fake.SimulatedBackendException
import com.twitterclone.core.data.fake.simulateLatency
import com.twitterclone.core.data.mapper.toModel
import com.twitterclone.core.database.dao.LikeDao
import com.twitterclone.core.database.dao.MediaDao
import com.twitterclone.core.database.dao.TweetDao
import com.twitterclone.core.database.dao.UserDao
import com.twitterclone.core.database.model.LikeEntity
import com.twitterclone.core.database.model.MediaEntity
import com.twitterclone.core.database.model.TweetEntity
import com.twitterclone.core.datastore.SessionManager
import com.twitterclone.core.model.SyncState
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.model.repository.TweetRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake tweet store backed by Room. `postTweet` inserts with `PENDING` sync
 * state and flips to `CONFIRMED` (or `FAILED` when [FailureInjector] is on)
 * after simulated latency, so UIs can exercise pending/rollback states.
 */
@Singleton
class FakeTweetRepository
    @Inject
    constructor(
        private val tweetDao: TweetDao,
        private val mediaDao: MediaDao,
        private val likeDao: LikeDao,
        private val userDao: UserDao,
        private val sessionManager: SessionManager,
        private val failureInjector: FailureInjector,
        @Dispatcher(TcDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    ) : TweetRepository {
        override suspend fun getTweet(id: String): Tweet? =
            withContext(ioDispatcher) {
                val viewerId = sessionManager.currentUserId.first() ?: return@withContext null
                tweetDao.hydratedById(id, viewerId)?.toModel()
            }

        override suspend fun postTweet(
            text: String,
            mediaLocalUris: List<String>
        ): Result<Tweet> =
            withContext(ioDispatcher) {
                safeCall {
                    val authorId =
                        sessionManager.currentUserId.first()
                            ?: throw SimulatedBackendException()
                    val now = System.currentTimeMillis()
                    val tweetId = "t-local-${UUID.randomUUID()}"
                    tweetDao.upsert(
                        TweetEntity(
                            id = tweetId,
                            authorId = authorId,
                            text = text,
                            createdAtEpochMillis = now,
                            replyCount = 0,
                            repostCount = 0,
                            likeCount = 0,
                            viewCount = 0,
                            type = "ORIGINAL",
                            inReplyToTweetId = null,
                            conversationId = tweetId,
                            syncState = SyncState.PENDING.name,
                        ),
                    )
                    mediaDao.upsert(
                        mediaLocalUris.mapIndexed { index, uri ->
                            MediaEntity(
                                id = "m-$tweetId-$index",
                                tweetId = tweetId,
                                type = "IMAGE",
                                url = uri,
                                localUri = uri,
                                width = 0,
                                height = 0,
                                altText = null,
                            )
                        },
                    )

                    simulateLatency()
                    val synced = !failureInjector.shouldFail
                    tweetDao.updateSyncState(tweetId, if (synced) SyncState.CONFIRMED.name else SyncState.FAILED.name)
                    if (!synced) throw SimulatedBackendException()
                    userDao.refreshTweetCount(authorId)
                    tweetDao.hydratedById(tweetId, authorId)?.toModel()
                        ?: throw IllegalStateException("Tweet vanished after insert: $tweetId")
                }
            }

        /**
         * Optimistic flip of the viewer's like. `INSERT OR IGNORE` on the
         * (userId, tweetId) PK keeps concurrent toggles constraint-safe.
         */
        override suspend fun toggleLike(tweetId: String): Result<Boolean> =
            withContext(ioDispatcher) {
                simulateLatency()
                safeCall {
                    failureInjector.throwIfEnabled()
                    val viewerId =
                        sessionManager.currentUserId.first()
                            ?: throw SimulatedBackendException()
                    val nowLiked =
                        if (likeDao.exists(viewerId, tweetId)) {
                            likeDao.delete(viewerId, tweetId)
                            tweetDao.incrementLikeCount(tweetId, -1)
                            false
                        } else {
                            if (likeDao.insertIgnore(LikeEntity(viewerId, tweetId, System.currentTimeMillis())) != -1L) {
                                tweetDao.incrementLikeCount(tweetId, +1)
                            }
                            true
                        }
                    nowLiked
                }
            }
    }
