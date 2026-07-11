package com.coldstock.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * All persisted domain models for Coldstock.
 *
 * Everything here is entered manually by the user. Coldstock does not inspect
 * food, monitor freezer conditions, determine food safety, or provide medical,
 * nutritional, dietary, or professional food-storage advice.
 *
 * All models are annotated with @Serializable and use explicit defaults so that
 * older stored JSON (missing newer fields) still deserializes safely.
 */

// ---------------------------------------------------------------------------
// Enums
// ---------------------------------------------------------------------------

@Serializable
enum class FreezerType {
    Upright, Chest, FridgeFreezer, Compact, Other;

    val label: String
        get() = when (this) {
            Upright -> "Upright"
            Chest -> "Chest"
            FridgeFreezer -> "Fridge Freezer"
            Compact -> "Compact"
            Other -> "Other"
        }
}

@Serializable
enum class DrawerType {
    Drawer, Shelf, Basket, DoorCompartment, Custom;

    val label: String
        get() = when (this) {
            Drawer -> "Drawer"
            Shelf -> "Shelf"
            Basket -> "Basket"
            DoorCompartment -> "Door Compartment"
            Custom -> "Custom"
        }
}

@Serializable
enum class FrozenFoodCategory {
    Meat, Fish, Vegetables, Fruit, PreparedMeals, Bread,
    Desserts, Dairy, Sauces, Herbs, Leftovers, Other;

    val label: String
        get() = when (this) {
            Meat -> "Meat"
            Fish -> "Fish"
            Vegetables -> "Vegetables"
            Fruit -> "Fruit"
            PreparedMeals -> "Prepared Meals"
            Bread -> "Bread"
            Desserts -> "Desserts"
            Dairy -> "Dairy"
            Sauces -> "Sauces"
            Herbs -> "Herbs"
            Leftovers -> "Leftovers"
            Other -> "Other"
        }
}

@Serializable
enum class StorageDurationUnit {
    Days, Weeks, Months;

    val label: String
        get() = when (this) {
            Days -> "Days"
            Weeks -> "Weeks"
            Months -> "Months"
        }
}

@Serializable
enum class ProductLifecycleState {
    Active, Used, Discarded
}

@Serializable
enum class ProductHistoryEventType {
    Created, Updated, PortionChanged, Moved, MarkedUsed,
    MarkedDiscarded, Restored, AddedToUseFirst, RemovedFromUseFirst;

    val label: String
        get() = when (this) {
            Created -> "Created"
            Updated -> "Updated"
            PortionChanged -> "Portion changed"
            Moved -> "Moved"
            MarkedUsed -> "Marked used"
            MarkedDiscarded -> "Marked discarded"
            Restored -> "Restored"
            AddedToUseFirst -> "Added to Use First"
            RemovedFromUseFirst -> "Removed from Use First"
        }
}

/**
 * Derived (never persisted) status of a product, computed from lifecycle state,
 * the manually entered freezing date, storage duration, the Soon threshold, and
 * the current local date. Represents only the user-entered storage timeline.
 */
enum class FrozenProductStatus {
    Ok, UseSoon, Old, NoReviewDate, Used, Discarded;

    /** Neutral, non-medical user-facing label. */
    val label: String
        get() = when (this) {
            Ok -> "OK"
            UseSoon -> "Use Soon"
            Old -> "Review Date Passed"
            NoReviewDate -> "No Review Date"
            Used -> "Used"
            Discarded -> "Discarded"
        }
}

// ---------------------------------------------------------------------------
// Data classes
// ---------------------------------------------------------------------------

@Serializable
data class FreezerProfile(
    val id: String = "",
    val name: String = "",
    val freezerType: FreezerType = FreezerType.Upright,
    val description: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class FreezerDrawer(
    val id: String = "",
    val freezerId: String = "",
    val name: String = "",
    val drawerType: DrawerType = DrawerType.Drawer,
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
    val note: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class FrozenProduct(
    val id: String = "",
    val freezerId: String = "",
    val drawerId: String = "",
    val name: String = "",
    val category: FrozenFoodCategory = FrozenFoodCategory.Other,
    val customCategoryName: String = "",
    val freezingDate: String = "",
    val storageDurationValue: Int? = null,
    val storageDurationUnit: StorageDurationUnit? = null,
    val portionCount: Int = 0,
    val portionLabel: String = "",
    val lifecycleState: ProductLifecycleState = ProductLifecycleState.Active,
    val useFirst: Boolean = false,
    val note: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    /** Effective category label, honouring a custom category name for Other. */
    val categoryDisplay: String
        get() = if (category == FrozenFoodCategory.Other && customCategoryName.isNotBlank())
            customCategoryName else category.label
}

@Serializable
data class ProductHistoryEvent(
    val id: String = "",
    val productId: String = "",
    val freezerId: String = "",
    val eventType: ProductHistoryEventType = ProductHistoryEventType.Created,
    val eventDate: String = "",
    val eventTime: String = "",
    val portionsBefore: Int? = null,
    val portionsAfter: Int? = null,
    val description: String = "",
    val createdAt: String = ""
)

@Serializable
data class ReminderSettings(
    val enabled: Boolean = true,
    val showUseSoon: Boolean = true,
    val showReviewDatePassed: Boolean = true,
    val showNoReviewDate: Boolean = true,
    val showLowPortions: Boolean = true
)

@Serializable
data class AppSettings(
    val onboardingCompleted: Boolean = false,
    val activeFreezerId: String? = null,
    val soonThresholdDays: Int = 7,
    val defaultDurationUnit: StorageDurationUnit = StorageDurationUnit.Months,
    val rememberPreviousDurations: Boolean = false,
    val reminderSettings: ReminderSettings = ReminderSettings()
)
