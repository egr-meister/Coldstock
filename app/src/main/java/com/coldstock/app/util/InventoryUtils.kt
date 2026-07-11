package com.coldstock.app.util

import com.coldstock.app.model.FreezerDrawer
import com.coldstock.app.model.FrozenFoodCategory
import com.coldstock.app.model.FrozenProduct
import com.coldstock.app.model.FrozenProductStatus
import com.coldstock.app.model.ProductLifecycleState
import java.time.LocalDate

/** Grouping, occupancy, filtering and sorting helpers for the inventory. */
object InventoryUtils {

    const val UNASSIGNED_ID = ""
    const val UNASSIGNED_LABEL = "Unassigned Drawer"

    fun activeProductsFor(products: List<FrozenProduct>, freezerId: String?): List<FrozenProduct> =
        products.filter {
            it.freezerId == freezerId && it.lifecycleState == ProductLifecycleState.Active
        }

    fun productsInDrawer(products: List<FrozenProduct>, drawerId: String): List<FrozenProduct> =
        products.filter { it.drawerId == drawerId && it.lifecycleState == ProductLifecycleState.Active }

    /** Groups active products of a freezer by drawer id (missing ref -> unassigned). */
    fun groupByDrawer(
        products: List<FrozenProduct>,
        drawers: List<FreezerDrawer>,
        freezerId: String?
    ): Map<String, List<FrozenProduct>> {
        val validDrawerIds = drawers.filter { it.freezerId == freezerId }.map { it.id }.toSet()
        val active = activeProductsFor(products, freezerId)
        return active.groupBy { p ->
            if (p.drawerId.isNotBlank() && p.drawerId in validDrawerIds) p.drawerId else UNASSIGNED_ID
        }
    }

    fun totalPortions(products: List<FrozenProduct>): Int =
        products.sumOf { it.portionCount.coerceAtLeast(0) }

    fun countByStatus(
        products: List<FrozenProduct>,
        soonThreshold: Int,
        status: FrozenProductStatus,
        today: LocalDate = Clock.today()
    ): Int = products.count { StatusUtils.statusOf(it, soonThreshold, today) == status }

    // -- Sorting -----------------------------------------------------------

    enum class ProductSort {
        ReviewDateNearest, ReviewDateOldest, FreezingNewest, FreezingOldest, Name, PortionCount, Drawer
    }

    fun sort(
        products: List<FrozenProduct>,
        sort: ProductSort,
        drawerNameOf: (String) -> String = { it }
    ): List<FrozenProduct> {
        val farFuture = LocalDate.MAX
        val farPast = LocalDate.MIN
        fun review(p: FrozenProduct): LocalDate? = DateUtils.plannedReviewDate(
            p.freezingDate, p.storageDurationValue, p.storageDurationUnit
        )
        fun freezing(p: FrozenProduct): LocalDate? = DateUtils.parseDate(p.freezingDate)
        return when (sort) {
            ProductSort.ReviewDateNearest ->
                products.sortedBy { review(it) ?: farFuture }
            ProductSort.ReviewDateOldest ->
                products.sortedByDescending { review(it) ?: farPast }
            ProductSort.FreezingNewest ->
                products.sortedByDescending { freezing(it) ?: farPast }
            ProductSort.FreezingOldest ->
                products.sortedBy { freezing(it) ?: farFuture }
            ProductSort.Name ->
                products.sortedBy { it.name.lowercase() }
            ProductSort.PortionCount ->
                products.sortedByDescending { it.portionCount }
            ProductSort.Drawer ->
                products.sortedBy { drawerNameOf(it.drawerId).lowercase() }
        }
    }

    // -- Statistics --------------------------------------------------------

    fun mostOccupiedDrawer(
        products: List<FrozenProduct>,
        drawers: List<FreezerDrawer>,
        freezerId: String?
    ): FreezerDrawer? {
        val grouped = groupByDrawer(products, drawers, freezerId)
        val best = grouped.filterKeys { it.isNotBlank() }.maxByOrNull { it.value.size } ?: return null
        return drawers.firstOrNull { it.id == best.key }
    }

    fun mostUsedCategory(products: List<FrozenProduct>): FrozenFoodCategory? =
        products.groupBy { it.category }
            .maxByOrNull { it.value.size }?.key
}
