package com.twitterclone.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.twitterclone.core.model.Routes
import com.twitterclone.core.ui.component.PlaceholderScreen

/**
 * Auth feature's destinations. V1 stub: the auth feature agent replaces these
 * placeholders with the real splash → login → register flow and may retarget
 * the NavHost startDestination to [Routes.SPLASH]; see docs/ARCHITECTURE.md.
 */
fun NavGraphBuilder.authGraph(navController: NavController) {
    composable(Routes.SPLASH) {
        PlaceholderScreen(name = "Splash", hint = "Auth gate — decides logged-in vs. logged-out.")
    }
    composable(Routes.AUTH_LOGIN) {
        PlaceholderScreen(name = "Log in", hint = "demo / demo works against the fake backend.")
    }
    composable(Routes.AUTH_REGISTER) {
        PlaceholderScreen(name = "Create account", hint = "AuthRepository.register persists to Room.")
    }
}
