package dev.foss.goldenpath.alternatives

import dev.foss.goldenpath.index.fdroid.FdroidPackageMeta
import dev.foss.goldenpath.inventory.sampleApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlternativesFromCacheTest {
    @Test
    fun emptyIfNoMaintainedNeighbor() {
        val now = 2_000_000_000_000L
        val stale = sampleApp("org.old.maps", "Old Maps", remoteReleasedAtMs = now - 400L * 86_400_000L)
        val older = sampleApp("org.other.maps", "Other Maps", remoteReleasedAtMs = now - 400L * 86_400_000L)
        val meta = mapOf(
            "org.old.maps" to FdroidPackageMeta("Maps", listOf("org.other.maps")),
            "org.other.maps" to FdroidPackageMeta("Maps"),
        )
        assertTrue(AlternativesFromCache.hits(stale, listOf(stale, older), meta, now).isEmpty())
    }

    @Test
    fun ranksMaintainedSameCategory() {
        val now = 2_000_000_000_000L
        val stale = sampleApp("org.old.maps", "Old Maps", remoteReleasedAtMs = now - 400L * 86_400_000L)
        val fresh = sampleApp("org.fresh.maps", "Fresh Maps", remoteReleasedAtMs = now - 10L * 86_400_000L)
        val meta = mapOf(
            "org.old.maps" to FdroidPackageMeta("Maps"),
            "org.fresh.maps" to FdroidPackageMeta("Maps"),
        )
        val hits = AlternativesFromCache.hits(stale, listOf(stale, fresh), meta, now)
        assertEquals("org.fresh.maps", hits.single().packageName)
    }
}
