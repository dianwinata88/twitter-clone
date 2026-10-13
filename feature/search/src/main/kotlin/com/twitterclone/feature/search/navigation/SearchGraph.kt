package com.twitterclone.feature.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.twitterclone.core.model.Routes
import com.twitterclone.feature.search.SearchScreen

/**
 * Search feature's destinations. A single [Routes.SEARCH] screen reached from
 * the bottom nav; it navigates out to tweet detail and profiles via route
 * helpers on [Routes].
 */
fun NavGraphBuilder.searchGraph(navController: NavController) {
    composable(Routes.SEARCH) {
        SearchScreen(
            onTweetClick = { tweetId -> navController.navigate(Routes.tweetDetail(tweetId)) },
            onProfileClick = { userId -> navController.navigate(Routes.profile(userId)) },
        )
    }
}
