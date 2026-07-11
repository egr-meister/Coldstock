package com.coldstock.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.coldstock.app.data.AppData
import com.coldstock.app.data.ColdstockRepository
import com.coldstock.app.model.AppSettings
import com.coldstock.app.model.DrawerType
import com.coldstock.app.model.FreezerDrawer
import com.coldstock.app.model.FreezerProfile
import com.coldstock.app.model.FreezerType
import com.coldstock.app.model.FrozenProduct
import com.coldstock.app.model.ProductHistoryEvent
import com.coldstock.app.model.ProductLifecycleState
import com.coldstock.app.model.ReminderSettings
import com.coldstock.app.model.StorageDurationUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Single shared ViewModel for Coldstock (simple MVVM, no DI framework).
 * Exposes immutable app state as StateFlow and mutation actions that delegate
 * to the repository. Session-only UI signals (dismissed reminders) live here.
 */
class ColdstockViewModel(private val repository: ColdstockRepository) : ViewModel() {

    val uiState: StateFlow<AppData> = repository.appData
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppData())

    // Session-only: reminder ids the user dismissed until app restart.
    private val dismissedReminderKinds = mutableSetOf<String>()
    fun dismissReminder(key: String) { dismissedReminderKinds.add(key) }
    fun isReminderDismissed(key: String): Boolean = dismissedReminderKinds.contains(key)
    fun clearDismissedReminders() { dismissedReminderKinds.clear() }

    // --- Lookups ----------------------------------------------------------

    fun freezer(id: String?): FreezerProfile? =
        uiState.value.freezers.firstOrNull { it.id == id }

    fun drawer(id: String?): FreezerDrawer? =
        uiState.value.drawers.firstOrNull { it.id == id }

    fun product(id: String?): FrozenProduct? =
        uiState.value.products.firstOrNull { it.id == id }

    fun historyEvent(id: String?): ProductHistoryEvent? =
        uiState.value.historyEvents.firstOrNull { it.id == id }

    fun drawersFor(freezerId: String?): List<FreezerDrawer> =
        uiState.value.drawers.filter { it.freezerId == freezerId }.sortedBy { it.sortOrder }

    fun enabledDrawersFor(freezerId: String?): List<FreezerDrawer> =
        drawersFor(freezerId).filter { it.enabled }

    // --- Settings ---------------------------------------------------------

    fun completeOnboarding() = launch { repository.completeOnboarding() }
    fun setActiveFreezer(id: String?) = launch { repository.setActiveFreezer(id) }
    fun updateSettings(transform: (AppSettings) -> AppSettings) =
        launch { repository.updateSettings(transform) }
    fun setSoonThreshold(days: Int) = updateSettings { it.copy(soonThresholdDays = days) }
    fun setDefaultDurationUnit(unit: StorageDurationUnit) =
        updateSettings { it.copy(defaultDurationUnit = unit) }
    fun setRememberDurations(value: Boolean) =
        updateSettings { it.copy(rememberPreviousDurations = value) }
    fun setReminders(transform: (ReminderSettings) -> ReminderSettings) =
        updateSettings { it.copy(reminderSettings = transform(it.reminderSettings)) }
    fun setShowOnboardingAgain() = updateSettings { it.copy(onboardingCompleted = false) }

    // --- Freezers ---------------------------------------------------------

    fun addFreezer(name: String, type: FreezerType, description: String, onDone: (String) -> Unit = {}) =
        launch { onDone(repository.addFreezer(name, type, description)) }

    fun editFreezer(id: String, name: String, type: FreezerType, description: String) =
        launch { repository.editFreezer(id, name, type, description) }

    fun deleteFreezer(id: String) = launch { repository.deleteFreezer(id) }

    // --- Drawers ----------------------------------------------------------

    fun addDrawer(freezerId: String, name: String, type: DrawerType, note: String = "") =
        launch { repository.addDrawer(freezerId, name, type, note) }

    fun editDrawer(id: String, name: String, type: DrawerType, note: String) =
        launch { repository.editDrawer(id, name, type, note) }

    fun moveDrawer(freezerId: String, drawerId: String, up: Boolean) =
        launch { repository.moveDrawer(freezerId, drawerId, up) }

    fun setDrawerEnabled(drawerId: String, enabled: Boolean) =
        launch { repository.setDrawerEnabled(drawerId, enabled) }

    fun deleteDrawer(drawerId: String, reassignToDrawerId: String?) =
        launch { repository.deleteDrawer(drawerId, reassignToDrawerId) }

    fun restoreDefaultDrawers(freezerId: String) =
        launch { repository.restoreDefaultDrawers(freezerId) }

    // --- Products ---------------------------------------------------------

    fun addProduct(product: FrozenProduct, onDone: (String) -> Unit = {}) =
        launch { onDone(repository.addProduct(product)) }

    fun updateProduct(product: FrozenProduct) = launch { repository.updateProduct(product) }
    fun deleteProduct(id: String) = launch { repository.deleteProduct(id) }
    fun moveProduct(id: String, freezerId: String, drawerId: String) =
        launch { repository.moveProduct(id, freezerId, drawerId) }
    fun setPortionCount(id: String, count: Int) = launch { repository.setPortionCount(id, count) }
    fun adjustPortions(id: String, delta: Int) = launch { repository.adjustPortions(id, delta) }
    fun markUsed(id: String, note: String = "", portionsUsed: Int? = null) =
        launch { repository.markUsed(id, note, portionsUsed) }
    fun markDiscarded(id: String, reason: String = "") = launch { repository.markDiscarded(id, reason) }
    fun restoreProduct(id: String, freezerId: String, drawerId: String, portions: Int) =
        launch { repository.restoreProduct(id, freezerId, drawerId, portions) }
    fun toggleUseFirst(id: String, useFirst: Boolean) =
        launch { repository.toggleUseFirst(id, useFirst) }

    /** Last manual duration entered for a matching product name (opt-in). */
    fun previousDurationFor(name: String): Pair<Int, StorageDurationUnit>? {
        if (!uiState.value.settings.rememberPreviousDurations) return null
        val match = uiState.value.products
            .filter { it.name.trim().equals(name.trim(), ignoreCase = true) }
            .filter { it.storageDurationValue != null && it.storageDurationUnit != null }
            .maxByOrNull { it.updatedAt }
        val v = match?.storageDurationValue ?: return null
        val u = match.storageDurationUnit ?: return null
        return v to u
    }

    // --- Maintenance ------------------------------------------------------

    fun deleteUsedHistory() = launch { repository.deleteUsedHistory() }
    fun deleteDiscardedHistory() = launch { repository.deleteDiscardedHistory() }
    fun deleteAllActiveProducts() = launch { repository.deleteAllActiveProducts() }
    fun resetAllData() = launch {
        clearDismissedReminders()
        repository.resetAllData()
    }

    private fun launch(block: suspend () -> Unit) = viewModelScope.launch { block() }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = ColdstockRepository(context.applicationContext)
                    return ColdstockViewModel(repo) as T
                }
            }
    }
}
