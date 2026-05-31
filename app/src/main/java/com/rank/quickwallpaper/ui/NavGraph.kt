package com.rank.quickwallpaper.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rank.quickwallpaper.R
import com.rank.quickwallpaper.ui.categories.CategoriesScreen
import com.rank.quickwallpaper.ui.collections.CollectionDetailScreen
import com.rank.quickwallpaper.ui.collections.CollectionsScreen
import com.rank.quickwallpaper.ui.detail.DetailScreen
import com.rank.quickwallpaper.ui.favorites.FavoritesScreen
import com.rank.quickwallpaper.ui.home.HomeScreen
import com.rank.quickwallpaper.ui.search.SearchScreen
import com.rank.quickwallpaper.ui.settings.SettingsScreen

private const val ANIM_DURATION = 300
private const val EXPANDED_WIDTH_DP = 840

sealed class Screen(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val labelRes: Int
) {
    object Home : Screen("home", Icons.Filled.Home, Icons.Outlined.Home, R.string.nav_home)
    object Search : Screen("search", Icons.Filled.Search, Icons.Outlined.Search, R.string.search_hint)
    object Categories : Screen(
        "categories", Icons.Filled.Category, Icons.Outlined.Category, R.string.nav_categories
    )
    object Favorites : Screen(
        "favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, R.string.nav_favorites
    )
    object Settings : Screen(
        "settings", Icons.Filled.Settings, Icons.Outlined.Settings, R.string.nav_settings
    )
    object Collections : Screen(
        "collections", Icons.Filled.Home, Icons.Outlined.Home, R.string.collections
    )
    object Detail : Screen(
        "detail/{photoId}", Icons.Filled.Home, Icons.Outlined.Home, R.string.nav_home
    ) {
        fun createRoute(photoId: String) = "detail/$photoId"
    }
    object CollectionDetail : Screen(
        "collection/{collectionId}/{collectionTitle}",
        Icons.Filled.Home, Icons.Outlined.Home, R.string.nav_home
    ) {
        fun createRoute(id: String, title: String) = "collection/$id/${title.encodeUrl()}"
    }
}

private fun String.encodeUrl() = java.net.URLEncoder.encode(this, "UTF-8")

private val bottomNavItems = listOf(
    Screen.Home, Screen.Search, Screen.Categories, Screen.Favorites, Screen.Settings
)

private val bottomRoutes = bottomNavItems.map { it.route }.toSet()

private val slideSpec: FiniteAnimationSpec<IntOffset> = tween(ANIM_DURATION)
private val fadeSpec: FiniteAnimationSpec<Float> = tween(ANIM_DURATION)

private fun AnimatedContentTransitionScope<*>.enterFromRight(): EnterTransition =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, slideSpec) + fadeIn(fadeSpec)

private fun AnimatedContentTransitionScope<*>.exitToLeft(): ExitTransition =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, slideSpec) + fadeOut(fadeSpec)

private fun AnimatedContentTransitionScope<*>.enterFromLeft(): EnterTransition =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, slideSpec) + fadeIn(fadeSpec)

private fun AnimatedContentTransitionScope<*>.exitToRight(): ExitTransition =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, slideSpec) + fadeOut(fadeSpec)

@Composable
fun PaziaNavGraph(startDestination: String = Screen.Home.route) {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStack?.destination
    val showBottomBar = currentDestination?.route in bottomRoutes

    val config = LocalConfiguration.current
    val isExpandedScreen = config.screenWidthDp >= EXPANDED_WIDTH_DP

    val onNavigate: (Screen) -> Unit = { screen ->
        if (navController.currentDestination?.route != screen.route) {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    if (isExpandedScreen) {
        Row(modifier = Modifier.fillMaxSize()) {
            if (showBottomBar) {
                PaziaNavigationRail(
                    items = bottomNavItems,
                    currentDestination = currentDestination?.route,
                    onItemClick = onNavigate
                )
            }
            PaziaNavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    PaziaBottomBar(
                        items = bottomNavItems,
                        currentDestination = currentDestination?.route,
                        onItemClick = onNavigate
                    )
                }
            }
        ) { paddingValues ->
            PaziaNavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            )
        }
    }
}

@Composable
private fun PaziaNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { enterFromRight() },
        exitTransition = { exitToLeft() },
        popEnterTransition = { enterFromLeft() },
        popExitTransition = { exitToRight() }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onPhotoClick = { navController.navigate(Screen.Detail.createRoute(it)) },
                onSearchClick = { navController.navigate(Screen.Search.route) }
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(onPhotoClick = { navController.navigate(Screen.Detail.createRoute(it)) })
        }
        composable(Screen.Categories.route) {
            CategoriesScreen(
                onPhotoClick = { navController.navigate(Screen.Detail.createRoute(it)) },
                onCollectionsClick = { navController.navigate(Screen.Collections.route) }
            )
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(onPhotoClick = { navController.navigate(Screen.Detail.createRoute(it)) })
        }
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("photoId") { type = NavType.StringType })
        ) {
            BackHandler { navController.popBackStack() }
            DetailScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Collections.route) {
            CollectionsScreen(
                onCollectionClick = { id, title ->
                    navController.navigate(Screen.CollectionDetail.createRoute(id, title))
                }
            )
        }
        composable(
            route = Screen.CollectionDetail.route,
            arguments = listOf(
                navArgument("collectionId") { type = NavType.StringType },
                navArgument("collectionTitle") { type = NavType.StringType }
            )
        ) {
            BackHandler { navController.popBackStack() }
            CollectionDetailScreen(
                onBack = { navController.popBackStack() },
                onPhotoClick = { navController.navigate(Screen.Detail.createRoute(it)) }
            )
        }
    }
}

@Composable
private fun PaziaBottomBar(
    items: List<Screen>,
    currentDestination: String?,
    onItemClick: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = NavigationBarDefaults.Elevation
    ) {
        items.forEach { screen ->
            val selected = currentDestination == screen.route
            val iconTint by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "nav_icon_tint"
            )
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(screen) },
                icon = {
                    Icon(
                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = null,
                        tint = iconTint
                    )
                },
                label = {
                    Text(
                        text = stringResource(screen.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                alwaysShowLabel = true
            )
        }
    }
}

@Composable
private fun PaziaNavigationRail(
    items: List<Screen>,
    currentDestination: String?,
    onItemClick: (Screen) -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            Text(
                text = "Pazia",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    ) {
        items.forEach { screen ->
            val selected = currentDestination == screen.route
            val iconTint by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "rail_icon_tint"
            )
            NavigationRailItem(
                selected = selected,
                onClick = { onItemClick(screen) },
                icon = {
                    Icon(
                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = null,
                        tint = iconTint
                    )
                },
                label = {
                    Text(
                        text = stringResource(screen.labelRes),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    }
}
