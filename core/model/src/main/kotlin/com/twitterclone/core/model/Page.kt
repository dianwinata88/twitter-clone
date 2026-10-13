package com.twitterclone.core.model

/** Cursor pagination envelope used by non-paged endpoints. */
data class Page<T>(
    val items: List<T>,
    val nextCursor: String?
)
