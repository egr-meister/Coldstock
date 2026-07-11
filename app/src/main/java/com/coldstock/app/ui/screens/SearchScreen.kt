package com.coldstock.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
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
import com.coldstock.app.model.FrozenFoodCategory
import com.coldstock.app.model.FrozenProductStatus
import com.coldstock.app.ui.ColdstockViewModel
import com.coldstock.app.ui.components.DropdownField
import com.coldstock.app.ui.components.EmptyState
import com.coldstock.app.ui.components.LabeledTextField
import com.coldstock.app.ui.components.ScreenScaffold
import com.coldstock.app.ui.components.SectionHeader
import com.coldstock.app.ui.navigation.Routes
import com.coldstock.app.util.InventoryUtils
import com.coldstock.app.util.SearchUtils
import com.coldstock.app.util.StatusUtils

private data class CatOpt(val cat: FrozenFoodCategory?, val label: String)
private data class FreezerOpt2(val id: String?, val label: String)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(vm: ColdstockViewModel, nav: NavHostController, appData: AppData) {
    val soon = appData.settings.soonThresholdDays
    var query by remember { mutableStateOf("") }
    var freezerId by remember { mutableStateOf<String?>(null) }
    var category by remember { mutableStateOf<FrozenFoodCategory?>(null) }
    var useFirstOnly by remember { mutableStateOf(false) }
    var statuses by remember { mutableStateOf<Set<FrozenProductStatus>>(emptySet()) }
    var sort by remember { mutableStateOf(InventoryUtils.ProductSort.ReviewDateNearest) }

    val drawerName: (String) -> String = { id ->
        appData.drawers.firstOrNull { it.id == id }?.name ?: "Unassigned Drawer"
    }

    val filters = SearchUtils.Filters(
        freezerId = freezerId,
        category = category,
        statuses = statuses,
        useFirstOnly = useFirstOnly
    )
    val results = SearchUtils.search(appData.products, appData.drawers, query, filters, soon)
    val sorted = InventoryUtils.sort(results, sort, drawerName)

    val freezerOpts = buildList {
        add(FreezerOpt2(null, "All freezers"))
        appData.freezers.forEach { add(FreezerOpt2(it.id, it.name.ifBlank { "Freezer" })) }
    }
    val catOpts = buildList {
        add(CatOpt(null, "All categories"))
        FrozenFoodCategory.entries.forEach { add(CatOpt(it, it.label)) }
    }

    val statusChoices = listOf(
        FrozenProductStatus.Ok, FrozenProductStatus.UseSoon, FrozenProductStatus.Old,
        FrozenProductStatus.NoReviewDate, FrozenProductStatus.Used, FrozenProductStatus.Discarded
    )

    ScreenScaffold(title = "Search", nav = nav, showBottomBar = true) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                LabeledTextField(
                    label = "Search products, categories, drawers, notes",
                    value = query,
                    onValueChange = { query = it }
                )
            }
            item {
                DropdownField(
                    label = "Freezer",
                    selected = freezerOpts.firstOrNull { it.id == freezerId } ?: freezerOpts.first(),
                    options = freezerOpts,
                    optionLabel = { it.label },
                    onSelected = { freezerId = it.id }
                )
            }
            item {
                DropdownField(
                    label = "Category",
                    selected = catOpts.firstOrNull { it.cat == category } ?: catOpts.first(),
                    options = catOpts,
                    optionLabel = { it.label },
                    onSelected = { category = it.cat }
                )
            }
            item { SectionHeader("Status filters") }
            item {
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = useFirstOnly,
                        onClick = { useFirstOnly = !useFirstOnly },
                        label = { Text("Use First") }
                    )
                    statusChoices.forEach { s ->
                        FilterChip(
                            selected = s in statuses,
                            onClick = {
                                statuses = if (s in statuses) statuses - s else statuses + s
                            },
                            label = { Text(s.label) }
                        )
                    }
                }
            }
            item { SortSelector(sort) { sort = it } }

            if (sorted.isEmpty()) {
                item { EmptyState("No matching frozen products.", "Try different search terms or filters.") }
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
