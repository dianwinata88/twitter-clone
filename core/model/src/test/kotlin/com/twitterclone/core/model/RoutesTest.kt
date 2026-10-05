package com.twitterclone.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RoutesTest {
    @Test
    fun `tweetDetail builds the concrete route`() {
        assertEquals("feed/tweet/abc123", Routes.tweetDetail("abc123"))
    }

    @Test
    fun `profile builds the concrete route`() {
        assertEquals("profile/u42", Routes.profile("u42"))
    }

    @Test
    fun `followList builds the concrete route`() {
        assertEquals("profile/u42/follows/followers", Routes.followList("u42", Routes.TAB_FOLLOWERS))
        assertEquals("profile/u42/follows/following", Routes.followList("u42", Routes.TAB_FOLLOWING))
    }
}
