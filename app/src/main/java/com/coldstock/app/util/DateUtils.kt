package com.coldstock.app.util

import com.coldstock.app.model.StorageDurationUnit
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Calendar-date utilities. Every function is total: it returns null (or a safe
 * fallback) instead of throwing, so it can never crash Compose UI.
 *
 * All calculations use LocalDate (calendar days), avoiding timezone-dependent
 * millisecond math. Months use plusMonths so calendar length is respected.
 */
object DateUtils {

    private val ISO: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    // Reasonable maximums to prevent overflow / absurd input.
    const val MAX_DAYS = 3650
    const val MAX_WEEKS = 520
    const val MAX_MONTHS = 120

    /** Parse an ISO (YYYY-MM-DD) date string, or null if invalid/blank. */
    fun parseDate(value: String?): LocalDate? {
        if (value.isNullOrBlank()) return null
        return try {
            LocalDate.parse(value.trim(), ISO)
        } catch (e: Exception) {
            null
        }
    }

    fun isValidDate(value: String?): Boolean = parseDate(value) != null

    fun formatIso(date: LocalDate?): String =
        date?.format(ISO) ?: ""

    /** Friendly medium format, e.g. "10 Jul 2026", or fallback text. */
    fun formatFriendly(value: String?): String {
        val date = parseDate(value) ?: return "Date unavailable"
        return try {
            date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
        } catch (e: Exception) {
            "Date unavailable"
        }
    }

    fun formatFriendly(date: LocalDate?): String {
        if (date == null) return "Date unavailable"
        return try {
            date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
        } catch (e: Exception) {
            "Date unavailable"
        }
    }

    fun isDurationValid(value: Int?, unit: StorageDurationUnit?): Boolean {
        if (value == null || unit == null) return false
        if (value <= 0) return false
        return when (unit) {
            StorageDurationUnit.Days -> value <= MAX_DAYS
            StorageDurationUnit.Weeks -> value <= MAX_WEEKS
            StorageDurationUnit.Months -> value <= MAX_MONTHS
        }
    }

    fun maxFor(unit: StorageDurationUnit): Int = when (unit) {
        StorageDurationUnit.Days -> MAX_DAYS
        StorageDurationUnit.Weeks -> MAX_WEEKS
        StorageDurationUnit.Months -> MAX_MONTHS
    }

    /**
     * plannedReviewDate = freezingDate + duration.
     * Returns null if the freezing date is invalid or the duration is missing
     * or invalid. Guards against date overflow.
     */
    fun plannedReviewDate(
        freezingDate: String?,
        durationValue: Int?,
        durationUnit: StorageDurationUnit?
    ): LocalDate? {
        val start = parseDate(freezingDate) ?: return null
        if (!isDurationValid(durationValue, durationUnit)) return null
        val v = durationValue!!.toLong()
        return try {
            when (durationUnit!!) {
                StorageDurationUnit.Days -> start.plusDays(v)
                StorageDurationUnit.Weeks -> start.plusWeeks(v)
                StorageDurationUnit.Months -> start.plusMonths(v)
            }
        } catch (e: Exception) {
            // Arithmetic overflow beyond LocalDate range.
            null
        }
    }

    fun plannedReviewDateIso(
        freezingDate: String?,
        durationValue: Int?,
        durationUnit: StorageDurationUnit?
    ): String? = plannedReviewDate(freezingDate, durationValue, durationUnit)?.let { formatIso(it) }

    /** Positive when review date is in the future, negative when passed. */
    fun daysUntil(reviewDate: LocalDate?, today: LocalDate = Clock.today()): Long? {
        if (reviewDate == null) return null
        return try {
            ChronoUnit.DAYS.between(today, reviewDate)
        } catch (e: Exception) {
            null
        }
    }

    /** Positive number of days after the review date has passed, else 0/null. */
    fun daysAfterReview(reviewDate: LocalDate?, today: LocalDate = Clock.today()): Long? {
        val until = daysUntil(reviewDate, today) ?: return null
        return if (until < 0) -until else 0L
    }
}
