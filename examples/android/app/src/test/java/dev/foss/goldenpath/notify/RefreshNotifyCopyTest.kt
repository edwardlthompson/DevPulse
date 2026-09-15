package dev.foss.goldenpath.notify

import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.RefreshProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RefreshNotifyCopyTest {
    @Test
    fun lookedUpCountPrefersTotal() {
        assertEquals(10, RefreshNotifyCopy.lookedUpCount(RefreshProgress(3, 10)))
        assertEquals(4, RefreshNotifyCopy.lookedUpCount(RefreshProgress(4, 0)))
    }

    @Test
    fun firstScanHintOnlyBeforeACompletedScan() {
        assertEquals(R.string.inventory_refresh_first_hint, RefreshNotifyCopy.firstScanHintRes(true))
        assertEquals(null, RefreshNotifyCopy.firstScanHintRes(false))
    }

    @Test
    fun progressPostsAreThrottled() {
        RefreshNotifyCopy.lastProgressAt = 0L
        assertTrue(RefreshNotifyCopy.allowProgress(1_000L))
        assertFalse(RefreshNotifyCopy.allowProgress(1_100L))
        assertTrue(RefreshNotifyCopy.allowProgress(1_000L + RefreshNotifyCopy.PROGRESS_MIN_MS))
    }
}
