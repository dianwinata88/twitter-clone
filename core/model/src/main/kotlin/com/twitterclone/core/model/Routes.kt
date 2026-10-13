package com.twitterclone.core.model

/**
 * Navigation route contracts shared across feature modules.
 *
 * Feature agents navigate cross-module by route string; never hard-code route
 * literals outside this object. Argument names used inside route templates are
 * exposed as [ARG_TWEET_ID] and [ARG_USER_ID] so both the graph definition and
 * the argument lookup stay in sync.
 */
object Routes {
    const val SPLASH = "auth/splash"
    const val AUTH_LOGIN = "auth/login"
    const val AUTH_REGISTER = "auth/register"

    const val FEED = "feed"
    const val TWEET_DETAIL = "feed/tweet/{tweetId}"

    const val COMPOSE = "compose"

    const val PROFILE = "profile/{userId}"
    const val PROFILE_ME = "profile/me"
    const val FOLLOWERS = "profile/{userId}/followers"
    const val FOLLOWING = "profile/{userId}/following"

    const val SEARCH = "search"

    /** V1 placeholder tabs wired into the bottom nav. */
    const val ALERTS = "alerts"
    const val MESSAGES = "messages"

    const val ARG_TWEET_ID = "tweetId"
    const val ARG_USER_ID = "userId"

    fun tweetDetail(id: String): String = "feed/tweet/$id"

    fun profile(userId: String): String = "profile/$userId"

    fun followers(userId: String): String = "profile/$userId/followers"

    fun following(userId: String): String = "profile/$userId/following"
}
