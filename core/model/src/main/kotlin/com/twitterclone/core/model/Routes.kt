package com.twitterclone.core.model

/**
 * Navigation route contracts shared across feature modules.
 *
 * Feature agents navigate cross-module by route string; never hard-code route
 * literals outside this object. Argument names used inside route templates are
 * exposed as [ARG_TWEET_ID], [ARG_USER_ID] and [ARG_TAB] so both the graph
 * definition and the argument lookup stay in sync.
 */
object Routes {
    const val AUTH_SPLASH = "auth/splash"
    const val AUTH_LOGIN = "auth/login"
    const val AUTH_REGISTER = "auth/register"

    const val FEED = "feed"
    const val TWEET_DETAIL = "feed/tweet/{tweetId}"

    const val COMPOSE_TWEET = "compose"

    const val PROFILE = "profile/{userId}"
    const val PROFILE_ME = "profile/me"
    const val FOLLOW_LIST = "profile/{userId}/follows/{tab}"

    const val ARG_TWEET_ID = "tweetId"
    const val ARG_USER_ID = "userId"
    const val ARG_TAB = "tab"

    /** Allowed values for the `{tab}` argument of [FOLLOW_LIST]. */
    const val TAB_FOLLOWERS = "followers"
    const val TAB_FOLLOWING = "following"

    fun tweetDetail(id: String): String = "feed/tweet/$id"

    fun profile(userId: String): String = "profile/$userId"

    /** @param tab [TAB_FOLLOWERS] or [TAB_FOLLOWING] */
    fun followList(
        userId: String,
        tab: String
    ): String = "profile/$userId/follows/$tab"
}
