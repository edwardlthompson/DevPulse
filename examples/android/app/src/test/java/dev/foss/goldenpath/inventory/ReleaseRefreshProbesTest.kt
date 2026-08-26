package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.aptoide.AptoideMetaFetcher
import dev.foss.goldenpath.index.forge.ForgeHost
import dev.foss.goldenpath.index.forge.GitHubSearchClient
import dev.foss.goldenpath.index.forge.GitHubSearchPage
import dev.foss.goldenpath.index.forge.LeftoverHint
import dev.foss.goldenpath.index.forge.LeftoverSearchClient
import dev.foss.goldenpath.index.play.PlayPageClient
import dev.foss.goldenpath.index.play.PlayPageResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ReleaseRefreshProbesTest {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
    }

    @Test
    fun playSkipsHttpWhenFresh() {
        val fetches = AtomicInteger(0)
        val now = 1_720_000_000_000L
        RemoteReleaseMemory.putAll(
            mapOf(
                "app.x" to RemoteReleaseRollup.from(
                    listOf(
                        RemoteReleaseOffer(
                            RemoteReleasedSource.Play,
                            1L,
                            "1.0",
                            listed = true,
                            fetchedAtMs = now,
                        ),
                    ),
                ),
            ),
        )
        val offer = ReleaseRefreshProbes.play(
            "app.x",
            PlayPageClient {
                fetches.incrementAndGet()
                PlayPageResponse(200, "")
            },
            nowMs = now + 1_000L,
        )
        assertEquals(0, fetches.get())
        assertEquals("1.0", offer.versionName)
    }

    @Test
    fun aptoideReusesBatchMiss() {
        val fetches = AtomicInteger(0)
        val now = 1_720_000_000_000L
        RemoteReleaseMemory.putAll(
            mapOf(
                "app.x" to RemoteReleaseRollup.from(
                    listOf(
                        RemoteReleaseOffer(
                            RemoteReleasedSource.Aptoide,
                            listed = false,
                            known = true,
                            fetchedAtMs = now,
                        ),
                    ),
                ),
            ),
        )
        val offer = ReleaseRefreshProbes.aptoide(
            "app.x",
            AptoideMetaFetcher { fetches.incrementAndGet(); Result.success("") },
            nowMs = now + 1_000L,
        )
        assertEquals(0, fetches.get())
        assertFalse(offer.listed)
        assertEquals(true, offer.known)
    }

    @Test
    fun aptoideMissStaysUnknown() {
        val offer = ReleaseRefreshProbes.aptoide(
            "app.x",
            AptoideMetaFetcher { Result.success("") },
            nowMs = 1L,
        )
        assertFalse(offer.listed)
        assertFalse(offer.known)
    }

    @Test
    fun githubReusesKnownMiss() {
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
                            fetchedAtMs = now,
                        ),
                    ),
                ),
            ),
        )
        val leftover = LeftoverSearchClient { _, _ ->
            fetches.incrementAndGet()
            GitHubSearchPage(200, "[]")
        }
        val offer = ReleaseRefreshProbes.github(
            "app.x",
            "X",
            GitHubSearchClient { fetches.incrementAndGet(); GitHubSearchPage(200, """{"items":[]}""") },
            searchUnknowns = true,
            leftover = leftover,
            nowMs = now + 1_000L,
        )
        assertEquals(0, fetches.get())
        assertEquals(false, offer.listed)
        assertEquals(true, offer.known)
    }

    @Test
    fun leftoverHintSkipsSearch() {
        val fetches = AtomicInteger(0)
        val leftover = LeftoverSearchClient { _, _ ->
            fetches.incrementAndGet()
            GitHubSearchPage(200, "[]")
        }
        val offer = ReleaseRefreshProbes.github(
            "org.gitlab.app",
            "App",
            GitHubSearchClient { fetches.incrementAndGet(); GitHubSearchPage(200, """{"items":[]}""") },
            leftover = leftover,
            leftoverHint = LeftoverHint(ForgeHost.GitLab, "acme/app", 9L, "1.0"),
        )
        assertEquals(0, fetches.get())
        assertEquals(true, offer.listed)
        assertEquals("https://gitlab.com/acme/app/-/releases", offer.pageUrl)
        assertEquals(9L, offer.ms)
    }
}
