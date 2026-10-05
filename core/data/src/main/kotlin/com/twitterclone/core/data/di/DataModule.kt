package com.twitterclone.core.data.di

import com.twitterclone.core.data.repository.FakeAuthRepository
import com.twitterclone.core.data.repository.FakeTimelineRepository
import com.twitterclone.core.data.repository.FakeTweetRepository
import com.twitterclone.core.data.repository.FakeUserRepository
import com.twitterclone.core.model.repository.AuthRepository
import com.twitterclone.core.model.repository.TimelineRepository
import com.twitterclone.core.model.repository.TweetRepository
import com.twitterclone.core.model.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds every repository contract to its fake implementation. When a real
 * backend lands, swap the `Fake*` classes for Retrofit/Room-mediator versions
 * here — feature modules never notice.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FakeAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTimelineRepository(impl: FakeTimelineRepository): TimelineRepository

    @Binds
    @Singleton
    abstract fun bindTweetRepository(impl: FakeTweetRepository): TweetRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: FakeUserRepository): UserRepository
}
