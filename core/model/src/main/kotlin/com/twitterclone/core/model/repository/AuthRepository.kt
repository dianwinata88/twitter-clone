package com.twitterclone.core.model.repository

import com.twitterclone.core.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** Emits the logged-in user, or `null` when logged out. */
    val currentUser: Flow<User?>

    suspend fun login(
        username: String,
        password: String
    ): Result<User>

    suspend fun register(
        username: String,
        displayName: String,
        password: String
    ): Result<User>

    suspend fun logout()
}
