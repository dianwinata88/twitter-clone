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
    fun `follow list routes build concrete routes`() {
        assertEquals("profile/u42/followers", Routes.followers("u42"))
        assertEquals("profile/u42/following", Routes.following("u42"))
    }
}
