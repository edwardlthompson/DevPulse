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

class ReleaseRefreshOverlapTest {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
        RefreshTrace.emit = {}
    }

    @Test
    fun githubOverlapsPlayAndAptoide() {
        val playInFlight = CountDownLatch(1)
        val githubStarted = CountDownLatch(1)
        val aptoideStarted = CountDownLatch(1)
        val repo = FdroidRepo("official", FdroidRepoKind.Official, "https://example/index.json", true)
        val pool = Executors.newFixedThreadPool(6)
        try {
            ReleaseRefresh.run(
                apps = listOf(sampleApp("com.instagram.android", "Instagram", installedAtMs = 1_600_000_000_000L)),
                repos = listOf(repo),
                aptoideEnabled = true,
                fdroidFetcher = FdroidIndexFetcher { Result.success("""{"apps":[]}""".toByteArray()) },
                aptoideFetcher = AptoideMetaFetcher {
                    aptoideStarted.countDown()
                    Result.success("""{"data":{"updated":"2024-01-02 00:00:00","file":{"vername":"1"}}}""")
                },
                nowMs = 1_720_000_000_000L,
                playClient = PlayPageClient {
                    playInFlight.countDown()
                    assertTrue(githubStarted.await(3, TimeUnit.SECONDS))
                    PlayPageResponse(200, """<div itemprop="softwareVersion" content="1.0"></div>""")
                },
                gitHubClient = GitHubSearchClient {
                    assertTrue(playInFlight.await(3, TimeUnit.SECONDS))
                    githubStarted.countDown()
                    GitHubSearchPage(200, """{"items":[]}""")
                },
                sleepMs = {},
                executor = pool,
                searchUnknowns = true,
            )
            assertEquals(0, aptoideStarted.count)
        } finally {
            pool.shutdown()
        }
    }
}
