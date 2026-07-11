package com.coldstock.app.data

import com.coldstock.app.model.DrawerType
import com.coldstock.app.model.FreezerDrawer
import com.coldstock.app.model.FreezerType
import com.coldstock.app.util.Clock
import java.util.UUID

/**
 * Provides the default drawer / section layout for a freezer type.
 * These are organizational names only, not storage recommendations.
 */
object DefaultDrawers {

    private data class DrawerSpec(val name: String, val type: DrawerType)

    private fun specsFor(type: FreezerType): List<DrawerSpec> = when (type) {
        FreezerType.Chest -> listOf(
            DrawerSpec("Left Basket", DrawerType.Basket),
            DrawerSpec("Right Basket", DrawerType.Basket),
            DrawerSpec("Main Compartment", DrawerType.Custom),
            DrawerSpec("Bottom Section", DrawerType.Custom),
        )
        FreezerType.Upright, FreezerType.FridgeFreezer, FreezerType.Compact, FreezerType.Other -> listOf(
            DrawerSpec("Top Drawer", DrawerType.Drawer),
            DrawerSpec("Upper Middle Drawer", DrawerType.Drawer),
            DrawerSpec("Lower Middle Drawer", DrawerType.Drawer),
            DrawerSpec("Bottom Drawer", DrawerType.Drawer),
        )
    }

    fun buildFor(freezerId: String, type: FreezerType): List<FreezerDrawer> {
        val now = Clock.nowTimestamp()
        return specsFor(type).mapIndexed { index, spec ->
            FreezerDrawer(
                id = UUID.randomUUID().toString(),
                freezerId = freezerId,
                name = spec.name,
                drawerType = spec.type,
                sortOrder = index,
                enabled = true,
                note = "",
                createdAt = now,
                updatedAt = now
            )
        }
    }
}
