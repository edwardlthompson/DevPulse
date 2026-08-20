package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.aptoide.AptoideMetaFetcher
import dev.foss.goldenpath.index.fdroid.FdroidAppRecord
import dev.foss.goldenpath.index.fdroid.FdroidIndexFetcher
import dev.foss.goldenpath.index.fdroid.FdroidRepo
import dev.foss.goldenpath.index.fdroid.FdroidRepoKind
import dev.foss.goldenpath.index.forge.GitHubSearchClient
import dev.foss.goldenpath.index.forge.GitHubSearchPage
import dev.foss.goldenpath.index.play.PlayPageClient
import dev.foss.goldenpath.index.play.PlayPageResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ReleaseRefreshTest {
    private fun indexJson(json: String) = Result.success(json.toByteArray())

    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
        RefreshTrace.emit = {}
    }

    @Test
    fun fdroidDateBeatsMissingAptoide() {
        val records = listOf(
            FdroidAppRecord("app.user", 1_600_000_000_000L, null, "official"),
        )
        val picks = ReleaseRefresh.fdroidPicks(records, setOf("app.user"))
        assertEquals(1_600_000_000_000L, picks.getValue("app.user").ms)
        assertEquals(RemoteReleasedSource.Fdroid, picks.getValue("app.user").source)
    }

    @Test
    fun runProbesAptoideEvenWhenFdroidLists() {
        val user = sampleApp("app.user", "User", installedAtMs = 1_600_000_000_000L)
        val other = sampleApp("app.other", "Other", installedAtMs = 1_600_000_000_000L)
        val repo = FdroidRepo("official", FdroidRepoKind.Official, "https://example/index.json", true)
        val fdroid = FdroidIndexFetcher {
            indexJson("""{"apps":[{"packageName":"app.user","lastUpdated":1610000000000}]}""")
        }
        val aptoide = AptoideMetaFetcher {
            Result.success("""{"data":{"updated":"2024-01-02 00:00:00","file":{"vername":"1"}}}""")
        }
        val picks = ReleaseRefresh.run(
            apps = listOf(user, other),
            repos = listOf(repo),
            aptoideEnabled = true,
            fdroidFetcher = fdroid,
            aptoideFetcher = aptoide,
            nowMs = 1_720_000_000_000L,
            sleepMs = {},
        )
        assertTrue(picks.getValue("app.user").offers.any { it.source == RemoteReleasedSource.Fdroid && it.listed })
        assertTrue(picks.getValue("app.user").offers.any { it.source == RemoteReleasedSource.Aptoide && it.listed })
        assertTrue(picks.getValue("app.other").offers.any { it.source == RemoteReleasedSource.Aptoide && it.listed })
        assertTrue(picks.getValue("app.other").ms != null)
    }

    @Test
    fun padsOfficialWhenOnlyIzzyHits() {
        val user = sampleApp("app.user", "User", installedAtMs = 1_600_000_000_000L)
        val official = FdroidRepo("official", FdroidRepoKind.Official, "https://example/official.json", true)
        val izzy = FdroidRepo("izzy", FdroidRepoKind.Izzy, "https://example/izzy.json", true)
        val picks = ReleaseRefresh.run(
            apps = listOf(user),
            repos = listOf(official, izzy),
            aptoideEnabled = false,
            fdroidFetcher = FdroidIndexFetcher { url ->
                if (url.contains("izzy")) {
                    indexJson("""{"apps":[{"packageName":"app.user","lastUpdated":1610000000000,"suggestedVersionName":"2.0","sourceCode":"https://github.com/a/a"}]}""")
                } else {
                    indexJson("""{"apps":[]}""")
                }
            },
            aptoideFetcher = AptoideMetaFetcher { Result.success("") },
            nowMs = 1_720_000_000_000L,
            playClient = PlayPageClient { PlayPageResponse(200, """<div itemprop="softwareVersion" content="1.0"></div>""") },
            sleepMs = {},
        )
        val sources = picks.getValue("app.user").offers.associate { it.source to it.listed }
        assertEquals(true, sources[RemoteReleasedSource.Izzy])
        assertEquals(false, sources[RemoteReleasedSource.Fdroid])
        assertTrue(
            picks.getValue("app.user").offers.any {
                it.source == RemoteReleasedSource.Fdroid && it.known && !it.listed
            },
        )
        assertEquals(true, sources[RemoteReleasedSource.Play])
        assertFalse(sources.containsKey(RemoteReleasedSource.Forge))
        assertFalse(sources.containsKey(RemoteReleasedSource.Aptoide))
    }

    @Test
    fun reportsProgressPerLocationNotPerApp() {
        val ticks = mutableListOf<RefreshProgress>()
        val githubHits = AtomicInteger(0)
        val repo = FdroidRepo("official", FdroidRepoKind.Official, "https://example/index.json", true)
        ReleaseRefresh.run(
            apps = listOf(
                sampleApp("app.user", "User", installedAtMs = 1_600_000_000_000L),
                sampleApp("app.other", "Other", installedAtMs = 1_600_000_000_000L),
            ),
            repos = listOf(repo),
            aptoideEnabled = false,
            fdroidFetcher = FdroidIndexFetcher {
                indexJson("""{"apps":[{"packageName":"app.user","lastUpdated":1610000000000}]}""")
            },
            aptoideFetcher = AptoideMetaFetcher { Result.success("") },
            nowMs = 1_720_000_000_000L,
            playClient = PlayPageClient { PlayPageResponse(404, "missing") },
            gitHubClient = GitHubSearchClient {
                githubHits.incrementAndGet()
                GitHubSearchPage(200, """{"items":[]}""")
            },
            sleepMs = {},
            onProgress = { ticks.add(it) },
        )
        assertEquals(0, ticks.first().done)
        assertEquals(RefreshProgress(5, 5, ticks.last().location), ticks.last())
        assertEquals(0, githubHits.get())
        assertTrue(ticks.any { it.location.startsWith("F-Droid · official") })
        assertTrue(ticks.any { it.location.contains("Play · Other (app.other)") })
        assertTrue(ticks.any { it.location.contains("Play · User (app.user)") })
        assertTrue(ticks.any { it.location.contains("GitHub · Other (app.other)") })
        assertTrue(ticks.any { it.location.contains("GitHub · User (app.user)") })
    }
}
