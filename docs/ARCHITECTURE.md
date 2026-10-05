# Twitter Clone — Architecture

Greenfield native Android Twitter/X clone. Phase 1 ships a complete mock
backend (Room + DataStore + Paging 3) behind stable repository contracts, so
feature work and a future real backend are both drop-in.

- Package root: `com.twitterclone`
- App name: "Twitter Clone"
- Build: Gradle KTS, version catalog at `gradle/libs.versions.toml`,
  `compileSdk`/`targetSdk` 37, `minSdk` 26, Kotlin 2.3.x + KSP (no kapt),
  Compose BOM + Material 3, Hilt, Room, Paging 3, Coil 3, WorkManager,
  Retrofit/OkHttp/kotlinx-serialization (interfaces only).

## Module map

| Module | Contains |
|---|---|
| `:app` | `TwitterCloneApp` (`@HiltAndroidApp`, seeds DB + demo login), `MainActivity`, `AppNavHost` |
| `:core:model` | Pure Kotlin/JVM module. Domain models (`User`, `Tweet`, `Media`, `Page`, enums), `Routes`, repository **interfaces** — the contracts every feature codes against |
| `:core:common` | `@Dispatcher`/`@ApplicationScope` qualifiers + providers, `Async<T>` flow wrapper, `safeCall` |
| `:core:database` | Room DB `twitter.db`: `UserEntity`, `TweetEntity`, `MediaEntity`, `LikeEntity` (unique `userId`+`tweetId`), `FollowEntity` (unique `followerId`+`followeeId`), `RemoteKeysEntity`; DAOs incl. hydrated-tweet queries (author + media + viewer state) and the home-timeline `PagingSource`; `SeedData` + `DatabaseSeeder` |
| `:core:datastore` | `SessionManager`: Preferences DataStore holding `session_token` + `user_id` |
| `:core:network` | Retrofit API interfaces + kotlinx.serialization DTOs documenting the future REST surface. **No real calls** — nothing is bound to a base URL |
| `:core:data` | `Fake*Repository` implementations bound in `DataModule`; `FailureInjector` |
| `:core:ui` | `TwitterCloneTheme` (light + dark M3), shared composables incl. `TweetCard`, `relativeTime()` |
| `:feature:auth` | `NavGraphBuilder.authGraph(navController)` — splash/login/register stubs |
| `:feature:feed` | `NavGraphBuilder.feedGraph(navController)` — working placeholder feed (real paging + likes) + tweet-detail stub |
| `:feature:compose-tweet` | `NavGraphBuilder.composeTweetGraph(navController)` — compose stub |
| `:feature:profile` | `NavGraphBuilder.profileGraph(navController)` — profile/me/follow-list stubs |

Dependency direction: `feature:*` and `:app` depend on `:core:model` (+ `:core:ui`); only
`:app` depends on `:core:data`/`:core:database`. Features must never depend on
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
enum class TweetType { ORIGINAL, REPLY, REPOST, QUOTE }  // MVP uses ORIGINAL only; reserved
data class Media(val id: String, val type: MediaType, val url: String, val localUri: String? = null,
  val width: Int, val height: Int, val altText: String? = null)
