package com.twitterclone.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.twitterclone.core.model.Routes
import com.twitterclone.feature.auth.navigation.authGraph
import com.twitterclone.feature.composetweet.navigation.composeTweetGraph
import com.twitterclone.feature.feed.navigation.feedGraph
import com.twitterclone.feature.profile.navigation.profileGraph

/**
 * Root navigation host. Every feature module contributes a
 * `NavGraphBuilder.*Graph(navController)` extension; this is the only place
 * they are wired together. Feature agents must not need to edit this file —
 * the exception is the auth feature, which owns the splash/auth gate and may
 * change [NavHost]'s startDestination to [Routes.AUTH_SPLASH].
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.AUTH_SPLASH,
        modifier = modifier,
    ) {
        authGraph(navController)
        feedGraph(navController)
        composeTweetGraph(navController)
        profileGraph(navController)
    }
}
