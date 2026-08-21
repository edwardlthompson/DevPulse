package dev.foss.goldenpath.opportunity

import dev.foss.goldenpath.inventory.AppOrigin
import dev.foss.goldenpath.inventory.sampleApp
import org.junit.Assert.assertEquals
import org.junit.Test

class OpportunityFromAppsTest {
    @Test
    fun groupsQuietByOriginAndHidesPins() {
        val now = 1_800_000_000_000L
        val stale = sampleApp("org.old.app", "Old", installedAtMs = now - 400L * 86_400_000L).copy(origin = AppOrigin.Fdroid)
        val pinned = sampleApp("org.pin.app", "Pin", installedAtMs = now - 400L * 86_400_000L).copy(origin = AppOrigin.Play)
        val gaps = OpportunityFromApps.gaps(listOf(stale, pinned), now, includePinned = false, pins = setOf("org.pin.app"))
        assertEquals(1, gaps.size)
        assertEquals("F-Droid", gaps.single().category)
        assertEquals(1, gaps.single().quietCount)
    }

    @Test
    fun storedCategoryBeatsOrigin() {
        val now = 1_800_000_000_000L
        val stale = sampleApp("org.old.app", "Old", installedAtMs = now - 400L * 86_400_000L).copy(origin = AppOrigin.Play)
        val gaps = OpportunityFromApps.gaps(
            listOf(stale),
            now,
            includePinned = false,
            pins = emptySet(),
            categories = mapOf("org.old.app" to "Navigation"),
        )
        assertEquals("Navigation", gaps.single().category)
    }
}
