package com.coldstock.app.util

import com.coldstock.app.model.FrozenProduct
import com.coldstock.app.model.FrozenProductStatus
import com.coldstock.app.model.ProductLifecycleState
import com.coldstock.app.model.ReminderSettings
import java.time.LocalDate

/**
 * Evaluates in-app reminders only. No push notifications, no background work,
 * no alarms. Reminders use neutral, organizational wording.
 */
object ReminderUtils {

    enum class Kind { ReviewDatePassed, UseSoon, NoReviewDate, LowPortions, UseFirstItems }

    data class Reminder(val kind: Kind, val message: String, val count: Int)

    fun evaluate(
        products: List<FrozenProduct>,
        settings: ReminderSettings,
        soonThreshold: Int,
        freezerId: String? = null,
        today: LocalDate = Clock.today()
    ): List<Reminder> {
        if (!settings.enabled) return emptyList()

        val active = products.filter {
            (freezerId == null || it.freezerId == freezerId) &&
                it.lifecycleState == ProductLifecycleState.Active
        }

        var passed = 0
        var soon = 0
        var noDate = 0
        var lowPortion = 0
        for (p in active) {
            when (StatusUtils.statusOf(p, soonThreshold, today)) {
                FrozenProductStatus.Old -> passed++
                FrozenProductStatus.UseSoon -> soon++
                FrozenProductStatus.NoReviewDate -> noDate++
                else -> Unit
            }
            if (p.portionCount == 1) lowPortion++
        }

        val useFirstCount = active.count { it.useFirst }
        val reminders = mutableListOf<Reminder>()

        if (settings.showReviewDatePassed && passed > 0) {
            reminders += Reminder(
                Kind.ReviewDatePassed,
                if (passed == 1) "1 freezer item has reached its review date."
                else "$passed freezer items have reached their review date.",
                passed
            )
        }
        if (settings.showUseSoon && soon > 0) {
            reminders += Reminder(
                Kind.UseSoon,
                if (soon == 1) "1 item is entering its Use Soon period."
                else "$soon items are entering their Use Soon period.",
                soon
            )
        }
        if (settings.showNoReviewDate && noDate > 0) {
            reminders += Reminder(
                Kind.NoReviewDate,
                if (noDate == 1) "1 item has no review date entered."
                else "$noDate items have no review date entered.",
                noDate
            )
        }
        if (settings.showLowPortions && lowPortion > 0) {
            reminders += Reminder(
                Kind.LowPortions,
                if (lowPortion == 1) "1 item has one portion remaining."
                else "$lowPortion items have one portion remaining.",
                lowPortion
            )
        }
        if (useFirstCount > 0) {
            reminders += Reminder(
                Kind.UseFirstItems,
                if (useFirstCount == 1) "1 item is on your Use First list."
                else "$useFirstCount items are on your Use First list.",
                useFirstCount
            )
        }
        return reminders
    }
}
