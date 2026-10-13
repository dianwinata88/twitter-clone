package com.twitterclone.feature.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.twitterclone.core.model.Routes
import com.twitterclone.core.ui.component.PlaceholderScreen

/**
 * Search feature's destinations. V1 stub: the search agent replaces this with
 * the search bar + Posts/People tabs and the trending section.
 */
fun NavGraphBuilder.searchGraph(navController: NavController) {
    composable(Routes.SEARCH) {
        PlaceholderScreen(name = "Search", hint = "Search bar + Posts/People tabs + trending.")
    }
}
