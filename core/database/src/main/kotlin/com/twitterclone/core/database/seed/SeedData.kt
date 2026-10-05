package com.twitterclone.core.database.seed

import com.twitterclone.core.database.model.FollowEntity
import com.twitterclone.core.database.model.LikeEntity
import com.twitterclone.core.database.model.MediaEntity
import com.twitterclone.core.database.model.TweetEntity
import com.twitterclone.core.database.model.UserEntity
import kotlin.random.Random

/**
 * Deterministic seed content for the mock backend: ~8 users, ~200 tweets
 * spread over the last two weeks, a follow graph centered on the demo user
 * (username `demo`, password `demo`), media pointing at picsum.photos.
 */
object SeedData {
    const val DEMO_USER_ID = "u-demo"
    const val DEMO_USERNAME = "demo"
    const val DEMO_PASSWORD = "demo"

    private const val TWO_WEEKS_MILLIS = 14L * 24 * 60 * 60 * 1000

    private data class SeedUserSpec(
        val id: String,
        val username: String,
        val displayName: String,
        val bio: String,
    )

    private val userSpecs =
        listOf(
            SeedUserSpec(DEMO_USER_ID, DEMO_USERNAME, "Demo User", "Trying out this Twitter clone. Probably liking your tweet."),
            SeedUserSpec("u-ava", "ava_codes", "Ava Martinez", "Android dev. Compose enjoyer. Opinions are cached, not fresh."),
            SeedUserSpec("u-ben", "ben_builds", "Ben Okafor", "I build backends so frontend folks can sleep."),
            SeedUserSpec("u-cleo", "cleo_design", "Cleo Tan", "Design systems, dark themes, and 8pt grids."),
            SeedUserSpec("u-dev", "devon_ships", "Devon Wright", "Shipping on Fridays since 2019."),
            SeedUserSpec("u-emma", "emma_writes", "Emma Silva", "Tech writer. I read changelogs for fun."),
            SeedUserSpec("u-finn", "finn_games", "Finn Bergström", "Indie gamedev | Kotlin | pixel art"),
            SeedUserSpec("u-gus", "gus_data", "Gus Petrov", "Data person. SELECT * FROM opinions."),
        )

    private val tweetTemplates =
        listOf(
            "Just shipped a new feature. The tests even pass.",
            "Hot take: semicolons are optional, good naming is not.",
            "Reading Room schemas at 2am hits different.",
            "Compose previews saved me an hour today.",
            "If it works on the first build, check which branch you're on.",
            "Refactoring is just cleaning your room for adults.",
            "Today's lesson: the emulator lies sometimes. Always test on device.",
            "Coffee count: 3. Bugs fixed: 4. Net positive day.",
            "Paging 3 is actually pleasant once it clicks.",
            "Nothing says Friday like a green CI pipeline.",
            "I renamed a variable and now the feature makes sense.",
            "Kotlin flows are just pipes that never leak (mostly).",
            "Dark mode first. Light mode as an afterthought. Fight me.",
            "Deleted 200 lines of dead code. Best PR of the week.",
            "The bug was a missing null check. It always is.",
        )

    fun users(nowEpochMillis: Long): List<UserEntity> =
        userSpecs.mapIndexed { index, spec ->
            UserEntity(
                id = spec.id,
                username = spec.username,
                displayName = spec.displayName,
                bio = spec.bio,
                avatarUrl = "https://picsum.photos/seed/avatar-${spec.username}/200/200",
                headerUrl = "https://picsum.photos/seed/header-${spec.username}/1200/400",
                followerCount = 0,
                followingCount = 0,
                tweetCount = 0,
                createdAtEpochMillis = nowEpochMillis - TWO_WEEKS_MILLIS - index * 86_400_000L,
            )
        }

    fun tweets(
        users: List<UserEntity>,
        nowEpochMillis: Long,
        random: Random = Random(42)
    ): List<TweetEntity> {
        val tweets = mutableListOf<TweetEntity>()
        repeat(200) { i ->
            val author = users[random.nextInt(users.size)]
            val ageMillis = random.nextLong(0L, TWO_WEEKS_MILLIS)
            tweets +=
                TweetEntity(
                    id = "t-seed-%04d".format(i),
                    authorId = author.id,
                    text = tweetTemplates[random.nextInt(tweetTemplates.size)] + " #" + (i + 1),
                    createdAtEpochMillis = nowEpochMillis - ageMillis,
                    replyCount = random.nextInt(0, 40),
                    repostCount = random.nextInt(0, 25),
                    likeCount = random.nextInt(0, 300),
                    viewCount = random.nextInt(50, 20_000),
                    type = "ORIGINAL",
                    inReplyToTweetId = null,
                    conversationId = null,
                    syncState = "CONFIRMED",
                )
        }
        return tweets
    }

    /** Roughly a third of tweets carry 1-4 seeded picsum images. */
    fun media(
        tweets: List<TweetEntity>,
        random: Random = Random(43)
    ): List<MediaEntity> {
        val media = mutableListOf<MediaEntity>()
        var seed = 0
        tweets.forEach { tweet ->
            if (random.nextInt(3) == 0) {
                repeat(random.nextInt(1, 5)) {
                    seed++
                    media +=
                        MediaEntity(
                            id = "m-${tweet.id}-$it",
                            tweetId = tweet.id,
                            type = "IMAGE",
                            url = "https://picsum.photos/seed/tc-$seed/600/400",
                            localUri = null,
                            width = 600,
                            height = 400,
                            altText = null,
                        )
                }
            }
        }
        return media
    }

    /** Demo follows everyone; a deterministic subset of users follows each other and demo. */
    fun follows(
        users: List<UserEntity>,
        nowEpochMillis: Long
    ): List<FollowEntity> {
        val follows = mutableListOf<FollowEntity>()
        users.filter { it.id != DEMO_USER_ID }.forEach { user ->
            follows += FollowEntity(DEMO_USER_ID, user.id, nowEpochMillis)
        }
        users.filter { it.id != DEMO_USER_ID }.forEachIndexed { index, user ->
            if (index % 2 == 0) follows += FollowEntity(user.id, DEMO_USER_ID, nowEpochMillis)
            val next = users[(index + 2) % users.size]
            if (next.id != user.id) follows += FollowEntity(user.id, next.id, nowEpochMillis)
        }
        return follows
    }

    /** Demo liked ~a quarter of the tweets, plus scattered likes from others. */
    fun likes(
        users: List<UserEntity>,
        tweets: List<TweetEntity>,
        nowEpochMillis: Long,
        random: Random = Random(44)
    ): List<LikeEntity> {
        val likes = mutableListOf<LikeEntity>()
        tweets.forEach { tweet ->
            if (random.nextInt(4) == 0) likes += LikeEntity(DEMO_USER_ID, tweet.id, nowEpochMillis)
            if (random.nextInt(6) == 0) {
                val liker = users[random.nextInt(users.size)]
                if (liker.id != DEMO_USER_ID) likes += LikeEntity(liker.id, tweet.id, nowEpochMillis)
            }
        }
        return likes
    }
}
