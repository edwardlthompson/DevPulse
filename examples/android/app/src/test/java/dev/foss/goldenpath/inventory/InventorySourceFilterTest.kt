package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InventorySourceFilterTest {
    @Test
    fun decodeBlankIsEmptyUnlessLegacyGithub() {
        assertTrue(InventorySourceFilter.decode(null).isEmpty())
        assertTrue(InventorySourceFilter.decode("").isEmpty())
        assertEquals(setOf(RemoteReleasedSource.Forge), InventorySourceFilter.decode(null, legacyGithub = true))
    }

    @Test
    fun encodeThenDecodeRoundTrip() {
        val sources = setOf(RemoteReleasedSource.Guardian, RemoteReleasedSource.Izzy, RemoteReleasedSource.Archive)
        assertEquals(sources, InventorySourceFilter.decode(InventorySourceFilter.encode(sources)))
    }

    @Test
    fun toggleAddsAndRemoves() {
        val added = InventorySourceFilter.toggle(emptySet(), RemoteReleasedSource.Calyx)
        assertEquals(setOf(RemoteReleasedSource.Calyx), added)
        assertTrue(InventorySourceFilter.toggle(added, RemoteReleasedSource.Calyx).isEmpty())
    }
}
