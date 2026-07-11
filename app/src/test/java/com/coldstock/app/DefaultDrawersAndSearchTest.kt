package com.coldstock.app

import com.coldstock.app.data.DefaultDrawers
import com.coldstock.app.model.FreezerDrawer
import com.coldstock.app.model.FreezerType
import com.coldstock.app.model.FrozenProduct
import com.coldstock.app.util.SearchUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultDrawersAndSearchTest {

    @Test fun uprightHasFourDefaultDrawers() {
        val d = DefaultDrawers.buildFor("f1", FreezerType.Upright)
        assertEquals(4, d.size)
        assertEquals("Top Drawer", d.first().name)
        assertEquals(listOf(0, 1, 2, 3), d.map { it.sortOrder })
    }

    @Test fun chestHasSectionDefaults() {
        val d = DefaultDrawers.buildFor("f1", FreezerType.Chest)
        assertEquals(4, d.size)
        assertTrue(d.any { it.name == "Left Basket" })
        assertTrue(d.any { it.name == "Main Compartment" })
    }

    @Test fun defaultDrawersGetUniqueIds() {
        val d = DefaultDrawers.buildFor("f1", FreezerType.Upright)
        assertEquals(d.size, d.map { it.id }.toSet().size)
    }

    @Test fun searchByNameCategoryDrawerNote() {
        val drawers = listOf(FreezerDrawer(id = "d1", freezerId = "f1", name = "Garage Basket"))
        val products = listOf(
            FrozenProduct(id = "1", freezerId = "f1", drawerId = "d1", name = "Chicken", note = "for stew"),
            FrozenProduct(id = "2", freezerId = "f1", drawerId = "d1", name = "Peas")
        )
        val byName = SearchUtils.search(products, drawers, "chick", SearchUtils.Filters(), 7)
        assertEquals(1, byName.size)
        val byNote = SearchUtils.search(products, drawers, "stew", SearchUtils.Filters(), 7)
        assertEquals(1, byNote.size)
        val byDrawer = SearchUtils.search(products, drawers, "garage", SearchUtils.Filters(), 7)
        assertEquals(2, byDrawer.size)
    }

    @Test fun searchUseFirstFilter() {
        val products = listOf(
            FrozenProduct(id = "1", name = "A", useFirst = true),
            FrozenProduct(id = "2", name = "B", useFirst = false)
        )
        val result = SearchUtils.search(products, emptyList(), "",
            SearchUtils.Filters(useFirstOnly = true), 7)
        assertEquals(1, result.size)
        assertEquals("1", result.first().id)
    }
}
