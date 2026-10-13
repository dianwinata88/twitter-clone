package com.twitterclone.feature.composetweet.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.twitterclone.core.model.Routes
import com.twitterclone.core.ui.component.PlaceholderScreen

/**
 * Compose-tweet feature's destinations. V1 stub: the composer agent replaces
 * this with the text + up-to-4-images composer with pending/failed sync states.
 */
fun NavGraphBuilder.composeTweetGraph(navController: NavController) {
    composable(Routes.COMPOSE) {
        PlaceholderScreen(name = "Compose tweet", hint = "Text + up to 4 images, sync states.")
    }
}
