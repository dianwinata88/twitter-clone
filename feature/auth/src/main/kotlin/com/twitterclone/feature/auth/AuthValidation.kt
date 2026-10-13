package com.twitterclone.feature.auth

/**
 * Pure-Kotlin input validation for the auth forms. Kept Android-free so unit
 * tests exercise it directly on the JVM.
 */
object AuthValidation {
    private val USERNAME_REGEX = Regex("^[A-Za-z0-9_]{3,}$")

    fun isUsernameValid(username: String): Boolean = USERNAME_REGEX.matches(username.trim())

    fun isDisplayNameValid(displayName: String): Boolean = displayName.isNotBlank()

    fun isPasswordValid(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

    private const val MIN_PASSWORD_LENGTH = 4
}
