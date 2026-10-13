package com.twitterclone.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.twitterclone.core.model.Routes
import com.twitterclone.feature.auth.LoginScreen
import com.twitterclone.feature.auth.RegisterScreen
import com.twitterclone.feature.auth.SplashScreen

/**
 * Auth feature's destinations: splash gate → login → register. Splash is the
 * NavHost start destination (see AppNavHost); successful auth navigates to the
 * feed with the whole auth back stack cleared so Back exits the app like X.
 */
fun NavGraphBuilder.authGraph(navController: NavController) {
    composable(Routes.SPLASH) {
        SplashScreen(
            onLoggedIn = {
                navController.navigate(Routes.FEED) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            },
            onLoggedOut = {
                navController.navigate(Routes.AUTH_LOGIN) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            },
        )
    }
    composable(Routes.AUTH_LOGIN) {
        LoginScreen(
            onNavigateToRegister = { navController.navigate(Routes.AUTH_REGISTER) },
            onLoggedIn = {
                navController.navigate(Routes.FEED) {
                    popUpTo(Routes.AUTH_LOGIN) { inclusive = true }
                }
            },
        )
    }
    composable(Routes.AUTH_REGISTER) {
        RegisterScreen(
            onNavigateToLogin = { navController.popBackStack() },
            onRegistered = {
                navController.navigate(Routes.FEED) {
                    popUpTo(Routes.AUTH_LOGIN) { inclusive = true }
                }
            },
        )
    }
}
