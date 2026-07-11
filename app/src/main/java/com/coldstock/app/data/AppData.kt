package com.coldstock.app.data

import com.coldstock.app.model.AppSettings
import com.coldstock.app.model.FreezerDrawer
import com.coldstock.app.model.FreezerProfile
import com.coldstock.app.model.FrozenProduct
import com.coldstock.app.model.ProductHistoryEvent

/**
 * In-memory snapshot of the full application state, combined from the
 * individually stored DataStore JSON keys. Never persisted as a single blob.
 */
data class AppData(
    val freezers: List<FreezerProfile> = emptyList(),
    val drawers: List<FreezerDrawer> = emptyList(),
    val products: List<FrozenProduct> = emptyList(),
    val historyEvents: List<ProductHistoryEvent> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val loaded: Boolean = false
)
