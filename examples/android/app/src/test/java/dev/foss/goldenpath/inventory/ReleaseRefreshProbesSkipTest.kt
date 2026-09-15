package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.forge.GitHubSearchClient
import dev.foss.goldenpath.index.forge.GitHubSearchPage
import dev.foss.goldenpath.index.forge.GitHubSearchPace
import dev.foss.goldenpath.index.forge.LeftoverSearchClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ReleaseRefreshProbesSkipTest {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
        GitHubSearchPace.reset()
        RefreshSkip.reset()
    }

    @Test
    fun storeListedSkipsGithubSearch() {
        val fetches = AtomicInteger(0)
        RemoteReleaseMemory.putAll(
            mapOf(
                "com.play.app" to RemoteReleaseRollup.from(
                    listOf(RemoteReleaseOffer(RemoteReleasedSource.Play, 1L, "1.0")),
                ),
            ),
        )
        val offer = ReleaseRefreshProbes.github(
            "com.play.app",
            "Play App",
            GitHubSearchClient { fetches.incrementAndGet(); GitHubSearchPage(200, """{"items":[]}""") },
            searchUnknowns = true,
            leftover = LeftoverSearchClient { _, _ -> fetches.incrementAndGet(); GitHubSearchPage(200, "[]") },
        )
        assertEquals(0, fetches.get())
        assertFalse(offer.listed)
        assertFalse(offer.known)
    }

    @Test
    fun playMissSearchesGithub() {
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
            leftover = LeftoverSearchClient { _, _ -> fetches.incrementAndGet(); GitHubSearchPage(200, "[]") },
            searchUnknowns = true,
        )
        assertEquals(1, fetches.get())
        assertFalse(offer.listed)
        assertTrue(offer.known)
    }

    @Test
    fun oldNeverMissDoesNotBlockLeftoverSearch() {
        val fetches = AtomicInteger(0)
        val now = 1_720_000_000_000L
        RemoteReleaseMemory.putAll(
            mapOf(
                "org.sideload.app" to RemoteReleaseRollup.from(
                    listOf(
                        RemoteReleaseOffer(
                            RemoteReleasedSource.Forge,
                            listed = false,
                            known = true,
                            miss = ListingMiss.Never,
                            fetchedAtMs = now,
                        ),
                    ),
                ),
            ),
        )
        ReleaseRefreshProbes.github(
            "org.sideload.app",
            "Sideload",
            GitHubSearchClient { fetches.incrementAndGet(); GitHubSearchPage(200, """{"items":[]}""") },
            nowMs = now + 1_000L,
            searchUnknowns = true,
        )
        assertEquals(1, fetches.get())
    }

    @Test
    fun searchedMissIsCached() {
        val now = 1_720_000_000_000L
        val fetches = AtomicInteger(0)
        RemoteReleaseMemory.putAll(
            mapOf(
                "app.x" to RemoteReleaseRollup.from(
                    listOf(
                        RemoteReleaseOffer(
                            RemoteReleasedSource.Forge,
                            listed = false,
                            known = true,
                            miss = ListingMiss.Searched,
                            fetchedAtMs = now,
                        ),
                    ),
                ),
            ),
        )
        val offer = ReleaseRefreshProbes.github(
            "app.x",
            "X",
            GitHubSearchClient { fetches.incrementAndGet(); GitHubSearchPage(200, """{"items":[]}""") },
            nowMs = now + 1_000L,
        )
        assertEquals(0, fetches.get())
        assertTrue(offer.known)
        assertFalse(offer.listed)
    }
}
