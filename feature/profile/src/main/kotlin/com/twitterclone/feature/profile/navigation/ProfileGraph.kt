package com.twitterclone.feature.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.twitterclone.core.model.Routes
import com.twitterclone.feature.profile.FollowListRoute
import com.twitterclone.feature.profile.FollowListType
import com.twitterclone.feature.profile.ProfileRoute

/**
 * Profile feature's destinations: `PROFILE/{userId}`, the `PROFILE_ME`
 * bottom-nav alias, and the `FOLLOWERS`/`FOLLOWING` lists. Called once from
 * the app-level NavHost; the signature must stay stable.
 *
 * [Routes.PROFILE_ME] is registered before [Routes.PROFILE] so the literal
 * "me" wins over the `{userId}` pattern.
 */
fun NavGraphBuilder.profileGraph(navController: NavController) {
    composable(Routes.PROFILE_ME) {
        ProfileRoute(
            showBackButton = false,
            onBackClick = {},
            onTweetClick = { tweetId -> navController.navigate(Routes.tweetDetail(tweetId)) },
            onProfileClick = { userId -> navController.navigate(Routes.profile(userId)) },
            onFollowersClick = { userId -> navController.navigate(Routes.followers(userId)) },
            onFollowingClick = { userId -> navController.navigate(Routes.following(userId)) },
            onLoginClick = { navController.navigate(Routes.AUTH_LOGIN) },
            onLoggedOut = { navController.navigateToLogin() },
        )
    }
    composable(
        route = Routes.PROFILE,
        arguments = listOf(navArgument(Routes.ARG_USER_ID) { type = NavType.StringType }),
    ) {
        ProfileRoute(
            showBackButton = true,
            onBackClick = { navController.popBackStack() },
            onTweetClick = { tweetId -> navController.navigate(Routes.tweetDetail(tweetId)) },
            onProfileClick = { userId -> navController.navigate(Routes.profile(userId)) },
            onFollowersClick = { userId -> navController.navigate(Routes.followers(userId)) },
            onFollowingClick = { userId -> navController.navigate(Routes.following(userId)) },
            onLoginClick = { navController.navigate(Routes.AUTH_LOGIN) },
            onLoggedOut = { navController.navigateToLogin() },
        )
    }
    composable(
        route = Routes.FOLLOWERS,
        arguments = listOf(navArgument(Routes.ARG_USER_ID) { type = NavType.StringType }),
    ) {
        FollowListRoute(
            listType = FollowListType.Followers,
            onBackClick = { navController.popBackStack() },
            onUserClick = { userId -> navController.navigate(Routes.profile(userId)) },
        )
    }
    composable(
        route = Routes.FOLLOWING,
        arguments = listOf(navArgument(Routes.ARG_USER_ID) { type = NavType.StringType }),
    ) {
        FollowListRoute(
            listType = FollowListType.Following,
            onBackClick = { navController.popBackStack() },
            onUserClick = { userId -> navController.navigate(Routes.profile(userId)) },
        )
    }
}

/** After logout, land on the login screen with the whole stack cleared. */
private fun NavController.navigateToLogin() {
    navigate(Routes.AUTH_LOGIN) {
        popUpTo(0) { inclusive = true }
    }
}
