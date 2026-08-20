package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.aptoide.AptoideMetaFetcher
import dev.foss.goldenpath.index.fdroid.FdroidIndexFetcher
import dev.foss.goldenpath.index.fdroid.FdroidRepo
import dev.foss.goldenpath.index.fdroid.FdroidRepoKind
import dev.foss.goldenpath.index.forge.GitHubSearchClient
import dev.foss.goldenpath.index.forge.GitHubSearchPage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReleaseRefreshOutletsTest {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
        RefreshTrace.emit = {}
    }

    @Test
    fun skipsDisabledOutletProbesAndCountsOnlyEnabledLocations() {
        val ticks = mutableListOf<RefreshProgress>()
        var aptoideHits = 0
        val official = FdroidRepo("official", FdroidRepoKind.Official, "https://example/official.json", true)
        ReleaseRefresh.run(
            apps = listOf(
                sampleApp("app.user", "User", installedAtMs = 1_600_000_000_000L),
                sampleApp("app.other", "Other", installedAtMs = 1_600_000_000_000L),
            ),
            repos = listOf(official),
            aptoideEnabled = false,
            fdroidFetcher = FdroidIndexFetcher {
                Result.success("""{"apps":[{"packageName":"app.user","lastUpdated":1610000000000}]}""".toByteArray())
            },
            aptoideFetcher = AptoideMetaFetcher {
                aptoideHits += 1
                Result.success("")
            },
            nowMs = 1_720_000_000_000L,
            playClient = null,
            gitHubClient = GitHubSearchClient { GitHubSearchPage(200, """{"items":[]}""") },
            sleepMs = {},
            onProgress = { ticks.add(it) },
        )
        assertEquals(0, aptoideHits)
        assertEquals(RefreshLocations.total(1, 2, play = false, aptoide = false, forge = true), ticks.last().total)
        assertTrue(ticks.none { it.location.contains("Play") })
        assertTrue(ticks.none { it.location.contains("Aptoide") })
        assertTrue(ticks.any { it.location.contains("GitHub · User (app.user)") })
        assertTrue(ticks.any { it.location.contains("GitHub · Other (app.other)") })
    }
}
