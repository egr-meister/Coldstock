package com.coldstock.app

import com.coldstock.app.model.DrawerType
import com.coldstock.app.model.FreezerDrawer
import com.coldstock.app.model.FrozenProduct
import com.coldstock.app.model.ProductLifecycleState
import com.coldstock.app.model.StorageDurationUnit
import com.coldstock.app.util.Clock
import com.coldstock.app.util.InventoryUtils
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class InventoryUtilsTest {

    @Before fun setup() { Clock.override = { LocalDateTime.of(2026, 7, 10, 12, 0) } }
    @After fun teardown() { Clock.override = null }

    private val drawers = listOf(
        FreezerDrawer(id = "d1", freezerId = "f1", name = "Top", drawerType = DrawerType.Drawer, sortOrder = 0),
        FreezerDrawer(id = "d2", freezerId = "f1", name = "Bottom", drawerType = DrawerType.Drawer, sortOrder = 1),
    )

    private fun p(id: String, drawer: String, freezer: String = "f1",
                  state: ProductLifecycleState = ProductLifecycleState.Active) =
        FrozenProduct(id = id, freezerId = freezer, drawerId = drawer, name = id,
            portionCount = 2, lifecycleState = state)

    @Test fun groupsByDrawer() {
        val products = listOf(p("a", "d1"), p("b", "d1"), p("c", "d2"))
        val grouped = InventoryUtils.groupByDrawer(products, drawers, "f1")
        assertEquals(2, grouped["d1"]?.size)
        assertEquals(1, grouped["d2"]?.size)
    }

    @Test fun missingDrawerBecomesUnassigned() {
        val products = listOf(p("a", "ghost"))
        val grouped = InventoryUtils.groupByDrawer(products, drawers, "f1")
        assertEquals(1, grouped[InventoryUtils.UNASSIGNED_ID]?.size)
    }

    @Test fun filtersByFreezer() {
        val products = listOf(p("a", "d1", "f1"), p("b", "d1", "f2"))
        assertEquals(1, InventoryUtils.activeProductsFor(products, "f1").size)
    }

    @Test fun excludesNonActive() {
        val products = listOf(p("a", "d1"), p("b", "d1", state = ProductLifecycleState.Used))
        assertEquals(1, InventoryUtils.activeProductsFor(products, "f1").size)
    }

    @Test fun totalPortionsSums() {
        assertEquals(4, InventoryUtils.totalPortions(listOf(p("a", "d1"), p("b", "d2"))))
    }

    @Test fun sortsByReviewDateNearest() {
        val near = FrozenProduct(id = "near", freezingDate = "2026-07-01",
            storageDurationValue = 12, storageDurationUnit = StorageDurationUnit.Days) // 07-13
        val far = FrozenProduct(id = "far", freezingDate = "2026-07-01",
            storageDurationValue = 3, storageDurationUnit = StorageDurationUnit.Months) // 10-01
        val sorted = InventoryUtils.sort(listOf(far, near), InventoryUtils.ProductSort.ReviewDateNearest)
        assertEquals("near", sorted.first().id)
    }
}
