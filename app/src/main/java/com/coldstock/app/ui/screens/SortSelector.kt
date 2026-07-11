package com.coldstock.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.coldstock.app.ui.components.DropdownField
import com.coldstock.app.util.InventoryUtils

fun sortLabel(sort: InventoryUtils.ProductSort): String = when (sort) {
    InventoryUtils.ProductSort.ReviewDateNearest -> "Review date (nearest)"
    InventoryUtils.ProductSort.ReviewDateOldest -> "Review date (oldest)"
    InventoryUtils.ProductSort.FreezingNewest -> "Freezing date (newest)"
    InventoryUtils.ProductSort.FreezingOldest -> "Freezing date (oldest)"
    InventoryUtils.ProductSort.Name -> "Product name"
    InventoryUtils.ProductSort.PortionCount -> "Portion count"
    InventoryUtils.ProductSort.Drawer -> "Drawer"
}

@Composable
fun SortSelector(
    selected: InventoryUtils.ProductSort,
    modifier: Modifier = Modifier,
    onSelected: (InventoryUtils.ProductSort) -> Unit
) {
    DropdownField(
        label = "Sort by",
        selected = selected,
        options = InventoryUtils.ProductSort.entries,
        optionLabel = { sortLabel(it) },
        onSelected = onSelected,
        modifier = modifier
    )
}
