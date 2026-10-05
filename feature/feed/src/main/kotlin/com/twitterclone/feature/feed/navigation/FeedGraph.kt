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
 * Feed feature's destinations. Called once from the app-level NavHost;
 * feature agents replace the screen bodies, not this wiring contract.
 */
fun NavGraphBuilder.feedGraph(navController: NavController) {
    composable(Routes.FEED) {
        FeedScreen(
            onTweetClick = { tweetId -> navController.navigate(Routes.tweetDetail(tweetId)) },
            onProfileClick = { userId -> navController.navigate(Routes.profile(userId)) },
            onComposeClick = { navController.navigate(Routes.COMPOSE_TWEET) },
        )
    }
    composable(
        route = Routes.TWEET_DETAIL,
        arguments = listOf(navArgument(Routes.ARG_TWEET_ID) { type = NavType.StringType }),
    ) { entry ->
        TweetDetailScreen(
            tweetId = entry.arguments?.getString(Routes.ARG_TWEET_ID).orEmpty(),
        )
    }
}
