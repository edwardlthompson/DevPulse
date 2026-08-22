package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.aptoide.AptoideMetaFetcher
import dev.foss.goldenpath.index.fdroid.FdroidAppRecord
import dev.foss.goldenpath.index.fdroid.FdroidHostResolver
import dev.foss.goldenpath.index.fdroid.FdroidIndexFetcher
import dev.foss.goldenpath.index.fdroid.FdroidNameCatalog
import dev.foss.goldenpath.index.fdroid.FdroidRepo
import dev.foss.goldenpath.index.fdroid.FdroidRepoKind
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ReleaseRefreshCatalogCacheTest {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
        RefreshTrace.emit = {}
    }

    @Test
    fun officialCatalogSkipsNamesNotOnFdroid() {
        val seen = mutableListOf<String>()
        val official = FdroidRepo("official", FdroidRepoKind.Official, "https://example/index-v1.jar", true)
        ReleaseRefresh.run(
            apps = listOf(
                sampleApp("org.acme.app", "Acme", installedAtMs = 1L),
                sampleApp("com.play.only", "Play", installedAtMs = 1L),
            ),
            repos = listOf(official),
            aptoideEnabled = false,
            fdroidFetcher = FdroidIndexFetcher { Result.success("""{"apps":[]}""".toByteArray()) },
            aptoideFetcher = AptoideMetaFetcher { Result.success("") },
            nowMs = 2L,
            sleepMs = {},
            hostResolve = FdroidHostResolver { _, wanted ->
                seen += wanted.sorted()
                wanted.map { FdroidAppRecord(it, 1L, null, "official", "1.0") }
            },
            nameCatalog = FdroidNameCatalog.parse("org.acme.app\n", ""),
        )
        assertEquals(listOf("org.acme.app"), seen)
    }

    @Test
    fun freshOfficialListingSkipsHostResolve() {
        val calls = AtomicInteger(0)
        val now = 1_720_000_000_000L
        RemoteReleaseMemory.putAll(
            mapOf(
                "org.acme.app" to RemoteReleaseRollup.from(
                    listOf(
                        RemoteReleaseOffer(
                            RemoteReleasedSource.Fdroid,
                            1L,
                            "9.9",
                            "https://f-droid.org/packages/org.acme.app/",
                            fetchedAtMs = now,
                        ),
                    ),
                ),
            ),
        )
        val pick = ReleaseRefresh.run(
            apps = listOf(sampleApp("org.acme.app", "Acme", installedAtMs = 1L)),
            repos = listOf(FdroidRepo("official", FdroidRepoKind.Official, "https://example/index-v1.jar", true)),
            aptoideEnabled = false,
            fdroidFetcher = FdroidIndexFetcher { Result.success("""{"apps":[]}""".toByteArray()) },
            aptoideFetcher = AptoideMetaFetcher { Result.success("") },
            nowMs = now + 1_000L,
            sleepMs = {},
            hostResolve = FdroidHostResolver { _, _ ->
                calls.incrementAndGet()
                emptyList()
            },
        ).getValue("org.acme.app")
        assertEquals(0, calls.get())
        assertEquals("9.9", pick.versionName)
    }
}
