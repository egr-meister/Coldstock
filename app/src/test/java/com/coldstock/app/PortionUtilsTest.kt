package com.coldstock.app

import com.coldstock.app.util.PortionUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PortionUtilsTest {
    @Test fun incrementAndDecrement() {
        assertEquals(3, PortionUtils.increment(2))
        assertEquals(1, PortionUtils.decrement(2))
    }

    @Test fun neverNegative() {
        assertEquals(0, PortionUtils.decrement(0))
        assertEquals(0, PortionUtils.clamp(-5))
    }

    @Test fun parseBlankIsZero() {
        assertEquals(0, PortionUtils.parse(""))
        assertEquals(0, PortionUtils.parse("   "))
    }

    @Test fun parseRejectsNegativeAndInvalid() {
        assertNull(PortionUtils.parse("-1"))
        assertNull(PortionUtils.parse("abc"))
    }

    @Test fun parseClampsToMax() {
        assertEquals(PortionUtils.MAX_PORTIONS, PortionUtils.parse("99999999"))
    }

    @Test fun describeUsesLabel() {
        assertEquals("2 bags", PortionUtils.describe(2, "bags"))
        assertEquals("1 portion", PortionUtils.describe(1, ""))
        assertEquals("3 portions", PortionUtils.describe(3, ""))
    }
}
