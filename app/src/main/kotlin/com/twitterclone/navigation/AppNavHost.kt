package com.twitterclone.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.twitterclone.core.model.Routes
import com.twitterclone.core.ui.component.PlaceholderScreen
import com.twitterclone.feature.auth.navigation.authGraph
import com.twitterclone.feature.composetweet.navigation.composeTweetGraph
import com.twitterclone.feature.feed.navigation.feedGraph
import com.twitterclone.feature.profile.navigation.profileGraph
import com.twitterclone.feature.search.navigation.searchGraph

/**
 * Root navigation host. Every feature module contributes a
 * `NavGraphBuilder.*Graph(navController)` extension; this is the only place
 * they are wired together. The Alerts and Messages bottom-nav tabs are V1
 * placeholders owned here until those features exist.
 *
 * Feature agents must not need to edit this file — the exception is the auth
 * feature, which owns the splash/auth gate and may change [NavHost]'s
 * startDestination to [Routes.SPLASH].
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.FEED,
        modifier = modifier,
    ) {
        authGraph(navController)
        feedGraph(navController)
        composeTweetGraph(navController)
        profileGraph(navController)
        searchGraph(navController)

        composable(Routes.ALERTS) {
            PlaceholderScreen(name = "Alerts", hint = "Notifications tab — V1 placeholder.")
        }
        composable(Routes.MESSAGES) {
            PlaceholderScreen(name = "Messages", hint = "Direct messages tab — V1 placeholder.")
        }
    }
}
