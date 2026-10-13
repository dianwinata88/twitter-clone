package com.twitterclone.core.model.repository

import androidx.paging.PagingData
import com.twitterclone.core.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUser(id: String): User?

    suspend fun getUserByHandle(username: String): User?

    /** @return the new `followedByMe` value for [userId]. */
    suspend fun toggleFollow(userId: String): Result<Boolean>

    fun followers(userId: String): Flow<PagingData<User>>

    fun following(userId: String): Flow<PagingData<User>>

    suspend fun updateProfile(
        displayName: String,
        bio: String
    ): Result<User>
}