enum class MediaType { IMAGE, GIF, VIDEO }  // MVP uses IMAGE
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
  suspend fun followers(userId: String): List<User>
  suspend fun following(userId: String): List<User>
  suspend fun updateProfile(displayName: String, bio: String): Result<User>
}
```

`Instant` is `kotlinx.datetime.Instant`; `Result` is `kotlin.Result`;
`PagingData` is `androidx.paging.PagingData`.

## Routes (`:core:model` `object Routes`)

Cross-module navigation goes through route strings — never hard-code route
literals. Argument names are exposed as `Routes.ARG_*` for `navArgument` and
`arguments?.getString(...)`.

| Constant | Route | Helper |
|---|---|---|
| `Routes.AUTH_SPLASH` | `"auth/splash"` | — |
| `Routes.AUTH_LOGIN` | `"auth/login"` | — |
| `Routes.AUTH_REGISTER` | `"auth/register"` | — |
| `Routes.FEED` | `"feed"` | — |
| `Routes.TWEET_DETAIL` | `"feed/tweet/{tweetId}"` | `Routes.tweetDetail(id)` |
| `Routes.COMPOSE_TWEET` | `"compose"` | — |
| `Routes.PROFILE` | `"profile/{userId}"` | `Routes.profile(userId)` |
| `Routes.PROFILE_ME` | `"profile/me"` | — |
| `Routes.FOLLOW_LIST` | `"profile/{userId}/follows/{tab}"` | `Routes.followList(userId, tab)` |

`{tab}` ∈ `Routes.TAB_FOLLOWERS` (`"followers"`) | `Routes.TAB_FOLLOWING`
(`"following"`).

## Navigation pattern

Every feature module exposes exactly one graph extension:

```kotlin
fun NavGraphBuilder.<feature>Graph(navController: NavController) { /* composable(...) {} */ }
```

`AppNavHost` (in `:app`) already calls `authGraph`, `feedGraph`,
`composeTweetGraph`, `profileGraph` — **feature agents must not need to edit
`:app` or other modules.** They replace their stub screen bodies in place.

Exception: **the auth feature owns the splash/auth gate.** It may change
`AppNavHost`'s `startDestination` from `Routes.FEED` to `Routes.AUTH_SPLASH` —
that is the one sanctioned `:app` edit.

## Mock backend

- **Seeding**: `DatabaseSeeder.seedIfEmpty()` runs on app start (idempotent;
  skips if any user exists): ~8 users incl. `demo`, ~200 tweets spread over
  two weeks, a follow graph centered on `demo`, likes, and 1–4
  `https://picsum.photos/seed/tc-<n>/600/400` images on ~⅓ of tweets.
- **Demo session**: on first launch `:app` calls
  `AuthRepository.login("demo", "demo")` so the app boots into a populated
  feed. `SessionManager` persists `session_token` + `user_id` in DataStore.
- **Latency**: every repository call goes through `simulateLatency()`
  (300–800 ms) so loading states are real.
- **Failure injection**: inject `FailureInjector` (singleton) and set
  `shouldFail = true` — every mutating repo call then fails with
  `SimulatedBackendException`. `postTweet` still writes its row first and
  flips `syncState` to `FAILED`, so rollback/retry UI is exercised for real.
- **Optimistic writes**: `toggleLike`/`toggleFollow` flip the join rows
  (`INSERT OR IGNORE` on composite PKs → unique-constraint safe) and
  recompute denormalized counts. Room `PagingSource` invalidation pushes the
  change to the timeline automatically; `refreshHome()` is a no-op.

## Naming conventions

- Gradle module `:feature:foo-bar` → package `com.twitterclone.feature.foobar`, directory `feature/foo-bar/`.
- `XxxEntity` = Room row, `XxxDto` = wire model, `Xxx` (no suffix) = domain model.
- `HydratedTweet` = a DAO projection joining author + media + viewer state.
- Repos: contract `XxxRepository` in `:core:model.repository`, impl `FakeXxxRepository` in `:core:data.repository`, binding in `DataModule`.
- ViewModels are `@HiltViewModel`, injected via `androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel()`.
- Screens are PascalCase `@Composable`s; each lives behind a `NavGraphBuilder` extension.

## CI

`.github/workflows/ci.yml`: checkout → setup-java (temurin 17) →
`gradle/actions/setup-gradle` → `./gradlew ktlintCheck` →
`./gradlew testDebugUnitTest` → `./gradlew assembleDebug`.

## Local setup

Needs JDK 17 and an Android SDK with `platforms;android-37` +
`build-tools;36.0.0+`. Point `local.properties` `sdk.dir` at it (or set
`ANDROID_HOME`). `./gradlew assembleDebug` produces
`app/build/outputs/apk/debug/app-debug.apk`; it launches straight into the
seeded feed (auto-login as `demo`).
