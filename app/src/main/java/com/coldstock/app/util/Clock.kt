package com.coldstock.app.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Central access point for the current local date/time.
 *
 * Uses the device's local calendar date only. No network time is used.
 * A test hook (`override`) allows deterministic unit tests.
 */
object Clock {

    @Volatile
    var override: (() -> LocalDateTime)? = null

    fun now(): LocalDateTime = override?.invoke() ?: LocalDateTime.now()

    fun today(): LocalDate = now().toLocalDate()

    fun nowTime(): LocalTime = now().toLocalTime()

    /** ISO date, e.g. 2026-07-10. */
    fun todayIso(): String = today().format(DateTimeFormatter.ISO_LOCAL_DATE)

    /** HH:mm, e.g. 14:05. */
    fun nowTimeIso(): String = nowTime().format(DateTimeFormatter.ofPattern("HH:mm"))

    /** ISO-8601 timestamp without offset, e.g. 2026-07-10T14:05:09. */
    fun nowTimestamp(): String =
        now().withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}
