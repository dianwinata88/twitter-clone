package com.twitterclone.feature.composetweet

import java.text.BreakIterator

/**
 * Counts the user-perceived length of a draft tweet.
 *
 * Current approximation: one unit per extended grapheme cluster (what a user
 * thinks of as "one character"), via [BreakIterator]. Emoji counting a single
 * UTF-16 surrogate pair as one unit and combining sequences like `é`
 * (`e` + U+0301) as one unit. Flag pairs and ZWJ emoji sequences are counted
 * per code point — X's weighted rules (URLs count as 23, CJK counts double,
 * etc.) will replace [count]'s body later without touching callers.
 */
object TweetLengthCounter {
    const val MAX_LENGTH = 280

    fun count(text: String): Int {
        if (text.isEmpty()) return 0
        val breaks = BreakIterator.getCharacterInstance()
        breaks.setText(text)
        var clusters = 0
        while (breaks.next() != BreakIterator.DONE) {
            clusters++
        }
        return clusters
    }

    fun remaining(text: String): Int = MAX_LENGTH - count(text)
}
