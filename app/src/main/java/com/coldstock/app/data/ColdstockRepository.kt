package com.coldstock.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.coldstock.app.model.AppSettings
import com.coldstock.app.model.DrawerType
import com.coldstock.app.model.FreezerDrawer
import com.coldstock.app.model.FreezerProfile
import com.coldstock.app.model.FreezerType
import com.coldstock.app.model.FrozenFoodCategory
import com.coldstock.app.model.FrozenProduct
import com.coldstock.app.model.ProductHistoryEvent
import com.coldstock.app.model.ProductHistoryEventType
import com.coldstock.app.model.ProductLifecycleState
import com.coldstock.app.model.StorageDurationUnit
import com.coldstock.app.util.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "coldstock_store")

/**
 * Single local data repository backed by DataStore Preferences (serialized JSON
 * strings). All operations are guarded; malformed data recovers to safe
 * fallbacks and never crashes. No cloud, no network, no backup code.
 */
class ColdstockRepository(private val context: Context) {

    private object Keys {
        val FREEZERS = stringPreferencesKey("freezer_profiles_json")
        val DRAWERS = stringPreferencesKey("freezer_drawers_json")
        val PRODUCTS = stringPreferencesKey("frozen_products_json")
        val HISTORY = stringPreferencesKey("product_history_json")
        val SETTINGS = stringPreferencesKey("settings_json")
    }

    // -----------------------------------------------------------------------
    // Observable state
    // -----------------------------------------------------------------------

    val appData: Flow<AppData> = context.dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { prefs -> readAppData(prefs) }

    private fun readAppData(prefs: Preferences): AppData {
        val freezers = decodeList(prefs[Keys.FREEZERS]) { it: FreezerProfile -> it.id.isNotBlank() }
        val drawers = decodeList(prefs[Keys.DRAWERS]) { it: FreezerDrawer -> it.id.isNotBlank() }
        val products = decodeList(prefs[Keys.PRODUCTS]) { it: FrozenProduct -> it.id.isNotBlank() }
            .map { sanitizeProduct(it) }
        val history = decodeList(prefs[Keys.HISTORY]) { it: ProductHistoryEvent -> it.id.isNotBlank() }
        val settings = decodeSettings(prefs[Keys.SETTINGS])
        return AppData(
            freezers = freezers,
            drawers = drawers.sortedBy { it.sortOrder },
            products = products,
            historyEvents = history,
            settings = settings,
            loaded = true
        )
    }

