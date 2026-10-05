package com.twitterclone.core.network.api

import com.twitterclone.core.network.model.AuthResponse
import com.twitterclone.core.network.model.LoginRequest
import com.twitterclone.core.network.model.PageDto
import com.twitterclone.core.network.model.PostTweetRequest
import com.twitterclone.core.network.model.RegisterRequest
import com.twitterclone.core.network.model.ToggleResponse
import com.twitterclone.core.network.model.TweetDto
import com.twitterclone.core.network.model.UpdateProfileRequest
import com.twitterclone.core.network.model.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Future REST surface. Interfaces only — nothing is wired to a base URL yet,
 * and the fake repositories in `:core:data` do not call these.
 */
interface AuthApi {
    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): AuthResponse

    @POST("auth/register")
    suspend fun register(
        @Body body: RegisterRequest
    ): AuthResponse

    @POST("auth/logout")
    suspend fun logout()

    @GET("auth/me")
    suspend fun me(): UserDto
}

interface TimelineApi {
    @GET("timeline/home")
    suspend fun home(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
    ): PageDto<TweetDto>

    @GET("users/{userId}/tweets")
    suspend fun userTweets(
        @Path("userId") userId: String,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
    ): PageDto<TweetDto>
}

interface TweetApi {
    @GET("tweets/{tweetId}")
    suspend fun getTweet(
        @Path("tweetId") tweetId: String
    ): TweetDto

    @POST("tweets")
    suspend fun postTweet(
        @Body body: PostTweetRequest
    ): TweetDto

    @POST("tweets/{tweetId}/like")
    suspend fun toggleLike(
        @Path("tweetId") tweetId: String
    ): ToggleResponse
}

interface UserApi {
    @GET("users/{userId}")
    suspend fun getUser(
        @Path("userId") userId: String
    ): UserDto

    @GET("users/by-handle/{username}")
    suspend fun getUserByHandle(
        @Path("username") username: String
    ): UserDto

    @POST("users/{userId}/follow")
    suspend fun toggleFollow(
        @Path("userId") userId: String
    ): ToggleResponse

    @GET("users/{userId}/followers")
    suspend fun followers(
        @Path("userId") userId: String,
        @Query("cursor") cursor: String? = null,
    ): PageDto<UserDto>

    @GET("users/{userId}/following")
    suspend fun following(
        @Path("userId") userId: String,
        @Query("cursor") cursor: String? = null,
    ): PageDto<UserDto>

    @PUT("users/me")
    suspend fun updateProfile(
        @Body body: UpdateProfileRequest
    ): UserDto
}
