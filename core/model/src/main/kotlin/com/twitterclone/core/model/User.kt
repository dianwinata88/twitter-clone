package com.twitterclone.core.model

import kotlinx.datetime.Instant

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val bio: String,
    val avatarUrl: String?,
    val headerUrl: String?,
    val followerCount: Int,
    val followingCount: Int,
    val tweetCount: Int,
    val createdAt: Instant,
    val followedByMe: Boolean = false,
)
