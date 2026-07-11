package com.coldstock.app.util

/**
 * Non-negative integer portion helpers with a sane maximum.
 */
object PortionUtils {

    const val MAX_PORTIONS = 100_000
    const val STEP = 1

    fun clamp(value: Int): Int = value.coerceIn(0, MAX_PORTIONS)

    fun increment(current: Int, step: Int = STEP): Int = clamp(current + step)

    fun decrement(current: Int, step: Int = STEP): Int = clamp(current - step)

    /** Parses free-typed portion input safely; blank -> 0, invalid -> null. */
    fun parse(raw: String): Int? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return 0
        val n = trimmed.toIntOrNull() ?: return null
        if (n < 0) return null
        return clamp(n)
    }

    fun describe(count: Int, label: String): String {
        val cleanLabel = label.trim()
        return if (cleanLabel.isEmpty()) {
            if (count == 1) "$count portion" else "$count portions"
        } else {
            "$count $cleanLabel"
        }
    }
}
