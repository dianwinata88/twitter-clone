package com.twitterclone.core.model.repository

import com.twitterclone.core.model.User

interface UserRepository {
    suspend fun getUser(id: String): User?

    suspend fun getUserByHandle(username: String): User?

    /** @return the new `followedByMe` value for [userId]. */
    suspend fun toggleFollow(userId: String): Result<Boolean>

    suspend fun followers(userId: String): List<User>

    suspend fun following(userId: String): List<User>

    suspend fun updateProfile(
        displayName: String,
        bio: String
    ): Result<User>
}
