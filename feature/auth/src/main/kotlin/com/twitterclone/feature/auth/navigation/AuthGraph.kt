package com.twitterclone.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.twitterclone.core.model.Routes
import com.twitterclone.feature.auth.LoginScreen
import com.twitterclone.feature.auth.RegisterScreen
import com.twitterclone.feature.auth.SplashScreen

/**
 * Auth feature's destinations. The auth gate ([Routes.AUTH_SPLASH]) is the
 * app's startDestination; it resolves the persisted session and routes to the
 * feed or the login screen, removing itself from the back stack either way.
 *
 * The login destination intentionally stays under the feed: backing out of
 * the feed lands on the login screen's signed-in card, which is the interim
 * logout affordance until profile owns settings. The splash's logged-in
 * branch therefore restores the same [login, feed] stack a manual sign-in
 * produces — a brief "signed in" card may flash during the transition.
 */
fun NavGraphBuilder.authGraph(navController: NavController) {
    composable(Routes.AUTH_SPLASH) {
        SplashScreen(
            onLoggedIn = {
                navController.navigate(Routes.AUTH_LOGIN) {
                    popUpTo(Routes.AUTH_SPLASH) { inclusive = true }
                }
                navController.navigate(Routes.FEED)
            },
            onLoggedOut = {
                navController.navigate(Routes.AUTH_LOGIN) {
                    popUpTo(Routes.AUTH_SPLASH) { inclusive = true }
                }
            },
        )
    }
    composable(Routes.AUTH_LOGIN) {
        LoginScreen(
            onFeed = {
                navController.navigate(Routes.FEED) {
                    // Replace any existing feed instead of stacking a second one.
                    popUpTo(Routes.FEED) { inclusive = true }
                }
            },
            onRegisterClick = { navController.navigate(Routes.AUTH_REGISTER) },
        )
    }
    composable(Routes.AUTH_REGISTER) {
        RegisterScreen(
            onRegistered = {
                navController.navigate(Routes.FEED) {
                    popUpTo(Routes.AUTH_REGISTER) { inclusive = true }
                }
            },
            onLoginClick = { navController.popBackStack() },
        )
    }
}
