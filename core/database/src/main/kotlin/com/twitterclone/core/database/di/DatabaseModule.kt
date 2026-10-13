package com.twitterclone.core.database.di

import android.content.Context
import androidx.room.Room
import com.twitterclone.core.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room
            .databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides fun provideUserDao(db: AppDatabase) = db.userDao()

    @Provides fun provideTweetDao(db: AppDatabase) = db.tweetDao()

    @Provides fun provideMediaDao(db: AppDatabase) = db.mediaDao()

    @Provides fun provideLikeDao(db: AppDatabase) = db.likeDao()

    @Provides fun provideFollowDao(db: AppDatabase) = db.followDao()

    @Provides fun provideRemoteKeysDao(db: AppDatabase) = db.remoteKeysDao()
}
