package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Test

class RefreshLocationsTest {
    @Test
    fun countsEveryRepoAndProbeLocation() {
        assertEquals(0, RefreshLocations.probesPerApp(play = false, aptoide = false, forge = false))
        assertEquals(3, RefreshLocations.probesPerApp(play = true, aptoide = true, forge = true))
        assertEquals(5 + 389 * 3, RefreshLocations.total(5, 389, play = true, aptoide = true, forge = true))
        assertEquals(1, RefreshLocations.total(1, 2, play = false, aptoide = false, forge = false))
        assertEquals(0, RefreshLocations.storeProbes(0, play = true, aptoide = true))
        assertEquals(20, RefreshLocations.storeProbes(10, play = true, aptoide = true))
        assertEquals(10, RefreshLocations.forgeProbes(10, forge = true))
        assertEquals(0, RefreshLocations.forgeProbes(10, forge = false))
    }

    @Test
    fun labelsSourceAndPackage() {
        assertEquals("F-Droid · official", RefreshLocations.label("F-Droid", "official"))
        assertEquals("GitHub · NewPipe (org.schabi.newpipe)", RefreshLocations.label("GitHub", "NewPipe", "org.schabi.newpipe"))
    }
}
