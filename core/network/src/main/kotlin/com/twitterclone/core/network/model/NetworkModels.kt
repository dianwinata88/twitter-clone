package com.twitterclone.core.network.model

import kotlinx.serialization.Serializable

// DTOs documenting the wire format of the future REST backend.
// No calls are made in the MVP; repositories are backed by Room seeds instead.

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val displayName: String,
    val bio: String = "",
    val avatarUrl: String? = null,
    val headerUrl: String? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val tweetCount: Int = 0,
    val createdAtEpochMillis: Long,
    val followedByMe: Boolean = false,
)

@Serializable
data class MediaDto(
    val id: String,
    val type: String,
    val url: String,
    val width: Int = 0,
    val height: Int = 0,
    val altText: String? = null,
)

@Serializable
data class TweetDto(
    val id: String,
    val author: UserDto,
    val text: String,
    val media: List<MediaDto> = emptyList(),
    val createdAtEpochMillis: Long,
    val replyCount: Int = 0,
    val repostCount: Int = 0,
    val likeCount: Int = 0,
    val viewCount: Int = 0,
    val likedByMe: Boolean = false,
    val repostedByMe: Boolean = false,
    val type: String = "ORIGINAL",
    val inReplyToTweetId: String? = null,
    val conversationId: String? = null,
    val syncState: String = "CONFIRMED",
)

@Serializable
data class PageDto<T>(
    val items: List<T>,
    val nextCursor: String? = null,
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val displayName: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto
)

@Serializable
data class PostTweetRequest(
    val text: String,
    val mediaIds: List<String> = emptyList()
)

@Serializable
data class UpdateProfileRequest(
    val displayName: String,
    val bio: String
)

@Serializable
data class ToggleResponse(
    val value: Boolean
)
