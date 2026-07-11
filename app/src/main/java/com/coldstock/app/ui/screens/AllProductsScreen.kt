package com.coldstock.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.coldstock.app.data.AppData
import com.coldstock.app.model.FrozenProductStatus
import com.coldstock.app.ui.ColdstockViewModel
import com.coldstock.app.ui.components.DropdownField
import com.coldstock.app.ui.components.EmptyState
import com.coldstock.app.ui.components.LabeledTextField
import com.coldstock.app.ui.components.ScreenScaffold
import com.coldstock.app.ui.navigation.Routes
import com.coldstock.app.util.InventoryUtils
import com.coldstock.app.util.StatusUtils

private data class StatusFilterOption(val status: FrozenProductStatus?, val label: String)

@Composable
fun AllProductsScreen(
    vm: ColdstockViewModel,
    nav: NavHostController,
    appData: AppData
) {
    val soon = appData.settings.soonThresholdDays
    val activeId = appData.settings.activeFreezerId
    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(InventoryUtils.ProductSort.Drawer) }
    var statusFilter by remember { mutableStateOf<FrozenProductStatus?>(null) }

    val drawerName: (String) -> String = { id ->
        appData.drawers.firstOrNull { it.id == id }?.name ?: "Unassigned Drawer"
    }

    val base = InventoryUtils.activeProductsFor(appData.products, activeId)
    val filtered = base.filter { p ->
        (query.isBlank() || p.name.contains(query, true) || p.categoryDisplay.contains(query, true) ||
            drawerName(p.drawerId).contains(query, true)) &&
            (statusFilter == null || StatusUtils.statusOf(p, soon) == statusFilter)
    }
    val sorted = InventoryUtils.sort(filtered, sort, drawerName)

    val statusOptions = listOf(
        StatusFilterOption(null, "All statuses"),
        StatusFilterOption(FrozenProductStatus.Ok, "OK"),
        StatusFilterOption(FrozenProductStatus.UseSoon, "Use Soon"),
        StatusFilterOption(FrozenProductStatus.Old, "Review Date Passed"),
        StatusFilterOption(FrozenProductStatus.NoReviewDate, "No Review Date"),
    )

    ScreenScaffold(title = "All Products", nav = nav, showBack = true) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                LabeledTextField(label = "Search", value = query, onValueChange = { query = it })
            }
            item {
                DropdownField(
                    label = "Status",
                    selected = statusOptions.firstOrNull { it.status == statusFilter } ?: statusOptions.first(),
                    options = statusOptions,
                    optionLabel = { it.label },
                    onSelected = { statusFilter = it.status }
                )
            }
            item { SortSelector(sort) { sort = it } }

            if (sorted.isEmpty()) {
                item { EmptyState("No matching frozen products.", "Adjust filters or add a product.") }
            }

            items(sorted, key = { it.id }) { p ->
                ProductRow(
                    product = p,
                    status = StatusUtils.statusOf(p, soon),
                    drawerName = drawerName(p.drawerId),
                    onClick = { nav.navigate(Routes.productDetail(p.id)) }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
