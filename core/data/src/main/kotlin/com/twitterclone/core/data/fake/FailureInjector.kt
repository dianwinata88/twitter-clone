package com.twitterclone.core.data.fake

import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

class SimulatedBackendException : IOException("Simulated backend failure (FailureInjector.shouldFail = true)")

/**
 * Mutable switch that makes every fake repository call fail. Flip
 * [shouldFail] from tests or a debug UI to exercise rollback / error paths —
 * e.g. a `postTweet` that stays `FAILED`, or a `toggleLike` that must be
 * reverted by the UI layer.
 */
@Singleton
class FailureInjector
    @Inject
    constructor() {
        @Volatile
        var shouldFail: Boolean = false

        /** Throws [SimulatedBackendException] when [shouldFail] is set. */
        fun throwIfEnabled() {
            if (shouldFail) throw SimulatedBackendException()
        }
    }
