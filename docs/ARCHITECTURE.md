# Twitter Clone — Architecture

Greenfield native Android Twitter/X clone. V1 ships a complete mock backend
(Room + DataStore + Paging 3) behind stable repository contracts, so feature
work and a future real backend are both drop-in.

- Package root: `com.twitterclone`
- App name: "Twitter Clone"
- Build: Gradle KTS, version catalog at `gradle/libs.versions.toml`,
  `compileSdk`/`targetSdk` 37, `minSdk` 26, Kotlin 2.3.x + KSP (no kapt),
  Compose BOM + Material 3, Hilt, Room, Paging 3, Coil 3, DataStore
  (preferences), Navigation Compose.
- Theme: **X Dark only** — pure-black `#000` background, white/gray text,
  X-blue `#1D9BF0` accents, pink `#F91880` like (`core/ui/theme/Theme.kt`).

## Module map

| Module | Contains |
|---|---|
| `:app` | `TwitterCloneApp` (`@HiltAndroidApp`, calls `DatabaseSeeder.seedIfEmpty` on start), `MainActivity` (X-style scaffold: wordmark top bar, icon-only bottom nav, compose FAB), `AppNavHost`, Alerts/Messages V1 placeholders |
| `:core:model` | Pure Kotlin/JVM module. Domain models (`User`, `Tweet`, `Media`, `Page`, enums), `Routes`, repository **interfaces** — the contracts every feature codes against |
| `:core:common` | `@Dispatcher`/`@ApplicationScope` qualifiers + providers, `Async<T>` flow wrapper, `safeCall` |
| `:core:database` | Room DB `twitter.db`: `UserEntity`, `TweetEntity`, `MediaEntity`, `LikeEntity` (unique `userId`+`tweetId`), `FollowEntity` (unique `followerId`+`followeeId`), `RemoteKeysEntity`; DAOs incl. hydrated-tweet queries (author + media + viewer state), the home-timeline `PagingSource`, and search queries; `SeedData` + `DatabaseSeeder` |
| `:core:datastore` | `SessionManager`: Preferences DataStore holding `session_token` + `user_id` |
| `:core:data` | `Fake*Repository` implementations bound in `DataModule`; `simulateLatency()` 300–800 ms; `FailureInjector` |
| `:core:ui` | `TwitterCloneTheme` (X Dark), shared composables (`TweetCard`, avatar via Coil, `PlaceholderScreen`), `relativeTime()` |
| `:feature:auth` | `NavGraphBuilder.authGraph(navController)` — splash/login/register stubs |
| `:feature:feed` | `NavGraphBuilder.feedGraph(navController)` — home timeline + tweet-detail stubs |
| `:feature:compose-tweet` | `NavGraphBuilder.composeTweetGraph(navController)` — composer stub |
| `:feature:profile` | `NavGraphBuilder.profileGraph(navController)` — profile/me/followers/following stubs |
| `:feature:search` | `NavGraphBuilder.searchGraph(navController)` — search stub |

Dependency direction: `feature:*` and `:app` depend on `:core:model`,
`:core:ui` (+ `:core:common`); **only `:app`** depends on
`:core:data`/`:core:database`/`:core:datastore`. Features must never depend on
each other or on `Fake*` implementations — Hilt binds the contracts in
`:core:data`'s `DataModule`.

## Contracts (`:core:model`)

