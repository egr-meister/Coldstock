package com.coldstock.app

import com.coldstock.app.model.FrozenProduct
import com.coldstock.app.model.FrozenProductStatus
import com.coldstock.app.model.ProductLifecycleState
import com.coldstock.app.model.StorageDurationUnit
import com.coldstock.app.util.Clock
import com.coldstock.app.util.StatusUtils
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class StatusUtilsTest {

    private val today = LocalDate.of(2026, 7, 10)

    @Before fun setup() {
        Clock.override = { LocalDateTime.of(2026, 7, 10, 12, 0) }
    }

    @After fun teardown() { Clock.override = null }

    private fun product(
        freezing: String = "2026-07-01",
        value: Int? = null,
        unit: StorageDurationUnit? = null,
        state: ProductLifecycleState = ProductLifecycleState.Active
    ) = FrozenProduct(
        id = "1", freezingDate = freezing,
        storageDurationValue = value, storageDurationUnit = unit, lifecycleState = state
    )

    @Test fun usedTakesPriority() {
        assertEquals(FrozenProductStatus.Used,
            StatusUtils.statusOf(product(state = ProductLifecycleState.Used), 7, today))
    }

    @Test fun discardedTakesPriority() {
        assertEquals(FrozenProductStatus.Discarded,
            StatusUtils.statusOf(product(state = ProductLifecycleState.Discarded), 7, today))
    }

    @Test fun noReviewDate() {
        assertEquals(FrozenProductStatus.NoReviewDate,
            StatusUtils.statusOf(product(), 7, today))
    }

    @Test fun okWhenFarInFuture() {
        val p = product("2026-07-01", 3, StorageDurationUnit.Months) // review 2026-10-01
        assertEquals(FrozenProductStatus.Ok, StatusUtils.statusOf(p, 7, today))
    }

    @Test fun useSoonWithinThreshold() {
        val p = product("2026-07-01", 12, StorageDurationUnit.Days) // review 2026-07-13
        assertEquals(FrozenProductStatus.UseSoon, StatusUtils.statusOf(p, 7, today))
    }

    @Test fun oldWhenReviewPassed() {
        val p = product("2026-06-01", 10, StorageDurationUnit.Days) // review 2026-06-11
        assertEquals(FrozenProductStatus.Old, StatusUtils.statusOf(p, 7, today))
    }

    @Test fun thresholdChangeAffectsUseSoon() {
        val p = product("2026-07-01", 20, StorageDurationUnit.Days) // review 2026-07-21 (11 days out)
        assertEquals(FrozenProductStatus.Ok, StatusUtils.statusOf(p, 7, today))
        assertEquals(FrozenProductStatus.UseSoon, StatusUtils.statusOf(p, 14, today))
    }

    @Test fun labelsAreNeutral() {
        assertEquals("Review Date Passed", FrozenProductStatus.Old.label)
        assertEquals("OK", FrozenProductStatus.Ok.label)
        assertEquals("Use Soon", FrozenProductStatus.UseSoon.label)
    }
}
