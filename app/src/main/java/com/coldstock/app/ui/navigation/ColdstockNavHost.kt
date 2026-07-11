package com.coldstock.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.coldstock.app.data.AppData
import com.coldstock.app.ui.ColdstockViewModel
import com.coldstock.app.ui.screens.AddEditProductScreen
import com.coldstock.app.ui.screens.AllProductsScreen
import com.coldstock.app.ui.screens.DrawerDetailScreen
import com.coldstock.app.ui.screens.DrawerManagementScreen
import com.coldstock.app.ui.screens.FreezerManagementScreen
import com.coldstock.app.ui.screens.FreezerSetupScreen
import com.coldstock.app.ui.screens.HistoryDetailScreen
import com.coldstock.app.ui.screens.HistoryScreen
import com.coldstock.app.ui.screens.HomeScreen
import com.coldstock.app.ui.screens.OnboardingScreen
import com.coldstock.app.ui.screens.ProductDetailScreen
import com.coldstock.app.ui.screens.SearchScreen
import com.coldstock.app.ui.screens.SettingsScreen
import com.coldstock.app.ui.screens.StatisticsScreen
import com.coldstock.app.ui.screens.UseFirstScreen

@Composable
fun ColdstockNavHost(viewModel: ColdstockViewModel, appData: AppData) {
    // Wait for DataStore to load before choosing the start destination so the
    // onboarding decision is based on real persisted state (not the empty default).
    if (!appData.loaded) {
        LoadingPlaceholder()
        return
    }

    val nav = rememberNavController()
    val start = if (!appData.settings.onboardingCompleted) Routes.ONBOARDING else Routes.HOME

    NavHost(navController = nav, startDestination = start) {

        composable(Routes.ONBOARDING) {
            OnboardingScreen(vm = viewModel, nav = nav)
        }

        composable(Routes.HOME) {
            HomeScreen(vm = viewModel, nav = nav, appData = appData)
        }

        composable(Routes.FREEZER_SETUP) {
            FreezerSetupScreen(vm = viewModel, nav = nav, freezerId = null)
        }

        composable(
            route = "${Routes.FREEZER_EDIT}/{${Routes.ARG_FREEZER_ID}}",
            arguments = listOf(navArgument(Routes.ARG_FREEZER_ID) { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString(Routes.ARG_FREEZER_ID)
            FreezerSetupScreen(vm = viewModel, nav = nav, freezerId = id)
        }

        composable(
            route = "${Routes.DRAWER_DETAIL}/{${Routes.ARG_DRAWER_ID}}",
            arguments = listOf(navArgument(Routes.ARG_DRAWER_ID) { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString(Routes.ARG_DRAWER_ID)
            DrawerDetailScreen(vm = viewModel, nav = nav, drawerId = id, appData = appData)
        }

        composable(
            route = "${Routes.ADD_PRODUCT}?${Routes.ARG_DRAWER_ID}={${Routes.ARG_DRAWER_ID}}",
            arguments = listOf(navArgument(Routes.ARG_DRAWER_ID) {
                type = NavType.StringType; nullable = true; defaultValue = null
            })
        ) { entry ->
            val drawerId = entry.arguments?.getString(Routes.ARG_DRAWER_ID)
            AddEditProductScreen(
                vm = viewModel, nav = nav, productId = null,
                presetDrawerId = drawerId, appData = appData
            )
        }

        composable(
            route = "${Routes.EDIT_PRODUCT}/{${Routes.ARG_PRODUCT_ID}}",
            arguments = listOf(navArgument(Routes.ARG_PRODUCT_ID) { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString(Routes.ARG_PRODUCT_ID)
            AddEditProductScreen(
                vm = viewModel, nav = nav, productId = id,
                presetDrawerId = null, appData = appData
            )
        }

        composable(
            route = "${Routes.PRODUCT_DETAIL}/{${Routes.ARG_PRODUCT_ID}}",
            arguments = listOf(navArgument(Routes.ARG_PRODUCT_ID) { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString(Routes.ARG_PRODUCT_ID)
            ProductDetailScreen(vm = viewModel, nav = nav, productId = id, appData = appData)
        }

        composable(Routes.ALL_PRODUCTS) {
            AllProductsScreen(vm = viewModel, nav = nav, appData = appData)
        }

        composable(Routes.SEARCH) {
            SearchScreen(vm = viewModel, nav = nav, appData = appData)
        }

        composable(Routes.USE_FIRST) {
            UseFirstScreen(vm = viewModel, nav = nav, appData = appData)
        }

        composable(Routes.HISTORY) {
            HistoryScreen(vm = viewModel, nav = nav, appData = appData)
        }

        composable(
            route = "${Routes.HISTORY_DETAIL}/{${Routes.ARG_EVENT_ID}}",
            arguments = listOf(navArgument(Routes.ARG_EVENT_ID) { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString(Routes.ARG_EVENT_ID)
            HistoryDetailScreen(vm = viewModel, nav = nav, eventId = id, appData = appData)
        }

        composable(Routes.FREEZER_MANAGEMENT) {
            FreezerManagementScreen(vm = viewModel, nav = nav, appData = appData)
        }

        composable(Routes.DRAWER_MANAGEMENT) {
            DrawerManagementScreen(vm = viewModel, nav = nav, appData = appData)
        }

        composable(Routes.STATISTICS) {
            StatisticsScreen(vm = viewModel, nav = nav, appData = appData)
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(vm = viewModel, nav = nav, appData = appData)
        }
    }
}

@Composable
private fun LoadingPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
