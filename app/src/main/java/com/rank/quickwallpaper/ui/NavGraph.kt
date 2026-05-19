package com.rank.quickwallpaper.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rank.quickwallpaper.R
import com.rank.quickwallpaper.ui.categories.CategoriesScreen
import com.rank.quickwallpaper.ui.collections.CollectionDetailScreen
import com.rank.quickwallpaper.ui.collections.CollectionsScreen
import com.rank.quickwallpaper.ui.detail.DetailScreen
import com.rank.quickwallpaper.ui.favorites.FavoritesScreen
import com.rank.quickwallpaper.ui.home.HomeScreen
import com.rank.quickwallpaper.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Categories : Screen("categories")
    object Favorites : Screen("favorites")
    object Settings : Screen("settings")
    object Detail : Screen("detail/{photoId}") {
        fun createRoute(photoId: String) = "detail/$photoId"
    }
    object Collections : Screen("collections")
    object CollectionDetail : Screen("collection/{collectionId}/{collectionTitle}") {
        fun createRoute(id: String, title: String) = "collection/$id/${title.encodeUrl()}"
    }
}

private fun String.encodeUrl() = java.net.URLEncoder.encode(this, "UTF-8")

private val bottomNavItems = listOf(
    Triple(Screen.Home, Icons.Outlined.Home, R.string.nav_home),
    Triple(Screen.Categories, Icons.Outlined.Category, R.string.nav_categories),
    Triple(Screen.Favorites, Icons.Outlined.Favorite, R.string.nav_favorites),
    Triple(Screen.Settings, Icons.Outlined.Settings, R.string.nav_settings)
)

private val bottomRoutes = setOf(
    Screen.Home.route, Screen.Categories.route,
    Screen.Favorites.route, Screen.Settings.route
)

@Composable
fun PaziaNavGraph(
    startDestination: String = Screen.Home.route
) {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStack?.destination
    val showBottomBar = currentDestination?.route in bottomRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { (screen, icon, labelRes) ->
                        NavigationBarItem(
                            icon = { Icon(icon, null) },
                            label = { Text(stringResource(labelRes)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onPhotoClick = { id ->
                        navController.navigate(Screen.Detail.createRoute(id))
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Categories.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Categories.route) {
                CategoriesScreen(
                    onPhotoClick = { id ->
                        navController.navigate(Screen.Detail.createRoute(id))
                    }
                )
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onPhotoClick = { id ->
                        navController.navigate(Screen.Detail.createRoute(id))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            composable(Screen.Detail.route) {
                DetailScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Collections.route) {
                CollectionsScreen(
                    onCollectionClick = { id, title ->
                        navController.navigate(Screen.CollectionDetail.createRoute(id, title))
                    }
                )
            }

            composable(Screen.CollectionDetail.route) {
                CollectionDetailScreen(
                    onBack = { navController.popBackStack() },
                    onPhotoClick = { id ->
                        navController.navigate(Screen.Detail.createRoute(id))
                    }
                )
            }
        }
    }
}
