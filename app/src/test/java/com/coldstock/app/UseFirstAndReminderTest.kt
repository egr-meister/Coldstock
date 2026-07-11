package com.coldstock.app

import com.coldstock.app.model.FrozenProduct
import com.coldstock.app.model.ReminderSettings
import com.coldstock.app.model.StorageDurationUnit
import com.coldstock.app.util.Clock
import com.coldstock.app.util.ReminderUtils
import com.coldstock.app.util.UseFirstUtils
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class UseFirstAndReminderTest {

    @Before fun setup() { Clock.override = { LocalDateTime.of(2026, 7, 10, 12, 0) } }
    @After fun teardown() { Clock.override = null }

    private val passed = FrozenProduct(id = "passed", freezingDate = "2026-06-01",
        storageDurationValue = 10, storageDurationUnit = StorageDurationUnit.Days) // 06-11 passed
    private val soon = FrozenProduct(id = "soon", freezingDate = "2026-07-01",
        storageDurationValue = 12, storageDurationUnit = StorageDurationUnit.Days) // 07-13 soon
    private val ok = FrozenProduct(id = "ok", freezingDate = "2026-07-01",
        storageDurationValue = 3, storageDurationUnit = StorageDurationUnit.Months) // 10-01 ok
    private val manual = ok.copy(id = "manual", useFirst = true)

    @Test fun orderingPassedBeforeSoon() {
        val ordered = UseFirstUtils.ordered(listOf(soon, passed, ok), 7, "f1".let { null })
        assertEquals("passed", ordered.first().product.id)
        assertEquals(UseFirstUtils.Section.ReviewDatePassed, ordered.first().section)
    }

    @Test fun okNotInUseFirstUnlessManual() {
        val ordered = UseFirstUtils.ordered(listOf(ok), 7)
        assertTrue(ordered.isEmpty())
        val withManual = UseFirstUtils.ordered(listOf(manual), 7)
        assertEquals(1, withManual.size)
        assertEquals(UseFirstUtils.Section.ManuallyPrioritized, withManual.first().section)
    }

    @Test fun remindersCountPassedAndSoon() {
        val reminders = ReminderUtils.evaluate(listOf(passed, soon, ok), ReminderSettings(), 7)
        val passedReminder = reminders.firstOrNull { it.kind == ReminderUtils.Kind.ReviewDatePassed }
        val soonReminder = reminders.firstOrNull { it.kind == ReminderUtils.Kind.UseSoon }
        assertEquals(1, passedReminder?.count)
        assertEquals(1, soonReminder?.count)
    }

    @Test fun remindersDisabledYieldsNone() {
        val reminders = ReminderUtils.evaluate(
            listOf(passed), ReminderSettings(enabled = false), 7
        )
        assertTrue(reminders.isEmpty())
    }

    @Test fun lowPortionReminder() {
        val onePortion = ok.copy(id = "low", portionCount = 1)
        val reminders = ReminderUtils.evaluate(listOf(onePortion), ReminderSettings(), 7)
        assertTrue(reminders.any { it.kind == ReminderUtils.Kind.LowPortions })
    }
}
