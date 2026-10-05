package com.twitterclone.feature.composetweet.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.twitterclone.core.model.Routes
import com.twitterclone.feature.composetweet.ComposeTweetScreen

/** Compose-tweet feature's destinations. */
fun NavGraphBuilder.composeTweetGraph(navController: NavController) {
    composable(Routes.COMPOSE_TWEET) {
        ComposeTweetScreen()
    }
}
