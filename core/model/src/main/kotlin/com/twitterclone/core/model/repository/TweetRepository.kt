package com.twitterclone.core.model.repository

import com.twitterclone.core.model.Tweet

interface TweetRepository {
    suspend fun getTweet(id: String): Tweet?

    /** @param mediaLocalUris local URIs of images to attach (MVP: images only). */
    suspend fun postTweet(
        text: String,
        mediaLocalUris: List<String>
    ): Result<Tweet>

    /** @return the new `likedByMe` value for the tweet. */
    suspend fun toggleLike(tweetId: String): Result<Boolean>
}
