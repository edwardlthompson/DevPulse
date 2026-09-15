package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.forge.GitHubSearchClient
import dev.foss.goldenpath.index.forge.GitHubSearchPage
import dev.foss.goldenpath.index.forge.GitHubSearchPace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ReleaseRefreshProbesUnknownsTest {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
        GitHubSearchPace.reset()
        RefreshSkip.reset()
    }

    @Test
    fun playMissDoesNotSearchWhenUnknownsOff() {
        val fetches = AtomicInteger(0)
        RemoteReleaseMemory.putAll(
            mapOf(
                "org.continuumcalendar.app" to RemoteReleaseRollup.from(
                    listOf(
                        RemoteReleaseOffer(
                            RemoteReleasedSource.Play,
                            listed = false,
                            known = true,
                        ),
                    ),
                ),
            ),
        )
        val offer = ReleaseRefreshProbes.github(
            "org.continuumcalendar.app",
            "Continuum Calendar",
            GitHubSearchClient { fetches.incrementAndGet(); GitHubSearchPage(200, """{"items":[]}""") },
        )
        assertEquals(0, fetches.get())
        assertFalse(offer.listed)
        assertFalse(offer.known)
    }
}
