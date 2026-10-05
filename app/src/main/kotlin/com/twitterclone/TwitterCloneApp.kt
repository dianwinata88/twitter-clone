package com.twitterclone

import android.app.Application
import com.twitterclone.core.common.di.ApplicationScope
import com.twitterclone.core.database.seed.DatabaseSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class TwitterCloneApp : Application() {
    @Inject lateinit var seeder: DatabaseSeeder

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        // Seed the mock backend on first launch so login lands on a populated feed.
        applicationScope.launch {
            seeder.seedIfEmpty()
        }
    }
}
