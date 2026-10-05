package com.twitterclone.core.common.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * UI-facing stream state. Distinct from [kotlin.Result], which the repository
 * contracts use for one-shot suspend calls.
 */
sealed interface Async<out T> {
    data object Loading : Async<Nothing>

    data class Success<T>(
        val data: T
    ) : Async<T>

    data class Error(
        val throwable: Throwable
    ) : Async<Nothing>
}

/** Wraps each emission in [Async], emitting [Async.Loading] first. */
fun <T> Flow<T>.asAsync(): Flow<Async<T>> =
    map<T, Async<T>> { Async.Success(it) }
        .onStart { emit(Async.Loading) }
        .catch { emit(Async.Error(it)) }

/** Runs [block], mapping thrown exceptions to [Result.failure]. */
suspend inline fun <T> safeCall(crossinline block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (t: Throwable) {
        if (t is kotlinx.coroutines.CancellationException) throw t
        Result.failure(t)
    }
