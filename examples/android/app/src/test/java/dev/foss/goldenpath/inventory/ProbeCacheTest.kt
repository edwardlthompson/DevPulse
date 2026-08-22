package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ProbeCacheTest {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
    }

    @Test
    fun freshKnownOfferWithinTtl() {
        val offer = RemoteReleaseOffer(
            RemoteReleasedSource.Play,
            listed = false,
            known = true,
            fetchedAtMs = 1_000L,
        )
        RemoteReleaseMemory.putAll(mapOf("app.x" to RemoteReleaseRollup.from(listOf(offer))))
        assertEquals(offer, ProbeCache.fresh("app.x", RemoteReleasedSource.Play, 2_000L, 10_000L))
        assertNull(ProbeCache.fresh("app.x", RemoteReleasedSource.Play, 20_000L, 10_000L))
    }

    @Test
    fun putAllKeepsOtherSources() {
        val play = RemoteReleaseOffer(RemoteReleasedSource.Play, listed = true, fetchedAtMs = 1_000L)
        val fdroid = RemoteReleaseOffer(RemoteReleasedSource.Fdroid, 1L, "1.0", fetchedAtMs = 2_000L)
        RemoteReleaseMemory.putAll(mapOf("app.x" to RemoteReleaseRollup.from(listOf(play))))
        RemoteReleaseMemory.putAll(mapOf("app.x" to RemoteReleaseRollup.from(listOf(fdroid))))
        val sources = RemoteReleaseMemory.byPackage.getValue("app.x").offers.map { it.source }.toSet()
        assertEquals(setOf(RemoteReleasedSource.Play, RemoteReleasedSource.Fdroid), sources)
        assertEquals(play, ProbeCache.fresh("app.x", RemoteReleasedSource.Play, 2_000L, 10_000L))
    }

    @Test
    fun knownMissUsesLongerTtl() {
        val offer = RemoteReleaseOffer(
            RemoteReleasedSource.Forge,
            listed = false,
            known = true,
            fetchedAtMs = 1_000L,
        )
        RemoteReleaseMemory.putAll(mapOf("app.x" to RemoteReleaseRollup.from(listOf(offer))))
        assertEquals(offer, ProbeCache.fresh("app.x", RemoteReleasedSource.Forge, 2_000L, 10L, missTtlMs = 10_000L))
        assertNull(ProbeCache.fresh("app.x", RemoteReleasedSource.Forge, 20_000L, 10L, missTtlMs = 10_000L))
    }

    @Test
    fun unknownOrMissingFetchedAtIsNotFresh() {
        RemoteReleaseMemory.putAll(
            mapOf(
                "app.x" to RemoteReleaseRollup.from(
                    listOf(RemoteReleaseOffer(RemoteReleasedSource.Play, listed = false, known = false, fetchedAtMs = 1L)),
                ),
            ),
        )
        assertNull(ProbeCache.fresh("app.x", RemoteReleasedSource.Play, 2L, 10_000L))
    }
}
