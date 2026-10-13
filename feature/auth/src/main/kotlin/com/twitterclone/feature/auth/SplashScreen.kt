package com.twitterclone.feature.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

/**
 * X-style splash: the wordmark on pure black while the session resolves.
 * Routes to the feed when a session exists, otherwise to login. The minimum
 * display delay keeps the logo visible instead of flashing through.
 */
@Composable
fun SplashScreen(
    onLoggedIn: () -> Unit,
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()

    LaunchedEffect(sessionState) {
        if (sessionState == SessionState.Unknown) return@LaunchedEffect
        delay(MIN_SPLASH_MILLIS)
        when (sessionState) {
            SessionState.LoggedIn -> onLoggedIn()
            SessionState.LoggedOut -> onLoggedOut()
            SessionState.Unknown -> Unit
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.auth_wordmark),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

private const val MIN_SPLASH_MILLIS = 800L
