package com.twitterclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.twitterclone.core.model.Routes
import com.twitterclone.core.ui.theme.TwitterCloneTheme
import com.twitterclone.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TwitterCloneTheme {
                AppRoot()
            }
        }
    }
}

private data class TopLevelTab(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
)

private val topLevelTabs =
    listOf(
        TopLevelTab(Routes.FEED, R.string.tab_home, Icons.Outlined.Home),
        TopLevelTab(Routes.SEARCH, R.string.tab_search, Icons.Outlined.Search),
        TopLevelTab(Routes.ALERTS, R.string.tab_alerts, Icons.Outlined.Notifications),
        TopLevelTab(Routes.MESSAGES, R.string.tab_messages, Icons.Outlined.MailOutline),
        TopLevelTab(Routes.PROFILE_ME, R.string.tab_profile, Icons.Outlined.Person),
    )

/**
 * X-style app shell: top bar with wordmark, icon-only bottom nav with the 5
 * tabs (Alerts/Messages route to V1 placeholders), FAB to compose. Chrome is
 * only shown on top-level tab destinations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val onTopLevelTab = topLevelTabs.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (onTopLevelTab) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_wordmark),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                )
            }
        },
        bottomBar = {
            if (onTopLevelTab) {
                BottomNav(navController = navController, currentRoute = currentRoute)
            }
        },
        floatingActionButton = {
            if (currentRoute == Routes.FEED) {
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.COMPOSE) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.compose_tweet),
                    )
                }
            }
        },
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun BottomNav(
    navController: NavHostController,
    currentRoute: String?,
) {
    NavigationBar {
        topLevelTabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = stringResource(tab.labelRes),
                    )
                },
            )
        }
    }
}
