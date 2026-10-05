package com.twitterclone.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

/**
 * Persists the auth session: a session token plus the current user id.
 * Written by `AuthRepository` implementations, read anywhere viewer state is
 * needed.
 */
@Singleton
class SessionManager
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val store = context.sessionDataStore

        val sessionToken: Flow<String?> = store.data.map { it[KEY_SESSION_TOKEN] }

        val currentUserId: Flow<String?> = store.data.map { it[KEY_USER_ID] }

        suspend fun setSession(
            token: String,
            userId: String
        ) {
            store.edit {
                it[KEY_SESSION_TOKEN] = token
                it[KEY_USER_ID] = userId
            }
        }

        suspend fun clear() {
            store.edit {
                it.remove(KEY_SESSION_TOKEN)
                it.remove(KEY_USER_ID)
            }
        }

        private companion object {
            val KEY_SESSION_TOKEN = stringPreferencesKey("session_token")
            val KEY_USER_ID = stringPreferencesKey("user_id")
        }
    }
