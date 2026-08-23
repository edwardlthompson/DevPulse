package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AppReprobeTest {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
    }

    @Test
    fun forgetFetchedClearsTtlThenApplyMergesLiveOffer() {
        val stale = RemoteReleaseOffer(
            RemoteReleasedSource.Play,
            listed = false,
            known = true,
            fetchedAtMs = 1_000L,
        )
        RemoteReleaseMemory.putAll(mapOf("app.one" to RemoteReleaseRollup.from(listOf(stale))))
        assertEquals(stale, ProbeCache.fresh("app.one", RemoteReleasedSource.Play, 2_000L, 10_000L))
        AppReprobe.forgetFetched("app.one")
        assertNull(ProbeCache.fresh("app.one", RemoteReleasedSource.Play, 2_000L, 10_000L))
        val live = RemoteReleaseOffer(
            RemoteReleasedSource.Play,
            ms = 9L,
            versionName = "2.0",
            listed = true,
            fetchedAtMs = 3_000L,
        )
        val pick = AppReprobe.apply("app.one", listOf(live))
        assertEquals("2.0", pick.versionName)
        assertEquals(live, ProbeCache.fresh("app.one", RemoteReleasedSource.Play, 4_000L, 10_000L))
    }
}
