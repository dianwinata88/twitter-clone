package com.twitterclone.core.data.fake

import kotlinx.coroutines.delay
import kotlin.random.Random

/** Simulates realistic network latency for the fake backend. */
internal suspend fun simulateLatency(range: LongRange = 300L..800L) {
    delay(Random.nextLong(range.first, range.last + 1))
}
