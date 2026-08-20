package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.aptoide.AptoideMetaFetcher
import dev.foss.goldenpath.index.fdroid.FdroidIndexFetcher
import dev.foss.goldenpath.index.fdroid.FdroidIndexStore
import dev.foss.goldenpath.index.fdroid.FdroidRepo
import dev.foss.goldenpath.index.fdroid.FdroidRepoKind
import dev.foss.goldenpath.index.play.PlayPageClient
import dev.foss.goldenpath.index.play.PlayPageResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.concurrent.atomic.AtomicInteger

class ReleaseRefreshCacheTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private fun indexJson(json: String) = Result.success(json.toByteArray())

    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
        RefreshTrace.emit = {}
    }

    @Test
    fun tracesRepoStartAndFinish() {
        val lines = mutableListOf<String>()
        RefreshTrace.emit = { lines.add(it) }
        val repo = FdroidRepo("official", FdroidRepoKind.Official, "https://example/index.json", true)
        ReleaseRefresh.run(
            apps = listOf(sampleApp("app.user", "User", installedAtMs = 1_600_000_000_000L)),
            repos = listOf(repo),
            aptoideEnabled = false,
            fdroidFetcher = FdroidIndexFetcher {
                indexJson("""{"apps":[{"packageName":"app.user","lastUpdated":1610000000000}]}""")
            },
            aptoideFetcher = AptoideMetaFetcher { Result.success("") },
            nowMs = 1_720_000_000_000L,
            sleepMs = {},
        )
        assertTrue(lines.any { it == "F-Droid · official" })
        assertTrue(lines.any { it.startsWith("fdroid official ok") })
        assertTrue(lines.any { it.startsWith("refresh done") })
    }

    @Test
    fun oneRepoFailureDoesNotDropOtherRepos() {
        val user = sampleApp("app.user", "User", installedAtMs = 1_600_000_000_000L)
        val official = FdroidRepo("official", FdroidRepoKind.Official, "https://example/official.json", true)
        val izzy = FdroidRepo("izzy", FdroidRepoKind.Izzy, "https://example/izzy.json", true)
        val picks = ReleaseRefresh.run(
            apps = listOf(user),
            repos = listOf(official, izzy),
            aptoideEnabled = false,
            fdroidFetcher = FdroidIndexFetcher { url ->
                if (url.contains("official")) error("boom")
                indexJson("""{"apps":[{"packageName":"app.user","lastUpdated":1610000000000,"suggestedVersionName":"2.0"}]}""")
            },
            aptoideFetcher = AptoideMetaFetcher { Result.success("") },
            nowMs = 1_720_000_000_000L,
            sleepMs = {},
        )
        assertTrue(picks.getValue("app.user").offers.any { it.source == RemoteReleasedSource.Izzy && it.listed })
    }

    @Test
    fun missingPlayPageIsNotListed() {
        val user = sampleApp("app.user", "User", installedAtMs = 1_600_000_000_000L)
        val repo = FdroidRepo("official", FdroidRepoKind.Official, "https://example/index.json", true)
        val picks = ReleaseRefresh.run(
            apps = listOf(user),
            repos = listOf(repo),
            aptoideEnabled = false,
            fdroidFetcher = FdroidIndexFetcher {
                indexJson("""{"apps":[]}""")
            },
            aptoideFetcher = AptoideMetaFetcher { Result.success("") },
            nowMs = 1_720_000_000_000L,
            playClient = PlayPageClient { PlayPageResponse(404, "not found") },
            sleepMs = {},
        )
        val pick = picks.getValue("app.user")
        assertTrue(pick.offers.any { it.source == RemoteReleasedSource.Play && !it.listed })
        assertFalse(pick.offers.any { it.source == RemoteReleasedSource.Play && it.listed })
        assertEquals(0, RemoteRelease.apply(user.copy(versionName = "1.0"), pick).updateLinks.size)
    }

    @Test
    fun emptyFdroidIndexIsFailNotOk() {
        val lines = mutableListOf<String>()
        RefreshTrace.emit = { lines.add(it) }
        val repo = FdroidRepo("archive", FdroidRepoKind.Archive, "https://example/archive.json", true)
        ReleaseRefresh.run(
            apps = listOf(sampleApp("app.user", "User", installedAtMs = 1_600_000_000_000L)),
            repos = listOf(repo),
            aptoideEnabled = false,
            fdroidFetcher = FdroidIndexFetcher { Result.success(ByteArray(0)) },
            aptoideFetcher = AptoideMetaFetcher { Result.success("") },
            nowMs = 1_720_000_000_000L,
            sleepMs = {},
        )
        assertTrue(lines.any { it.startsWith("fdroid archive fail") && it.contains("empty index") })
        assertFalse(lines.any { it.contains("downloaded 0B") })
        assertFalse(lines.any { it.startsWith("fdroid archive ok") })
    }

    @Test
    fun cacheHitSkipsNetwork() {
        val hits = AtomicInteger(0)
        val store = FdroidIndexStore(tmp.newFolder())
        val repo = FdroidRepo("official", FdroidRepoKind.Official, "https://example/index.json", true)
        val fetcher = FdroidIndexFetcher {
            hits.incrementAndGet()
            indexJson("""{"apps":[{"packageName":"app.user","lastUpdated":1610000000000,"suggestedVersionName":"2.0"}]}""")
        }
        val user = sampleApp("app.user", "User", installedAtMs = 1_600_000_000_000L)
        repeat(2) {
            RemoteReleaseMemory.clear()
            ReleaseRefresh.run(
                apps = listOf(user),
                repos = listOf(repo),
                aptoideEnabled = false,
                fdroidFetcher = fetcher,
                aptoideFetcher = AptoideMetaFetcher { Result.success("") },
                nowMs = 1_720_000_000_000L,
                indexStore = store,
                sleepMs = {},
            )
        }
        assertEquals(1, hits.get())
    }
}
