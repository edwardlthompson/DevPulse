package dev.foss.goldenpath.inventory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanScheduleTest {
    @Test
    fun onDemandNeverAutoStarts() {
        assertFalse(ScanSchedule.due(ScanInterval.OnDemand, null, 1_000L))
        assertFalse(ScanSchedule.due(ScanInterval.OnDemand, 1L, 1_000L))
    }

    @Test
    fun weeklyAndMonthlyHonorElapsedTime() {
        val now = 1_700_000_000_000L
        val day = 86_400_000L
        assertTrue(ScanSchedule.due(ScanInterval.Weekly, null, now))
        assertFalse(ScanSchedule.due(ScanInterval.Weekly, now - day, now))
        assertTrue(ScanSchedule.due(ScanInterval.Weekly, now - 8 * day, now))
        assertFalse(ScanSchedule.due(ScanInterval.Monthly, now - 10 * day, now))
        assertTrue(ScanSchedule.due(ScanInterval.Monthly, now - 31 * day, now))
    }
}