    /** Item-level recovery: drops only malformed entries, keeps valid ones. */
    private inline fun <reified T> decodeList(raw: String?, valid: (T) -> Boolean): List<T> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            ColdstockJson.decodeFromString(ListSerializer(kotlinx.serialization.serializer<T>()), raw)
                .filter { valid(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun decodeSettings(raw: String?): AppSettings {
        if (raw.isNullOrBlank()) return AppSettings()
        return try {
            ColdstockJson.decodeFromString(AppSettings.serializer(), raw)
        } catch (e: Exception) {
            AppSettings()
        }
    }

    private fun sanitizeProduct(p: FrozenProduct): FrozenProduct {
        val safePortion = if (p.portionCount < 0) 0 else p.portionCount
        return if (safePortion == p.portionCount) p else p.copy(portionCount = safePortion)
    }

    // -----------------------------------------------------------------------
    // Low-level persistence helpers
    // -----------------------------------------------------------------------

    private suspend fun mutate(block: (AppData) -> AppData) {
        context.dataStore.edit { prefs ->
            val current = readAppData(prefs)
            val next = block(current)
            prefs[Keys.FREEZERS] = ColdstockJson.encodeToString(
                ListSerializer(FreezerProfile.serializer()), next.freezers
            )
            prefs[Keys.DRAWERS] = ColdstockJson.encodeToString(
                ListSerializer(FreezerDrawer.serializer()), next.drawers.sortedBy { it.sortOrder }
            )
            prefs[Keys.PRODUCTS] = ColdstockJson.encodeToString(
                ListSerializer(FrozenProduct.serializer()), next.products
            )
            prefs[Keys.HISTORY] = ColdstockJson.encodeToString(
                ListSerializer(ProductHistoryEvent.serializer()), next.historyEvents
            )
            prefs[Keys.SETTINGS] = ColdstockJson.encodeToString(
                AppSettings.serializer(), next.settings
            )
        }
    }

    private fun newId(): String = UUID.randomUUID().toString()

    private fun historyEvent(
        productId: String,
        freezerId: String,
        type: ProductHistoryEventType,
        description: String,
        before: Int? = null,
        after: Int? = null
    ): ProductHistoryEvent = ProductHistoryEvent(
        id = newId(),
        productId = productId,
        freezerId = freezerId,
        eventType = type,
        eventDate = Clock.todayIso(),
        eventTime = Clock.nowTimeIso(),
        portionsBefore = before,
        portionsAfter = after,
        description = description,
        createdAt = Clock.nowTimestamp()
    )

    // -----------------------------------------------------------------------
    // Settings
    // -----------------------------------------------------------------------

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) =
        mutate { it.copy(settings = transform(it.settings)) }

    suspend fun completeOnboarding() =
        updateSettings { it.copy(onboardingCompleted = true) }

    suspend fun setActiveFreezer(freezerId: String?) =
        updateSettings { it.copy(activeFreezerId = freezerId) }

    // -----------------------------------------------------------------------
    // Freezer profiles
    // -----------------------------------------------------------------------

    suspend fun addFreezer(
        name: String,
        type: FreezerType,
        description: String,
        makeActive: Boolean = true
    ): String {
        val id = newId()
        val now = Clock.nowTimestamp()
        mutate { data ->
            val freezer = FreezerProfile(
                id = id,
                name = name.trim(),
                freezerType = type,
                description = description.trim(),
                createdAt = now,
                updatedAt = now
            )
            val defaults = DefaultDrawers.buildFor(id, type)
            val newSettings = if (makeActive || data.settings.activeFreezerId.isNullOrBlank())
                data.settings.copy(activeFreezerId = id) else data.settings
            data.copy(
                freezers = data.freezers + freezer,
                drawers = data.drawers + defaults,
                settings = newSettings
            )
        }
        return id
    }

    suspend fun editFreezer(
        freezerId: String,
        name: String,
        type: FreezerType,
        description: String
    ) = mutate { data ->
        data.copy(freezers = data.freezers.map { f ->
            if (f.id == freezerId) f.copy(
                name = name.trim(),
                freezerType = type,
                description = description.trim(),
                updatedAt = Clock.nowTimestamp()
            ) else f
        })
    }

    suspend fun deleteFreezer(freezerId: String) = mutate { data ->
        val remainingFreezers = data.freezers.filterNot { it.id == freezerId }
        val remainingDrawers = data.drawers.filterNot { it.freezerId == freezerId }
        val remainingProducts = data.products.filterNot { it.freezerId == freezerId }
        val remainingHistory = data.historyEvents.filterNot { it.freezerId == freezerId }
        val newActive = if (data.settings.activeFreezerId == freezerId)
            remainingFreezers.firstOrNull()?.id else data.settings.activeFreezerId
        data.copy(
            freezers = remainingFreezers,
            drawers = remainingDrawers,
            products = remainingProducts,
            historyEvents = remainingHistory,
            settings = data.settings.copy(activeFreezerId = newActive)
        )
    }

    // -----------------------------------------------------------------------
    // Drawers
    // -----------------------------------------------------------------------

    suspend fun addDrawer(
        freezerId: String,
        name: String,
        type: DrawerType,
        note: String = ""
    ): String {
        val id = newId()
        val now = Clock.nowTimestamp()
        mutate { data ->
            val maxOrder = data.drawers.filter { it.freezerId == freezerId }
                .maxOfOrNull { it.sortOrder } ?: -1
            val drawer = FreezerDrawer(
                id = id,
                freezerId = freezerId,
                name = name.trim(),
                drawerType = type,
                sortOrder = maxOrder + 1,
                enabled = true,
                note = note.trim(),
                createdAt = now,
                updatedAt = now
            )
            data.copy(drawers = data.drawers + drawer)
        }
        return id
    }

    suspend fun editDrawer(
        drawerId: String,
        name: String,
        type: DrawerType,
        note: String
    ) = mutate { data ->
        data.copy(drawers = data.drawers.map { d ->
            if (d.id == drawerId) d.copy(
                name = name.trim(),
                drawerType = type,
                note = note.trim(),
                updatedAt = Clock.nowTimestamp()
            ) else d
        })
    }

    suspend fun moveDrawer(freezerId: String, drawerId: String, up: Boolean) = mutate { data ->
        val ordered = data.drawers.filter { it.freezerId == freezerId }
            .sortedBy { it.sortOrder }
            .toMutableList()
        val index = ordered.indexOfFirst { it.id == drawerId }
        if (index == -1) return@mutate data
        val target = if (up) index - 1 else index + 1
        if (target < 0 || target >= ordered.size) return@mutate data
        val tmp = ordered[index]
        ordered[index] = ordered[target]
        ordered[target] = tmp
        val renumbered = ordered.mapIndexed { i, d -> d.copy(sortOrder = i) }
        val others = data.drawers.filter { it.freezerId != freezerId }
        data.copy(drawers = others + renumbered)
    }

    /**
     * Enable/disable a drawer. When disabling a drawer that still contains
     * active products, the caller must first move those products; this method
     * never deletes products.
     */
    suspend fun setDrawerEnabled(drawerId: String, enabled: Boolean) = mutate { data ->
        data.copy(drawers = data.drawers.map { d ->
            if (d.id == drawerId) d.copy(enabled = enabled, updatedAt = Clock.nowTimestamp()) else d
        })
    }

    /**
     * Delete a drawer, reassigning any products it holds to [reassignToDrawerId]
     * (or leaving them unassigned when null). Products are never removed.
     */
    suspend fun deleteDrawer(drawerId: String, reassignToDrawerId: String?) = mutate { data ->
        val movedProducts = data.products.map { p ->
            if (p.drawerId == drawerId) p.copy(
                drawerId = reassignToDrawerId ?: "",
                updatedAt = Clock.nowTimestamp()
            ) else p
        }
        data.copy(
            drawers = data.drawers.filterNot { it.id == drawerId },
            products = movedProducts
        )
    }

    suspend fun restoreDefaultDrawers(freezerId: String) = mutate { data ->
        val freezer = data.freezers.firstOrNull { it.id == freezerId } ?: return@mutate data
        val existing = data.drawers.filter { it.freezerId == freezerId }
        val defaults = DefaultDrawers.buildFor(freezerId, freezer.freezerType)
        // Add only default names that are not already present (prevents duplicates).
        val existingNames = existing.map { it.name.lowercase().trim() }.toSet()
        val maxOrder = existing.maxOfOrNull { it.sortOrder } ?: -1
        val toAdd = defaults
            .filter { it.name.lowercase().trim() !in existingNames }
            .mapIndexed { i, d -> d.copy(sortOrder = maxOrder + 1 + i) }
        // Re-enable any disabled default-named drawers as well.
        val reEnabled = existing.map { d ->
            if (defaults.any { it.name.equals(d.name, ignoreCase = true) } && !d.enabled)
                d.copy(enabled = true, updatedAt = Clock.nowTimestamp()) else d
        }
        val others = data.drawers.filter { it.freezerId != freezerId }
        data.copy(drawers = others + reEnabled + toAdd)
    }

    // -----------------------------------------------------------------------
    // Products
    // -----------------------------------------------------------------------

    suspend fun addProduct(product: FrozenProduct): String {
        val id = newId()
        val now = Clock.nowTimestamp()
        mutate { data ->
            val safe = product.copy(
                id = id,
                portionCount = product.portionCount.coerceAtLeast(0),
                createdAt = now,
                updatedAt = now
            )
            val events = mutableListOf(
                historyEvent(id, safe.freezerId, ProductHistoryEventType.Created,
                    "Product created", after = safe.portionCount)
            )
            if (safe.useFirst) {
                events += historyEvent(id, safe.freezerId,
                    ProductHistoryEventType.AddedToUseFirst, "Marked Use First")
            }
            data.copy(
                products = data.products + safe,
                historyEvents = data.historyEvents + events
            )
        }
        return id
    }

    suspend fun updateProduct(product: FrozenProduct) = mutate { data ->
        val existing = data.products.firstOrNull { it.id == product.id } ?: return@mutate data
        val safe = product.copy(
            portionCount = product.portionCount.coerceAtLeast(0),
            updatedAt = Clock.nowTimestamp()
        )
        val events = mutableListOf(
            historyEvent(product.id, safe.freezerId, ProductHistoryEventType.Updated, "Product updated")
        )
        if (existing.useFirst != safe.useFirst) {
            events += if (safe.useFirst)
                historyEvent(product.id, safe.freezerId,
                    ProductHistoryEventType.AddedToUseFirst, "Marked Use First")
            else
                historyEvent(product.id, safe.freezerId,
                    ProductHistoryEventType.RemovedFromUseFirst, "Removed from Use First")
        }
        data.copy(
            products = data.products.map { if (it.id == product.id) safe else it },
            historyEvents = data.historyEvents + events
        )
    }

    suspend fun deleteProduct(productId: String) = mutate { data ->
        data.copy(products = data.products.filterNot { it.id == productId })
    }

    suspend fun moveProduct(productId: String, freezerId: String, drawerId: String) = mutate { data ->
        val p = data.products.firstOrNull { it.id == productId } ?: return@mutate data
        val moved = p.copy(freezerId = freezerId, drawerId = drawerId, updatedAt = Clock.nowTimestamp())
        val event = historyEvent(productId, freezerId, ProductHistoryEventType.Moved, "Product moved")
        data.copy(
            products = data.products.map { if (it.id == productId) moved else it },
            historyEvents = data.historyEvents + event
        )
    }

    suspend fun setPortionCount(productId: String, newCount: Int) = mutate { data ->
        val p = data.products.firstOrNull { it.id == productId } ?: return@mutate data
        val safe = newCount.coerceAtLeast(0)
        if (safe == p.portionCount) return@mutate data
        val updated = p.copy(portionCount = safe, updatedAt = Clock.nowTimestamp())
        val event = historyEvent(productId, p.freezerId, ProductHistoryEventType.PortionChanged,
            "Portions changed", before = p.portionCount, after = safe)
        data.copy(
            products = data.products.map { if (it.id == productId) updated else it },
            historyEvents = data.historyEvents + event
        )
    }

    suspend fun adjustPortions(productId: String, delta: Int) = mutate { data ->
        val p = data.products.firstOrNull { it.id == productId } ?: return@mutate data
        val safe = (p.portionCount + delta).coerceAtLeast(0)
        if (safe == p.portionCount) return@mutate data
        val updated = p.copy(portionCount = safe, updatedAt = Clock.nowTimestamp())
        val event = historyEvent(productId, p.freezerId, ProductHistoryEventType.PortionChanged,
            "Portions changed", before = p.portionCount, after = safe)
        data.copy(
            products = data.products.map { if (it.id == productId) updated else it },
            historyEvents = data.historyEvents + event
        )
    }

    suspend fun markUsed(productId: String, note: String, portionsUsed: Int?) = mutate { data ->
        val p = data.products.firstOrNull { it.id == productId } ?: return@mutate data
        val after = 0
        val updated = p.copy(
            lifecycleState = ProductLifecycleState.Used,
            useFirst = false,
            portionCount = after,
            note = if (note.isBlank()) p.note else note.trim(),
            updatedAt = Clock.nowTimestamp()
        )
        val desc = buildString {
            append("Marked used")
            if (portionsUsed != null) append(" (${portionsUsed} used)")
            if (note.isNotBlank()) append(": ${note.trim()}")
        }
        val event = historyEvent(productId, p.freezerId, ProductHistoryEventType.MarkedUsed,
            desc, before = p.portionCount, after = after)
        data.copy(
            products = data.products.map { if (it.id == productId) updated else it },
            historyEvents = data.historyEvents + event
        )
    }

    suspend fun markDiscarded(productId: String, reason: String) = mutate { data ->
        val p = data.products.firstOrNull { it.id == productId } ?: return@mutate data
        val updated = p.copy(
            lifecycleState = ProductLifecycleState.Discarded,
            useFirst = false,
            updatedAt = Clock.nowTimestamp()
        )
        val desc = if (reason.isBlank()) "Marked discarded" else "Marked discarded: $reason"
        val event = historyEvent(productId, p.freezerId, ProductHistoryEventType.MarkedDiscarded,
            desc, before = p.portionCount, after = p.portionCount)
        data.copy(
            products = data.products.map { if (it.id == productId) updated else it },
            historyEvents = data.historyEvents + event
        )
    }

    suspend fun restoreProduct(
        productId: String,
        freezerId: String,
        drawerId: String,
        portionCount: Int
    ) = mutate { data ->
        val p = data.products.firstOrNull { it.id == productId } ?: return@mutate data
        val safe = portionCount.coerceAtLeast(0)
        val updated = p.copy(
            lifecycleState = ProductLifecycleState.Active,
            freezerId = freezerId,
            drawerId = drawerId,
            portionCount = safe,
            updatedAt = Clock.nowTimestamp()
        )
        val event = historyEvent(productId, freezerId, ProductHistoryEventType.Restored,
            "Product restored to active", before = p.portionCount, after = safe)
        data.copy(
            products = data.products.map { if (it.id == productId) updated else it },
            historyEvents = data.historyEvents + event
        )
    }

    suspend fun toggleUseFirst(productId: String, useFirst: Boolean) = mutate { data ->
        val p = data.products.firstOrNull { it.id == productId } ?: return@mutate data
        if (p.useFirst == useFirst) return@mutate data
        val updated = p.copy(useFirst = useFirst, updatedAt = Clock.nowTimestamp())
        val event = if (useFirst)
            historyEvent(productId, p.freezerId, ProductHistoryEventType.AddedToUseFirst, "Marked Use First")
        else
            historyEvent(productId, p.freezerId, ProductHistoryEventType.RemovedFromUseFirst, "Removed from Use First")
        data.copy(
            products = data.products.map { if (it.id == productId) updated else it },
            historyEvents = data.historyEvents + event
        )
    }

    // -----------------------------------------------------------------------
    // History & destructive maintenance
    // -----------------------------------------------------------------------

    suspend fun deleteUsedHistory() = mutate { data ->
        data.copy(historyEvents = data.historyEvents.filterNot {
            it.eventType == ProductHistoryEventType.MarkedUsed
        })
    }

    suspend fun deleteDiscardedHistory() = mutate { data ->
        data.copy(historyEvents = data.historyEvents.filterNot {
            it.eventType == ProductHistoryEventType.MarkedDiscarded
        })
    }

    suspend fun deleteAllActiveProducts() = mutate { data ->
        val activeIds = data.products.filter { it.lifecycleState == ProductLifecycleState.Active }
            .map { it.id }.toSet()
        data.copy(products = data.products.filterNot { it.id in activeIds })
    }

    suspend fun resetAllData() {
        context.dataStore.edit { it.clear() }
    }
}
