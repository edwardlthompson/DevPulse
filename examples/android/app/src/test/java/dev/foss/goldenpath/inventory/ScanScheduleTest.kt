package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
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
    fun periodicIntervalsHonorElapsedTime() {
        val now = 1_700_000_000_000L
        val day = 86_400_000L
        assertTrue(ScanSchedule.due(ScanInterval.Daily, null, now))
        assertFalse(ScanSchedule.due(ScanInterval.Daily, now - day / 2, now))
        assertTrue(ScanSchedule.due(ScanInterval.Daily, now - 2 * day, now))
        assertTrue(ScanSchedule.due(ScanInterval.Weekly, null, now))
        assertFalse(ScanSchedule.due(ScanInterval.Weekly, now - day, now))
        assertTrue(ScanSchedule.due(ScanInterval.Weekly, now - 8 * day, now))
        assertFalse(ScanSchedule.due(ScanInterval.Monthly, now - 10 * day, now))
        assertTrue(ScanSchedule.due(ScanInterval.Monthly, now - 31 * day, now))
    }

    @Test
    fun nextDelayWaitsFullPeriodUntilDue() {
        val now = 1_700_000_000_000L
        val day = 86_400_000L
        assertEquals(0L, ScanSchedule.nextDelayMs(ScanInterval.OnDemand, null, now))
        assertEquals(day, ScanSchedule.nextDelayMs(ScanInterval.Daily, null, now))
        assertEquals(day / 2, ScanSchedule.nextDelayMs(ScanInterval.Daily, now - day / 2, now))
        assertEquals(0L, ScanSchedule.nextDelayMs(ScanInterval.Daily, now - 2 * day, now))
        assertEquals(7 * day, ScanSchedule.nextDelayMs(ScanInterval.Weekly, null, now))
        assertEquals(30 * day, ScanSchedule.nextDelayMs(ScanInterval.Monthly, null, now))
    }
}
