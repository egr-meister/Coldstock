package com.coldstock.app.util

import com.coldstock.app.model.FrozenProduct
import com.coldstock.app.model.FrozenProductStatus
import com.coldstock.app.model.ProductLifecycleState
import java.time.LocalDate

/**
 * Derives a product's status from its lifecycle state and the user-entered
 * storage timeline. Purely organizational; never a food-safety judgement.
 */
object StatusUtils {

    /**
     * Status calculation order:
     * 1. Used            -> Used
     * 2. Discarded       -> Discarded
     * 3. no review date  -> NoReviewDate
     * 4. review < today  -> Old (label: "Review Date Passed")
     * 5. review <= today+threshold -> UseSoon
     * 6. otherwise       -> Ok
     */
    fun statusOf(
        product: FrozenProduct,
        soonThresholdDays: Int,
        today: LocalDate = Clock.today()
    ): FrozenProductStatus {
        when (product.lifecycleState) {
            ProductLifecycleState.Used -> return FrozenProductStatus.Used
            ProductLifecycleState.Discarded -> return FrozenProductStatus.Discarded
            ProductLifecycleState.Active -> Unit
        }

        val reviewDate = DateUtils.plannedReviewDate(
            product.freezingDate, product.storageDurationValue, product.storageDurationUnit
        ) ?: return FrozenProductStatus.NoReviewDate

        val daysUntil = DateUtils.daysUntil(reviewDate, today)
            ?: return FrozenProductStatus.NoReviewDate

        return when {
            daysUntil < 0 -> FrozenProductStatus.Old
            daysUntil <= soonThresholdDays -> FrozenProductStatus.UseSoon
            else -> FrozenProductStatus.Ok
        }
    }

    val soonThresholdOptions: List<Int> = listOf(1, 3, 7, 14, 30)
}
