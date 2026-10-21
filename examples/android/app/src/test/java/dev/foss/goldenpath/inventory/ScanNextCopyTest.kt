package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Test

class ScanNextCopyTest {
    @Test
    fun onDemandHidden() {
        assertEquals(
            ScanNextCopy.Kind.Hidden,
            ScanNextCopy.kind(ScanInterval.OnDemand, null, 1L),
        )
    }

    @Test
    fun dueOrNeverScannedIsSoon() {
        val now = 1_700_000_000_000L
        assertEquals(ScanNextCopy.Kind.Soon, ScanNextCopy.kind(ScanInterval.Daily, null, now))
        assertEquals(
            ScanNextCopy.Kind.Soon,
            ScanNextCopy.kind(ScanInterval.Daily, now - 2 * 86_400_000L, now),
        )
    }

    @Test
    fun remainingDelayUsesHoursOrDays() {
        val now = 1_700_000_000_000L
        val day = 86_400_000L
        assertEquals(
            ScanNextCopy.Kind.Hours(12),
            ScanNextCopy.kind(ScanInterval.Daily, now - day / 2, now),
        )
        assertEquals(
            ScanNextCopy.Kind.Days(6),
            ScanNextCopy.kind(ScanInterval.Weekly, now - day, now),
        )
    }
}
