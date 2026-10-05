package com.twitterclone.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.twitterclone.core.model.Routes
import com.twitterclone.feature.auth.AuthPlaceholderScreen

/**
 * Auth feature's destinations. The auth agent owns the splash/auth gate and
 * may retarget the NavHost startDestination to [Routes.AUTH_SPLASH]; see
 * docs/ARCHITECTURE.md.
 */
fun NavGraphBuilder.authGraph(navController: NavController) {
    composable(Routes.AUTH_SPLASH) {
        AuthPlaceholderScreen(
            title = "Splash",
            hint = "Auth gate stub — auth feature decides logged-in vs. logged-out here.",
        )
    }
    composable(Routes.AUTH_LOGIN) {
        AuthPlaceholderScreen(
            title = "Log in",
            hint = "Login stub — demo / demo works against the fake backend.",
        )
    }
    composable(Routes.AUTH_REGISTER) {
        AuthPlaceholderScreen(
            title = "Create account",
            hint = "Register stub — AuthRepository.register persists to Room.",
        )
    }
}
