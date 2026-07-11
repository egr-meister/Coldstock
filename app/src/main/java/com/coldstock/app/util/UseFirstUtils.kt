package com.coldstock.app.util

import com.coldstock.app.model.FrozenProduct
import com.coldstock.app.model.FrozenProductStatus
import java.time.LocalDate

/**
 * Builds and orders the Use First list. Items qualify through calculated
 * Review Date Passed / Use Soon status, or a manual user flag. Ordering is
 * organizational only; no consumption or safety recommendation is implied.
 */
object UseFirstUtils {

    enum class Section { ReviewDatePassed, UseSoon, ManuallyPrioritized, NoReviewDate }

    data class Entry(val product: FrozenProduct, val section: Section)

    /**
     * Full ordered Use First list across sections:
     * 1. oldest passed review date first
     * 2. nearest review date first
     * 3. manually prioritized (most recently updated first)
     * 4. most recently added without review date
     */
    fun ordered(
        products: List<FrozenProduct>,
        soonThreshold: Int,
        freezerId: String? = null,
        today: LocalDate = Clock.today()
    ): List<Entry> {
        val active = products.filter {
            (freezerId == null || it.freezerId == freezerId) &&
                it.lifecycleState == com.coldstock.app.model.ProductLifecycleState.Active
        }
        fun review(p: FrozenProduct) = DateUtils.plannedReviewDate(
            p.freezingDate, p.storageDurationValue, p.storageDurationUnit
        )

        val passed = mutableListOf<FrozenProduct>()
        val soon = mutableListOf<FrozenProduct>()
        val manual = mutableListOf<FrozenProduct>()
        val noDate = mutableListOf<FrozenProduct>()

        for (p in active) {
            when (StatusUtils.statusOf(p, soonThreshold, today)) {
                FrozenProductStatus.Old -> passed += p
                FrozenProductStatus.UseSoon -> soon += p
                FrozenProductStatus.NoReviewDate -> if (p.useFirst) noDate += p
                FrozenProductStatus.Ok -> if (p.useFirst) manual += p
                else -> Unit
            }
        }

        val orderedPassed = passed.sortedBy { review(it) ?: LocalDate.MAX }
        val orderedSoon = soon.sortedBy { review(it) ?: LocalDate.MAX }
        val orderedManual = manual.sortedByDescending { it.updatedAt }
        val orderedNoDate = noDate.sortedByDescending { it.createdAt }

        return orderedPassed.map { Entry(it, Section.ReviewDatePassed) } +
            orderedSoon.map { Entry(it, Section.UseSoon) } +
            orderedManual.map { Entry(it, Section.ManuallyPrioritized) } +
            orderedNoDate.map { Entry(it, Section.NoReviewDate) }
    }

    fun sectionLabel(section: Section): String = when (section) {
        Section.ReviewDatePassed -> "Review Date Passed"
        Section.UseSoon -> "Use Soon"
        Section.ManuallyPrioritized -> "Manually Prioritized"
        Section.NoReviewDate -> "No Review Date"
    }
}
