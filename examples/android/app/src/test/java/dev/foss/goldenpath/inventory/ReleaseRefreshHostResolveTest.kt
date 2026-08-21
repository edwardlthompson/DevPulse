package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.aptoide.AptoideMetaFetcher
import dev.foss.goldenpath.index.fdroid.FdroidAppRecord
import dev.foss.goldenpath.index.fdroid.FdroidHostResolver
import dev.foss.goldenpath.index.fdroid.FdroidIndexFetcher
import dev.foss.goldenpath.index.fdroid.FdroidRepo
import dev.foss.goldenpath.index.fdroid.FdroidRepoKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ReleaseRefreshHostResolveTest {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
        RefreshTrace.emit = {}
    }

    @Test
    fun officialUsesHostResolveNotIndex() {
        val downloads = AtomicInteger(0)
        val official = FdroidRepo("official", FdroidRepoKind.Official, "https://example/index-v1.jar", true)
        val resolver = FdroidHostResolver { repo, wanted ->
            wanted.map { FdroidAppRecord(it, 1_700_000_000_000L, "https://github.com/acme/app", repo.id, "2.0") }
        }
        val pick = ReleaseRefresh.run(
            apps = listOf(sampleApp("org.acme.app", "Acme", installedAtMs = 1_600_000_000_000L)),
            repos = listOf(official),
            aptoideEnabled = false,
            fdroidFetcher = FdroidIndexFetcher {
                downloads.incrementAndGet()
                Result.success("""{"apps":[]}""".toByteArray())
            },
            aptoideFetcher = AptoideMetaFetcher { Result.success("") },
            nowMs = 1_720_000_000_000L,
            sleepMs = {},
            hostResolve = resolver,
        ).getValue("org.acme.app")
        assertEquals(0, downloads.get())
        assertEquals("2.0", pick.versionName)
        assertFalse(pick.offers.isEmpty())
    }

    @Test
    fun largeIndexIsHarvested() {
        val izzy = FdroidRepo("izzy", FdroidRepoKind.Izzy, "https://example/izzy.json", true)
        val raw = """{"apps":[{"packageName":"org.big.app","lastUpdated":1700000000000}],"packages":{"org.big.app":[{"apkName":"org.big.app_1.apk"}]}}"""
        val pick = ReleaseRefresh.run(
            apps = listOf(sampleApp("org.big.app", "Big", installedAtMs = 1L)),
            repos = listOf(izzy),
            aptoideEnabled = false,
            fdroidFetcher = FdroidIndexFetcher { Result.success(raw.toByteArray()) },
            aptoideFetcher = AptoideMetaFetcher { Result.success("") },
            nowMs = 2L,
            sleepMs = {},
        ).getValue("org.big.app")
        assertTrue(pick.offers.any { it.source == RemoteReleasedSource.Izzy && it.listed })
    }

    @Test
    fun emptyIzzyUsesExtraHostResolve() {
        val izzy = FdroidRepo("izzy", FdroidRepoKind.Izzy, "https://example/izzy.json", true)
        val resolver = FdroidHostResolver { repo, wanted ->
            wanted.map { FdroidAppRecord(it, 1_700_000_000_000L, "https://github.com/skip/app", repo.id, "3.0") }
        }
        val pick = ReleaseRefresh.run(
            apps = listOf(sampleApp("org.skip.app", "Skip", installedAtMs = 1L)),
            repos = listOf(izzy),
            aptoideEnabled = false,
            fdroidFetcher = FdroidIndexFetcher { Result.success(ByteArray(0)) },
            aptoideFetcher = AptoideMetaFetcher { Result.success("") },
            nowMs = 2L,
            sleepMs = {},
            hostResolve = resolver,
        ).getValue("org.skip.app")
        assertEquals("3.0", pick.versionName)
    }
}
