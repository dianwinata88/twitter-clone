package com.twitterclone.core.ui.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * X-style relative timestamp: `45s`, `3m`, `2h`, `6d`, or `MMM d` past a week.
 */
fun Instant.relativeTime(now: Instant = Clock.System.now()): String {
    val elapsed = now - this
    return when {
        elapsed < 1.minutes -> "${elapsed.inWholeSeconds.coerceAtLeast(0)}s"
        elapsed < 1.hours -> "${elapsed.inWholeMinutes}m"
        elapsed < 1.days -> "${elapsed.inWholeHours}h"
        elapsed < 7.days -> "${elapsed.inWholeDays}d"
        else -> {
            val dt = toString() // ISO-8601: YYYY-MM-DDTHH:MM:SSZ
            val month = dt.substring(5, 7).toInt()
            val day = dt.substring(8, 10).toInt()
            "${MONTHS[month - 1]} $day"
        }
    }
}

private val MONTHS =
    listOf(
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec",
    )
