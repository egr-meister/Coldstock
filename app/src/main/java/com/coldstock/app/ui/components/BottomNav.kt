package com.coldstock.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.coldstock.app.ui.navigation.Routes

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

private val items = listOf(
    NavItem(Routes.HOME, "Freezer", Icons.Filled.Kitchen),
    NavItem(Routes.SEARCH, "Search", Icons.Filled.Search),
    NavItem(Routes.USE_FIRST, "Use First", Icons.Filled.Star),
    NavItem(Routes.HISTORY, "History", Icons.Filled.History),
    NavItem(Routes.SETTINGS, "Settings", Icons.Filled.Settings),
)

@Composable
fun ColdstockBottomBar(nav: NavHostController) {
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = current == item.route,
                onClick = {
                    if (current != item.route) {
                        nav.navigate(item.route) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
