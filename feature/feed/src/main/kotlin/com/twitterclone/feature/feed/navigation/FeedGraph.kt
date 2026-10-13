package com.twitterclone.feature.feed.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.twitterclone.core.model.Routes
import com.twitterclone.core.ui.component.PlaceholderScreen

/**
 * Feed feature's destinations. V1 stub: the feed agent replaces these
 * placeholders with the real home timeline (paged, pull-to-refresh) and tweet
 * detail. Called once from the app-level NavHost.
 */
fun NavGraphBuilder.feedGraph(navController: NavController) {
    composable(Routes.FEED) {
        PlaceholderScreen(name = "Feed", hint = "Home timeline — paged, pull-to-refresh.")
    }
    composable(
        route = Routes.TWEET_DETAIL,
        arguments = listOf(navArgument(Routes.ARG_TWEET_ID) { type = NavType.StringType }),
    ) { entry ->
        PlaceholderScreen(
            name = "Tweet detail",
            hint = "tweetId = ${entry.arguments?.getString(Routes.ARG_TWEET_ID).orEmpty()}",
        )
    }
}