```kotlin
data class User(
  val id: String, val username: String, val displayName: String,
  val bio: String, val avatarUrl: String?, val headerUrl: String?,
  val followerCount: Int, val followingCount: Int, val tweetCount: Int,
  val createdAt: Instant, val followedByMe: Boolean = false,
)
enum class SyncState { PENDING, CONFIRMED, FAILED }
enum class TweetType { ORIGINAL, REPLY, REPOST, QUOTE }  // V1 uses ORIGINAL only
data class Media(val id: String, val type: MediaType, val url: String,
  val localUri: String? = null, val width: Int, val height: Int, val altText: String? = null)
enum class MediaType { IMAGE, GIF, VIDEO }  // V1 uses IMAGE
data class Tweet(
  val id: String, val author: User, val text: String, val media: List<Media>,
  val createdAt: Instant, val replyCount: Int, val repostCount: Int,
  val likeCount: Int, val viewCount: Int = 0,
  val likedByMe: Boolean, val repostedByMe: Boolean = false,
  val type: TweetType = TweetType.ORIGINAL,
  val inReplyToTweetId: String? = null, val conversationId: String? = null,
  val syncState: SyncState = SyncState.CONFIRMED,
)
data class Page<T>(val items: List<T>, val nextCursor: String?)

interface AuthRepository {
  val currentUser: Flow<User?>          // null = logged out
  suspend fun login(username: String, password: String): Result<User>
  suspend fun register(username: String, displayName: String, password: String): Result<User>
  suspend fun logout()
}
interface TimelineRepository {
  fun homeTimeline(): Flow<PagingData<Tweet>>
  fun userTweets(userId: String): Flow<PagingData<Tweet>>
  suspend fun refreshHome()
}
interface TweetRepository {
  suspend fun getTweet(id: String): Tweet?
  suspend fun postTweet(text: String, mediaLocalUris: List<String>): Result<Tweet>
  suspend fun toggleLike(tweetId: String): Result<Boolean>  // returns new likedByMe
}
interface UserRepository {
  suspend fun getUser(id: String): User?
  suspend fun getUserByHandle(username: String): User?
  suspend fun toggleFollow(userId: String): Result<Boolean> // returns new followedByMe
  fun followers(userId: String): Flow<PagingData<User>>
  fun following(userId: String): Flow<PagingData<User>>
  suspend fun updateProfile(displayName: String, bio: String): Result<User>
}
interface SearchRepository {
  fun searchTweets(query: String): Flow<PagingData<Tweet>>
  fun searchUsers(query: String): Flow<PagingData<User>>
  fun trending(limit: Int = 10): Flow<List<Tweet>>
}
```

`Routes`: SPLASH, AUTH_LOGIN, AUTH_REGISTER, FEED, TWEET_DETAIL/{tweetId},
COMPOSE, PROFILE/{userId}, PROFILE_ME, FOLLOWERS/{userId},
FOLLOWING/{userId}, SEARCH, plus placeholder ALERTS, MESSAGES. Helper
builders (`tweetDetail(id)`, `profile(id)`, `followers(id)`, `following(id)`)
produce concrete route strings — never hard-code literals outside `Routes`.

## Dummy SQLite database

- `DatabaseSeeder.seedIfEmpty()` runs on app start (idempotent: no-op once any
  user row exists). Seeds 8 users incl. `demo` (password `demo`), 200 tweets
  over two weeks, a follow graph centered on `demo`, likes, and 1–4
  `https://picsum.photos/seed/tc-<n>/600/400` images on ~⅓ of tweets. Tweet
  text is varied + hashtagged so search has real material.
- Hydrated-tweet DAO projection: tweet + author + media + `likedByMe`
  (`EXISTS` join on the session user) + reserved `repostedByMe`.
  `PagingSource`s back the timeline (`createdAt DESC`), per-user tweets,
  tweet search, user search; `trending` is a `Flow<List<HydratedTweet>>`.
- Optimistic writes: `toggleLike`/`toggleFollow` flip join rows
  (`INSERT OR IGNORE` on composite PKs → unique-safe) and recompute
  denormalized counts; Room invalidation propagates to the UI.
- `postTweet` inserts `syncState=PENDING` then flips `CONFIRMED` (or `FAILED`
  under `FailureInjector`); local image URIs persist on `MediaEntity.localUri`.

## Conventions

- MVVM + UDF. ViewModels via `hiltViewModel()`; collect with
  `collectAsStateWithLifecycle`; hoisted state; feature screens never touch
  DAOs/entities — only `core:model` contracts.
- Viewer identity always comes from `SessionManager.currentUserId`; fake
  repos return empty paging/flows while logged out.
- `./gradlew ktlintFormat` before every commit; CI runs `ktlintCheck`,
  `testDebugUnitTest`, `assembleDebug`.
- JVM unit tests use `kotlinx-coroutines-test`; Room tests run under
  Robolectric (see `DatabaseSeederTest`).
- Conventional-commit messages.
