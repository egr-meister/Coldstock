package com.coldstock.app.util

import com.coldstock.app.model.StorageDurationUnit

/** Input validation shared by Add/Edit product forms. */
object ValidationUtils {

    data class ProductFormError(
        val name: String? = null,
        val freezer: String? = null,
        val drawer: String? = null,
        val freezingDate: String? = null,
        val duration: String? = null,
        val portions: String? = null
    ) {
        val hasError: Boolean
            get() = listOf(name, freezer, drawer, freezingDate, duration, portions).any { it != null }
    }

    fun validateProduct(
        name: String,
        freezerId: String?,
        drawerId: String?,
        allowUnassignedDrawer: Boolean,
        freezingDate: String,
        durationText: String,
        durationUnit: StorageDurationUnit,
        portionText: String
    ): ProductFormError {
        val nameErr = if (name.trim().isEmpty()) "Product name is required." else null
        val freezerErr = if (freezerId.isNullOrBlank()) "Select a freezer." else null
        val drawerErr = if (drawerId.isNullOrBlank() && !allowUnassignedDrawer)
            "Select a drawer." else null
        val dateErr = if (!DateUtils.isValidDate(freezingDate))
            "Enter a valid freezing date." else null

        val durationErr = if (durationText.isBlank()) {
            null // duration is optional
        } else {
            val v = durationText.trim().toIntOrNull()
            when {
                v == null -> "Duration must be a whole number."
                v <= 0 -> "Duration must be greater than zero."
                v > DateUtils.maxFor(durationUnit) ->
                    "Duration is too large (max ${DateUtils.maxFor(durationUnit)} ${durationUnit.label.lowercase()})."
                else -> null
            }
        }

        val portionErr = when (val parsed = PortionUtils.parse(portionText)) {
            null -> "Portions must be zero or a positive whole number."
            else -> if (parsed > PortionUtils.MAX_PORTIONS) "Portion count is too large." else null
        }

        return ProductFormError(nameErr, freezerErr, drawerErr, dateErr, durationErr, portionErr)
    }
}
