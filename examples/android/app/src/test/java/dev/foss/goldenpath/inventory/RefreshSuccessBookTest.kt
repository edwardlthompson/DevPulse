package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RefreshSuccessBookTest {
    @Before
    fun reset() {
        RefreshSuccessBook.clear()
    }

    @Test
    fun captureKeepsLatestFinishedOutlet() {
        RefreshSuccessBook.hydrate(mapOf(RefreshOutletIds.PLAY to 10L))
        RefreshSuccessBook.capture(
            listOf(
                RefreshOutletSnap(
                    id = RefreshOutletIds.PLAY,
                    title = "Play",
                    done = 1,
                    total = 1,
                    current = "",
                    skipped = false,
                    etaMs = 0L,
                    finishedAtMs = 20L,
                ),
            ),
        )
        assertEquals(20L, RefreshSuccessBook.snapshot()[RefreshOutletIds.PLAY])
    }
}
