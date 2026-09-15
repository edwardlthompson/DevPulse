package dev.foss.goldenpath.inventory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateAllSnapTest {
    @Test
    fun byteSnapsSkipMidProgressWithinWindow() {
        assertTrue(UpdateAll.snapBytes(0, 100, 1L))
        assertTrue(UpdateAll.snapBytes(100, 100, 1L))
        assertTrue(UpdateAll.snapBytes(50, 100, 10_000L))
        assertFalse(UpdateAll.snapBytes(60, 100, 10_100L))
        assertTrue(UpdateAll.snapBytes(70, 100, 10_000L + UpdateAll.SNAP_MS))
    }
}
