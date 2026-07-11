package com.coldstock.app.util

import com.coldstock.app.model.FreezerDrawer
import com.coldstock.app.model.FrozenFoodCategory
import com.coldstock.app.model.FrozenProduct
import com.coldstock.app.model.FrozenProductStatus
import com.coldstock.app.model.ProductLifecycleState
import java.time.LocalDate

/** Fully local search & filtering. No text ever leaves the device. */
object SearchUtils {

    data class Filters(
        val freezerId: String? = null,
        val drawerId: String? = null,
        val category: FrozenFoodCategory? = null,
        val statuses: Set<FrozenProductStatus> = emptySet(),
        val useFirstOnly: Boolean = false
    )

    fun search(
        products: List<FrozenProduct>,
        drawers: List<FreezerDrawer>,
        query: String,
        filters: Filters,
        soonThreshold: Int,
        today: LocalDate = Clock.today()
    ): List<FrozenProduct> {
        val q = query.trim().lowercase()
        val drawerNames = drawers.associate { it.id to it.name.lowercase() }

        return products.filter { p ->
            if (filters.freezerId != null && p.freezerId != filters.freezerId) return@filter false
            if (filters.drawerId != null && p.drawerId != filters.drawerId) return@filter false
            if (filters.category != null && p.category != filters.category) return@filter false
            if (filters.useFirstOnly && !p.useFirst) return@filter false

            if (filters.statuses.isNotEmpty()) {
                val status = StatusUtils.statusOf(p, soonThreshold, today)
                if (status !in filters.statuses) return@filter false
            }

            if (q.isNotEmpty()) {
                val drawerName = drawerNames[p.drawerId] ?: ""
                val haystack = listOf(
                    p.name.lowercase(),
                    p.categoryDisplay.lowercase(),
                    drawerName,
                    p.note.lowercase()
                )
                if (haystack.none { it.contains(q) }) return@filter false
            }
            true
        }
    }
}
