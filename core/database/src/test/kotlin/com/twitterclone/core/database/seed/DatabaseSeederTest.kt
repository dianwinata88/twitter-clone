package com.twitterclone.core.database.seed

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.twitterclone.core.database.AppDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies [DatabaseSeeder.seedIfEmpty]: it populates users/tweets/follows/likes
 * and is a no-op once rows exist (idempotent across repeated calls).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DatabaseSeederTest {
    private lateinit var db: AppDatabase
    private lateinit var seeder: DatabaseSeeder

    @Before
    fun setUp() {
        db =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    AppDatabase::class.java,
                ).build()
        seeder = DatabaseSeeder(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `seedIfEmpty populates users tweets follows and likes`() =
        runTest {
            seeder.seedIfEmpty()

            assertEquals(8, db.userDao().count())
            assertEquals(200, db.tweetDao().count())
            assertTrue(db.followDao().count() > 0)
            assertTrue(db.likeDao().count() > 0)

            // The demo user is seeded and its denormalized counts are computed.
            val demo = db.userDao().getByUsername(SeedData.DEMO_USERNAME)
            assertTrue(demo != null)
            assertTrue(demo!!.followingCount > 0)
            assertTrue(demo.tweetCount >= 0)
        }

    @Test
    fun `seedIfEmpty is idempotent`() =
        runTest {
            seeder.seedIfEmpty()
            val users = db.userDao().count()
            val tweets = db.tweetDao().count()
            val follows = db.followDao().count()
            val likes = db.likeDao().count()

            seeder.seedIfEmpty()
            seeder.seedIfEmpty()

            assertEquals(users, db.userDao().count())
            assertEquals(tweets, db.tweetDao().count())
            assertEquals(follows, db.followDao().count())
            assertEquals(likes, db.likeDao().count())
        }

    @Test
    fun `follow graph is centered on demo and demo follows everyone`() =
        runTest {
            seeder.seedIfEmpty()

            val demo = db.userDao().getByUsername(SeedData.DEMO_USERNAME)!!
            db.userDao().getAllIds().filter { it != demo.id }.forEach { otherId ->
                assertTrue(db.followDao().exists(demo.id, otherId))
            }
        }
}
