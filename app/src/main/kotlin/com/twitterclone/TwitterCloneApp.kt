package com.twitterclone

import android.app.Application
import com.twitterclone.core.common.di.ApplicationScope
import com.twitterclone.core.database.seed.DatabaseSeeder
import com.twitterclone.core.datastore.SessionManager
import com.twitterclone.core.model.repository.AuthRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class TwitterCloneApp : Application() {
    @Inject lateinit var seeder: DatabaseSeeder

    @Inject lateinit var sessionManager: SessionManager

    @Inject lateinit var authRepository: AuthRepository

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        // Seed the mock backend, then sign in the demo account on first launch
        // so the app boots straight into a populated feed.
        applicationScope.launch {
            seeder.seedIfEmpty()
            if (sessionManager.sessionToken.first() == null) {
                authRepository.login("demo", "demo")
            }
        }
    }
}
