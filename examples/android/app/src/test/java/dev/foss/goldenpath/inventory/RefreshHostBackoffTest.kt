package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RefreshHostBackoffTest {
    @Before
    fun reset() {
        RefreshHostBackoff.clear()
    }

    @Test
    fun activeDropsExpiredHosts() {
        RefreshHostBackoff.note("github", delayMs = 5_000L, nowMs = 1_000L)
        assertEquals(4_000L, RefreshHostBackoff.active(nowMs = 2_000L)["github"])
        assertTrue(RefreshHostBackoff.active(nowMs = 8_000L).isEmpty())
    }
}
