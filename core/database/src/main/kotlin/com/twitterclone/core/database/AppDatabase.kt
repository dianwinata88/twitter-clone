package com.twitterclone.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.twitterclone.core.database.dao.FollowDao
import com.twitterclone.core.database.dao.LikeDao
import com.twitterclone.core.database.dao.MediaDao
import com.twitterclone.core.database.dao.RemoteKeysDao
import com.twitterclone.core.database.dao.TweetDao
import com.twitterclone.core.database.dao.UserDao
import com.twitterclone.core.database.model.FollowEntity
import com.twitterclone.core.database.model.LikeEntity
import com.twitterclone.core.database.model.MediaEntity
import com.twitterclone.core.database.model.RemoteKeysEntity
import com.twitterclone.core.database.model.TweetEntity
import com.twitterclone.core.database.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        TweetEntity::class,
        MediaEntity::class,
        LikeEntity::class,
        FollowEntity::class,
        RemoteKeysEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun tweetDao(): TweetDao

    abstract fun mediaDao(): MediaDao

    abstract fun likeDao(): LikeDao

    abstract fun followDao(): FollowDao

    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {
        const val DATABASE_NAME = "twitter.db"
    }
}
