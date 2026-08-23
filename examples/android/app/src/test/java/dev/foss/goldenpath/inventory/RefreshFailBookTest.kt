package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RefreshFailBookTest {
    @Before
    fun reset() {
        RefreshFailBook.clear()
    }

    @Test
    fun captureKeepsSkippedOutlets() {
        RefreshFailBook.capture(
            listOf(
                RefreshOutletSnap(
                    id = RefreshOutletIds.GITHUB,
                    title = "GitHub",
                    done = 1,
                    total = 1,
                    current = "",
                    skipped = true,
                    etaMs = 0L,
                    finishedAtMs = 30L,
                ),
                RefreshOutletSnap(
                    id = RefreshOutletIds.PLAY,
                    title = "Play",
                    done = 1,
                    total = 1,
                    current = "",
                    skipped = false,
                    etaMs = 0L,
                    finishedAtMs = 40L,
                ),
            ),
        )
        assertEquals(30L, RefreshFailBook.snapshot()[RefreshOutletIds.GITHUB])
        assertTrue(RefreshFailBook.snapshot()[RefreshOutletIds.PLAY] == null)
    }
}
