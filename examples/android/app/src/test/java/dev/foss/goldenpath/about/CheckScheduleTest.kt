package dev.foss.goldenpath.about

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckScheduleTest {
    @Test
    fun offIntervalNeverChecks() {
        assertFalse(CheckSchedule.shouldCheck("off", null, 1_000L))
    }

    @Test
    fun weeklyRequiresSevenDays() {
        val now = 10_000_000L
        assertTrue(CheckSchedule.shouldCheck("weekly", now - 8 * 86_400_000L, now))
        assertFalse(CheckSchedule.shouldCheck("weekly", now - 86_400_000L, now))
    }

    @Test
    fun onSessionChecksOnce() {
        assertTrue(CheckSchedule.shouldCheck("on_session", null, 1L))
        assertFalse(CheckSchedule.shouldCheck("on_session", 1L, 2L))
    }
}
