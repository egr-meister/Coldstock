package com.coldstock.app

import com.coldstock.app.model.StorageDurationUnit
import com.coldstock.app.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {

    @Test fun parsesValidDate() {
        assertEquals(LocalDate.of(2026, 7, 10), DateUtils.parseDate("2026-07-10"))
    }

    @Test fun rejectsInvalidDate() {
        assertNull(DateUtils.parseDate("not-a-date"))
        assertNull(DateUtils.parseDate(""))
        assertNull(DateUtils.parseDate(null))
        assertFalse(DateUtils.isValidDate("2026-13-40"))
    }

    @Test fun reviewDatePlusDays() {
        val r = DateUtils.plannedReviewDate("2026-01-01", 30, StorageDurationUnit.Days)
        assertEquals(LocalDate.of(2026, 1, 31), r)
    }

    @Test fun reviewDatePlusWeeks() {
        val r = DateUtils.plannedReviewDate("2026-01-01", 8, StorageDurationUnit.Weeks)
        assertEquals(LocalDate.of(2026, 2, 26), r)
    }

    @Test fun reviewDatePlusMonths() {
        val r = DateUtils.plannedReviewDate("2026-01-15", 3, StorageDurationUnit.Months)
        assertEquals(LocalDate.of(2026, 4, 15), r)
    }

    @Test fun monthsRespectCalendarLength() {
        // Jan 31 + 1 month => Feb 28 (2026 not leap) rather than fixed 30 days.
        val r = DateUtils.plannedReviewDate("2026-01-31", 1, StorageDurationUnit.Months)
        assertEquals(LocalDate.of(2026, 2, 28), r)
    }

    @Test fun missingDurationReturnsNull() {
        assertNull(DateUtils.plannedReviewDate("2026-01-01", null, StorageDurationUnit.Days))
        assertNull(DateUtils.plannedReviewDate("2026-01-01", 5, null))
    }

    @Test fun zeroOrNegativeDurationInvalid() {
        assertFalse(DateUtils.isDurationValid(0, StorageDurationUnit.Days))
        assertFalse(DateUtils.isDurationValid(-3, StorageDurationUnit.Weeks))
        assertNull(DateUtils.plannedReviewDate("2026-01-01", 0, StorageDurationUnit.Days))
    }

    @Test fun durationOverMaximumInvalid() {
        assertFalse(DateUtils.isDurationValid(4000, StorageDurationUnit.Days))
        assertTrue(DateUtils.isDurationValid(3650, StorageDurationUnit.Days))
    }

    @Test fun invalidFreezingDateReturnsNull() {
        assertNull(DateUtils.plannedReviewDate("bad", 5, StorageDurationUnit.Days))
    }
}
