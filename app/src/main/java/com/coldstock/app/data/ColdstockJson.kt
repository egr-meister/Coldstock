package com.coldstock.app.data

import kotlinx.serialization.json.Json

/**
 * Shared lenient JSON configuration for all Coldstock persistence.
 *
 * - ignoreUnknownKeys: forward compatible if a key is removed later.
 * - encodeDefaults: newer fields always written so old readers stay consistent.
 * - isLenient / coerceInputValues: recover from minor malformations and null
 *   enum values by coercing to defaults rather than throwing.
 */
val ColdstockJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
    coerceInputValues = true
    allowStructuredMapKeys = true
}
