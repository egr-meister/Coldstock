package com.coldstock.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.coldstock.app.data.AppData
import com.coldstock.app.ui.ColdstockViewModel
import com.coldstock.app.ui.components.CountPill
import com.coldstock.app.ui.components.EmptyState
import com.coldstock.app.ui.components.InfoBanner
import com.coldstock.app.ui.components.LabeledTextField
import com.coldstock.app.ui.components.ScreenScaffold
import com.coldstock.app.ui.navigation.Routes
import com.coldstock.app.ui.theme.OkTeal
import com.coldstock.app.ui.theme.ReviewPassedRed
import com.coldstock.app.ui.theme.UseSoonAmber
import com.coldstock.app.model.FrozenProductStatus
import com.coldstock.app.util.InventoryUtils
import com.coldstock.app.util.StatusUtils

@Composable
fun DrawerDetailScreen(
    vm: ColdstockViewModel,
    nav: NavHostController,
    drawerId: String?,
    appData: AppData
) {
    val drawer = appData.drawers.firstOrNull { it.id == drawerId }
    if (drawer == null) {
        NotFoundScreen("Drawer", "Drawer not found", nav)
        return
    }
    val soon = appData.settings.soonThresholdDays
    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(InventoryUtils.ProductSort.ReviewDateNearest) }

    val all = InventoryUtils.productsInDrawer(appData.products, drawer.id)
    val filtered = all.filter {
        query.isBlank() ||
            it.name.contains(query, true) ||
            it.categoryDisplay.contains(query, true) ||
            it.note.contains(query, true)
    }
    val sorted = InventoryUtils.sort(filtered, sort) { drawer.name }

    val totalPortions = InventoryUtils.totalPortions(all)
    val useSoon = all.count { StatusUtils.statusOf(it, soon) == FrozenProductStatus.UseSoon }
    val passed = all.count { StatusUtils.statusOf(it, soon) == FrozenProductStatus.Old }

    ScreenScaffold(
        title = drawer.name,
        nav = nav,
        showBack = true,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { nav.navigate(Routes.addProduct(drawer.id)) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add Product") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CountPill("products", all.size, MaterialTheme.colorScheme.primary)
                    CountPill("portions", totalPortions, OkTeal)
                    CountPill("Use Soon", useSoon, UseSoonAmber)
                    CountPill("Passed", passed, ReviewPassedRed)
                }
            }
            if (drawer.note.isNotBlank()) {
                item { InfoBanner("Drawer note: ${drawer.note}") }
            }
            item {
                LabeledTextField(
                    label = "Search in drawer",
                    value = query,
                    onValueChange = { query = it }
                )
            }
            item { SortSelector(sort) { sort = it } }

            if (sorted.isEmpty()) {
                item {
                    EmptyState(
                        title = "Empty drawer",
                        message = "Add a product to place it in this drawer."
                    )
                }
            }

            items(sorted, key = { it.id }) { p ->
                ProductRow(
                    product = p,
                    status = StatusUtils.statusOf(p, soon),
                    drawerName = null,
                    onClick = { nav.navigate(Routes.productDetail(p.id)) }
                )
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}
