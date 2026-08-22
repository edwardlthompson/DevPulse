package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.forge.GitHubSearchClient
import dev.foss.goldenpath.index.forge.GitHubSearchPage
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
        assertTrue(offer.known)
    }

    @Test
    fun playMissSkipsGithubSearch() {
        val fetches = AtomicInteger(0)
        RemoteReleaseMemory.putAll(
            mapOf(
                "com.instagram.android" to RemoteReleaseRollup.from(
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
            "com.instagram.android",
            "Instagram",
            GitHubSearchClient { fetches.incrementAndGet(); GitHubSearchPage(200, """{"items":[]}""") },
            leftover = LeftoverSearchClient { _, _ -> fetches.incrementAndGet(); GitHubSearchPage(200, "[]") },
        )
        assertEquals(0, fetches.get())
        assertFalse(offer.listed)
        assertTrue(offer.known)
    }
}
