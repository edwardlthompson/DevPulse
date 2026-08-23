package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.R
import org.junit.Assert.assertEquals
import org.junit.Test

class OutletLabelsTest {
    @Test
    fun mapsKnownIdsAndFdroidFallback() {
        assertEquals(R.string.inventory_source_play, OutletLabels.nameRes(RefreshOutletIds.PLAY))
        assertEquals(R.string.inventory_source_leftover, OutletLabels.nameRes(RefreshOutletIds.LEFTOVER))
        assertEquals(R.string.inventory_source_fdroid, OutletLabels.nameRes(RefreshOutletIds.fdroid("official")))
        assertEquals("official", OutletLabels.fallback(RefreshOutletIds.fdroid("official")))
        assertEquals(null, OutletLabels.nameRes("other"))
        assertEquals("other", OutletLabels.fallback("other"))
    }
}
