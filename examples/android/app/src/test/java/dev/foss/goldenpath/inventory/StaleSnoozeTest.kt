package dev.foss.goldenpath.inventory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StaleSnoozeTest {
    @Test
    fun hidesUntilDeadline() {
        assertTrue(StaleSnooze.hidden(10L, nowMs = 9L))
        assertFalse(StaleSnooze.hidden(10L, nowMs = 10L))
        assertFalse(StaleSnooze.hidden(null, nowMs = 1L))
    }
}
