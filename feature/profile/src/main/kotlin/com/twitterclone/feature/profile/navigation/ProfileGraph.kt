package com.twitterclone.feature.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.twitterclone.core.model.Routes
import com.twitterclone.feature.profile.FollowListScreen
import com.twitterclone.feature.profile.ProfileScreen

/** Profile feature's destinations: profile, self-profile, follow lists. */
fun NavGraphBuilder.profileGraph(navController: NavController) {
    composable(
        route = Routes.PROFILE,
        arguments = listOf(navArgument(Routes.ARG_USER_ID) { type = NavType.StringType }),
    ) {
        ProfileScreen(
            onBackClick = { navController.navigateUp() },
            onTweetClick = { tweetId -> navController.navigate(Routes.tweetDetail(tweetId)) },
            onProfileClick = { userId -> navController.navigate(Routes.profile(userId)) },
            onFollowListClick = { userId, tab ->
                navController.navigate(Routes.followList(userId, tab))
            },
        )
    }
    composable(Routes.PROFILE_ME) {
        // ProfileViewModel resolves a missing {userId} arg to the logged-in user.
        ProfileScreen(
            onBackClick = { navController.navigateUp() },
            onTweetClick = { tweetId -> navController.navigate(Routes.tweetDetail(tweetId)) },
            onProfileClick = { userId -> navController.navigate(Routes.profile(userId)) },
            onFollowListClick = { userId, tab ->
                navController.navigate(Routes.followList(userId, tab))
            },
        )
    }
    composable(
        route = Routes.FOLLOW_LIST,
        arguments =
            listOf(
                navArgument(Routes.ARG_USER_ID) { type = NavType.StringType },
                navArgument(Routes.ARG_TAB) { type = NavType.StringType },
            ),
    ) {
        FollowListScreen(
            onBackClick = { navController.navigateUp() },
            onUserClick = { userId -> navController.navigate(Routes.profile(userId)) },
        )
    }
}
