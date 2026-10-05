package com.twitterclone.feature.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.twitterclone.core.model.Routes
import com.twitterclone.feature.profile.FollowListPlaceholderScreen
import com.twitterclone.feature.profile.ProfilePlaceholderScreen

/** Profile feature's destinations: profile, self-profile, follow lists. */
fun NavGraphBuilder.profileGraph(navController: NavController) {
    composable(
        route = Routes.PROFILE,
        arguments = listOf(navArgument(Routes.ARG_USER_ID) { type = NavType.StringType }),
    ) { entry ->
        ProfilePlaceholderScreen(
            userId = entry.arguments?.getString(Routes.ARG_USER_ID).orEmpty(),
        )
    }
    composable(Routes.PROFILE_ME) {
        ProfilePlaceholderScreen(userId = "me")
    }
    composable(
        route = Routes.FOLLOW_LIST,
        arguments =
            listOf(
                navArgument(Routes.ARG_USER_ID) { type = NavType.StringType },
                navArgument(Routes.ARG_TAB) { type = NavType.StringType },
            ),
    ) { entry ->
        FollowListPlaceholderScreen(
            userId = entry.arguments?.getString(Routes.ARG_USER_ID).orEmpty(),
            tab = entry.arguments?.getString(Routes.ARG_TAB).orEmpty(),
        )
    }
}
