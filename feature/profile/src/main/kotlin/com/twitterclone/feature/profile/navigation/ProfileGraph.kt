package com.twitterclone.feature.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.twitterclone.core.model.Routes
import com.twitterclone.core.ui.component.PlaceholderScreen

/**
 * Profile feature's destinations. V1 stub: the profile agent replaces these
 * placeholders with profile, edit-profile dialog, followers/following lists
 * and the follow toggle.
 *
 * [Routes.PROFILE_ME] is registered before [Routes.PROFILE] so the literal
 * "me" wins over the `{userId}` pattern.
 */
fun NavGraphBuilder.profileGraph(navController: NavController) {
    composable(Routes.PROFILE_ME) {
        PlaceholderScreen(name = "Profile", hint = "The signed-in user's profile.")
    }
    composable(
        route = Routes.PROFILE,
        arguments = listOf(navArgument(Routes.ARG_USER_ID) { type = NavType.StringType }),
    ) { entry ->
        PlaceholderScreen(
            name = "Profile",
            hint = "userId = ${entry.arguments?.getString(Routes.ARG_USER_ID).orEmpty()}",
        )
    }
    composable(
        route = Routes.FOLLOWERS,
        arguments = listOf(navArgument(Routes.ARG_USER_ID) { type = NavType.StringType }),
    ) { entry ->
        PlaceholderScreen(
            name = "Followers",
            hint = "userId = ${entry.arguments?.getString(Routes.ARG_USER_ID).orEmpty()}",
        )
    }
    composable(
        route = Routes.FOLLOWING,
        arguments = listOf(navArgument(Routes.ARG_USER_ID) { type = NavType.StringType }),
    ) { entry ->
        PlaceholderScreen(
            name = "Following",
            hint = "userId = ${entry.arguments?.getString(Routes.ARG_USER_ID).orEmpty()}",
        )
    }
}
