package com.twitterclone.feature.feed.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.twitterclone.core.model.Routes
import com.twitterclone.feature.feed.FeedScreen
import com.twitterclone.feature.feed.TweetDetailScreen

/**
 * Feed feature's destinations: the paged home timeline and tweet detail.
 * Called once from the app-level NavHost.
 */
fun NavGraphBuilder.feedGraph(navController: NavController) {
    composable(Routes.FEED) {
        FeedScreen(
            onTweetClick = { tweetId -> navController.navigate(Routes.tweetDetail(tweetId)) },
            onProfileClick = { userId -> navController.navigate(Routes.profile(userId)) },
        )
    }
    composable(
        route = Routes.TWEET_DETAIL,
        arguments = listOf(navArgument(Routes.ARG_TWEET_ID) { type = NavType.StringType }),
    ) {
        TweetDetailScreen(
            onBack = navController::popBackStack,
            onProfileClick = { userId -> navController.navigate(Routes.profile(userId)) },
        )
    }
}
