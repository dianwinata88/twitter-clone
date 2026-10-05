package com.twitterclone.feature.composetweet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TweetLengthCounterTest {
    @Test
    fun `empty text counts zero`() {
        assertEquals(0, TweetLengthCounter.count(""))
    }

    @Test
    fun `ascii text counts one unit per character`() {
        assertEquals(5, TweetLengthCounter.count("hello"))
        assertEquals(
            TweetLengthCounter.MAX_LENGTH,
            TweetLengthCounter.count("a".repeat(TweetLengthCounter.MAX_LENGTH)),
        )
    }

    @Test
    fun `single emoji surrogate pair counts as one`() {
        assertEquals(1, TweetLengthCounter.count("👍"))
        assertEquals(3, TweetLengthCounter.count("😀😀😀"))
        assertEquals(3, TweetLengthCounter.count("a👍b"))
    }

    @Test
    fun `combining sequence counts as one grapheme cluster`() {
        // "e" + combining acute accent renders as a single character.
        assertEquals(1, TweetLengthCounter.count("é"))
    }

    @Test
    fun `remaining is max length minus count`() {
        assertEquals(TweetLengthCounter.MAX_LENGTH, TweetLengthCounter.remaining(""))
        assertEquals(TweetLengthCounter.MAX_LENGTH - 5, TweetLengthCounter.remaining("hello"))
        assertTrue(TweetLengthCounter.remaining("a".repeat(TweetLengthCounter.MAX_LENGTH + 1)) < 0)
    }
}
