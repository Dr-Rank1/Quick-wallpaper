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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
private val EXPANDED_WIDTH_DP = 840.dp

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
    Screen.Home, Screen.Categories, Screen.Favorites, Screen.Settings
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

    val isExpandedScreen = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.width.toDp() >= EXPANDED_WIDTH_DP
    }

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
        Box(modifier = Modifier.fillMaxSize()) {
            PaziaNavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize()
            )
            if (showBottomBar) {
                PaziaBottomBar(
                    items = bottomNavItems,
                    currentDestination = currentDestination?.route,
                    onItemClick = onNavigate,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
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
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onRandomPhoto = { navController.navigate(Screen.Detail.createRoute(it)) }
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
    onItemClick: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.extraLarge)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val selected = currentDestination == screen.route
                val iconTint by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "nav_tint"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { onItemClick(screen) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = stringResource(screen.labelRes),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = iconTint
                        )
                    }
                }
            }
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
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Pazia",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) {
        items.forEach { screen ->
            val selected = currentDestination == screen.route
            val iconTint by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "rail_icon_tint"
            )
            NavigationRailItem(
                selected = selected,
                onClick = { onItemClick(screen) },
                icon = {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .offset(x = 14.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = stringResource(screen.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = iconTint
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
                )
            )
        }
    }
}
