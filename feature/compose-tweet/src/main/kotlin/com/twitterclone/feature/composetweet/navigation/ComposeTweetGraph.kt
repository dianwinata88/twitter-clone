package com.twitterclone.feature.composetweet.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.twitterclone.core.model.Routes
import com.twitterclone.feature.composetweet.ComposeTweetScreen

/** Compose-tweet feature's destination: the modal-style composer on [Routes.COMPOSE]. */
fun NavGraphBuilder.composeTweetGraph(navController: NavController) {
    composable(Routes.COMPOSE) {
        ComposeTweetScreen(navController = navController)
    }
}
