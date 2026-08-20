package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.aptoide.AptoideMetaFetcher
import dev.foss.goldenpath.index.fdroid.FdroidIndexFetcher
import dev.foss.goldenpath.index.fdroid.FdroidRepo
import dev.foss.goldenpath.index.fdroid.FdroidRepoKind
import dev.foss.goldenpath.index.forge.GitHubSearchClient
import dev.foss.goldenpath.index.forge.GitHubSearchPage
import dev.foss.goldenpath.index.play.PlayPageClient
import dev.foss.goldenpath.index.play.PlayPageResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ReleaseRefreshParallelTest {
    private fun indexJson(json: String) = Result.success(json.toByteArray())

    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
        RefreshTrace.emit = {}
    }

    @Test
    fun fetchesReposInParallel() {
        val started = CountDownLatch(2)
        val official = FdroidRepo("official", FdroidRepoKind.Official, "https://example/official.json", true)
        val izzy = FdroidRepo("izzy", FdroidRepoKind.Izzy, "https://example/izzy.json", true)
        val pool = Executors.newFixedThreadPool(2)
        try {
            val picks = ReleaseRefresh.run(
                apps = listOf(sampleApp("app.user", "User", installedAtMs = 1_600_000_000_000L)),
                repos = listOf(official, izzy),
                aptoideEnabled = false,
                fdroidFetcher = FdroidIndexFetcher {
                    started.countDown()
                    assertTrue(started.await(2, TimeUnit.SECONDS))
                    if (it.contains("izzy")) {
                        indexJson("""{"apps":[{"packageName":"app.user","lastUpdated":1610000000000}]}""")
                    } else {
                        indexJson("""{"apps":[]}""")
                    }
                },
                aptoideFetcher = AptoideMetaFetcher { Result.success("") },
                nowMs = 1_720_000_000_000L,
                sleepMs = {},
                executor = pool,
            )
            assertTrue(picks.getValue("app.user").offers.any { it.source == RemoteReleasedSource.Izzy && it.listed })
        } finally {
            pool.shutdown()
        }
    }

    @Test
    fun probesGithubEvenWhenPlayLists() {
        var githubHits = 0
        val repo = FdroidRepo("official", FdroidRepoKind.Official, "https://example/index.json", true)
        val picks = ReleaseRefresh.run(
            apps = listOf(sampleApp("app.user", "User", installedAtMs = 1_600_000_000_000L)),
            repos = listOf(repo),
            aptoideEnabled = false,
            fdroidFetcher = FdroidIndexFetcher { indexJson("""{"apps":[]}""") },
            aptoideFetcher = AptoideMetaFetcher { Result.success("") },
            nowMs = 1_720_000_000_000L,
            playClient = PlayPageClient {
                PlayPageResponse(200, """<div itemprop="softwareVersion" content="1.0"></div>""")
            },
            gitHubClient = GitHubSearchClient {
                githubHits += 1
                GitHubSearchPage(200, """{"items":[]}""")
            },
            sleepMs = {},
        )
        assertEquals(0, githubHits)
        assertTrue(picks.getValue("app.user").offers.any { it.source == RemoteReleasedSource.Play && it.listed })
        assertTrue(picks.getValue("app.user").offers.any { it.source == RemoteReleasedSource.Forge })
    }

    @Test
    fun probesPlayWhenFdroidLists() {
        var playHits = 0
        val repo = FdroidRepo("official", FdroidRepoKind.Official, "https://example/index.json", true)
        val picks = ReleaseRefresh.run(
            apps = listOf(sampleApp("app.user", "User", installedAtMs = 1_600_000_000_000L)),
            repos = listOf(repo),
            aptoideEnabled = false,
            fdroidFetcher = FdroidIndexFetcher {
                indexJson("""{"apps":[{"packageName":"app.user","lastUpdated":1610000000000}]}""")
            },
            aptoideFetcher = AptoideMetaFetcher { Result.success("") },
            nowMs = 1_720_000_000_000L,
            playClient = PlayPageClient {
                playHits += 1
                PlayPageResponse(200, """<div itemprop="softwareVersion" content="1.0"></div>""")
            },
            sleepMs = {},
        )
        assertEquals(1, playHits)
        assertTrue(picks.getValue("app.user").offers.any { it.source == RemoteReleasedSource.Fdroid && it.listed })
        assertTrue(picks.getValue("app.user").offers.any { it.source == RemoteReleasedSource.Play && it.listed })
    }

    @Test
    fun onePlayFailureDoesNotCancelOtherApps() {
        val repo = FdroidRepo("official", FdroidRepoKind.Official, "https://example/index.json", true)
        val picks = ReleaseRefresh.run(
            apps = listOf(
                sampleApp("app.bad", "Bad", installedAtMs = 1_600_000_000_000L),
                sampleApp("app.ok", "Ok", installedAtMs = 1_600_000_000_000L),
            ),
            repos = listOf(repo),
            aptoideEnabled = false,
            fdroidFetcher = FdroidIndexFetcher { indexJson("""{"apps":[]}""") },
            aptoideFetcher = AptoideMetaFetcher { Result.success("") },
            nowMs = 1_720_000_000_000L,
            playClient = PlayPageClient { pkg ->
                if (pkg == "app.bad") error("boom")
                PlayPageResponse(404, "missing")
            },
            sleepMs = {},
        )
        assertTrue(picks.containsKey("app.ok"))
        assertTrue(picks.containsKey("app.bad"))
    }
}
