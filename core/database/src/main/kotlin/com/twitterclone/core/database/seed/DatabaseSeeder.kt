package com.twitterclone.core.database.seed

import androidx.room.withTransaction
import com.twitterclone.core.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeds the database on first launch. Idempotent: it is a no-op once any user
 * row exists.
 */
@Singleton
class DatabaseSeeder
    @Inject
    constructor(
        private val database: AppDatabase,
    ) {
        suspend fun seedIfEmpty() =
            withContext(Dispatchers.IO) {
                if (database.userDao().count() > 0) return@withContext
                val now = System.currentTimeMillis()
                val users = SeedData.users(now)
                val tweets = SeedData.tweets(users, now)
                val media = SeedData.media(tweets)
                val follows = SeedData.follows(users, now)
                val likes = SeedData.likes(users, tweets, now)

                database.withTransaction {
                    database.userDao().upsert(users)
                    database.tweetDao().upsert(tweets)
                    database.mediaDao().upsert(media)
                    database.followDao().insertIgnore(follows)
                    database.likeDao().insertIgnore(likes)
                    users.forEach {
                        database.userDao().refreshFollowCounts(it.id)
                        database.userDao().refreshTweetCount(it.id)
                    }
                }
            }
    }
