package dev.foss.goldenpath.ui.refresh

import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayRefreshTest {
    @Test
    fun pickMaxHzAtCurrentResolution() {
        val current = RefreshMode(1, 1080, 2400, 60f)
        val picked = DisplayRefresh.pick(
            listOf(
                current,
                RefreshMode(2, 1080, 2400, 120f),
                RefreshMode(3, 1440, 3200, 90f),
            ),
            current,
        )
        assertEquals(2, picked.modeId)
        assertEquals(120f, picked.refreshHz, 0.01f)
    }

    @Test
    fun pickFallsBackWhenResolutionHasNoModes() {
        val current = RefreshMode(9, 800, 600, 60f)
        val picked = DisplayRefresh.pick(
            listOf(RefreshMode(1, 1080, 2400, 144f)),
            current,
        )
        assertEquals(144f, picked.refreshHz, 0.01f)
    }

    @Test
    fun pickKeepsCurrentWhenModesEmpty() {
        val current = RefreshMode(1, 1080, 2400, 90f)
        assertEquals(current, DisplayRefresh.pick(emptyList(), current))
    }
}
