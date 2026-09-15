package dev.foss.goldenpath.index.aptoide

import dev.foss.goldenpath.inventory.ListingExtraBook
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.UpdateArtifactMemory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AptoideScanTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
        ListingExtraBook.clear()
    }

    @Test
    fun okLookupBecomesAptoidePick() {
        val pick = AptoideScan.toPick(AptoideLookup(99L, "1", AptoideLookupStatus.Ok))
        assertEquals(99L, pick?.ms)
    }

    @Test
    fun unknownLookupHasNoPick() {
        assertNull(AptoideScan.toPick(AptoideLookup(null, null, AptoideLookupStatus.UnknownCheckManually)))
    }

    @Test
    fun fakeFetcherDoesNotSleepWhenIntervalZero() {
        val json = """{"data":{"updated":"2024-01-02 00:00:00","file":{"vername":"1"}}}"""
        val picks = AptoideScan.picksFor(
            packageNames = listOf("app.one"),
            fetcher = AptoideMetaFetcher { Result.success(json) },
            nowMs = 1_720_000_000_000L,
            sleepMs = {},
        )
        assertEquals(1, picks.size)
        assertEquals(dev.foss.goldenpath.inventory.RemoteReleasedSource.Aptoide, picks.getValue("app.one").source)
        assertEquals("https://en.aptoide.com/app?package_name=app.one", picks.getValue("app.one").pageUrl)
    }

    @Test
    fun applyBatchListsHitsAndSkipsUnsigned() {
        val json = checkNotNull(javaClass.classLoader?.getResourceAsStream("aptoide/updates-ok.json"))
            .bufferedReader().use { it.readText() }
        val hits = AptoideScan.applyBatch(
            listOf(
                AptoideApkRef("cm.aptoide.pt", "D5:90:A7:D7:92:FD:03:31:54:2D:99:FA:F9:99:76:41:79:07:73:A9"),
                AptoideApkRef("app.unsigned", signature = null),
            ),
            AptoideUpdatesFetcher { Result.success(json) },
            nowMs = 1_720_000_000_000L,
        )
        assertEquals("9.22.0", hits.getValue("cm.aptoide.pt").versionName)
        assertEquals(true, hits.getValue("cm.aptoide.pt").listed)
        assertEquals(null, hits["app.unsigned"])
    }

    @Test
    fun applyBatchOmissionIsKnownMiss() {
        val json = checkNotNull(javaClass.classLoader?.getResourceAsStream("aptoide/updates-ok.json"))
            .bufferedReader().use { it.readText() }
        val hits = AptoideScan.applyBatch(
            listOf(AptoideApkRef("app.other", "AA:BB")),
            AptoideUpdatesFetcher { Result.success(json) },
            nowMs = 1_720_000_000_000L,
        )
        assertEquals(false, hits.getValue("app.other").listed)
        assertEquals(true, hits.getValue("app.other").known)
    }

    @Test
    fun unameBecomesAppViewListing() {
        val json = """{"data":{"uname":"wipefiles","updated":"2024-01-02 00:00:00","file":{"vername":"1"}}}"""
        val pick = AptoideScan.toPick(
            AptoideMetaParser.parse(json, 1_720_000_000_000L),
            "uk.org.platitudes.wipefiles",
        )
        assertEquals("https://wipefiles.en.aptoide.com/", pick?.pageUrl)
    }

    @Test
    fun lookupForInstallUsesBatchWhenSigned() {
        val json = checkNotNull(javaClass.classLoader?.getResourceAsStream("aptoide/updates-ok.json"))
            .bufferedReader().use { it.readText() }
        var metaCalls = 0
        val artifact = AptoideScan.lookupForInstall(
            packageName = "cm.aptoide.pt",
            signingSha1 = "D5:90:A7:D7:92:FD:03:31:54:2D:99:FA:F9:99:76:41:79:07:73:A9",
            versionCode = 1,
            updates = AptoideUpdatesFetcher { Result.success(json) },
            meta = AptoideMetaFetcher { metaCalls += 1; error("getMeta") },
            nowMs = 1_720_000_000_000L,
        )
        assertEquals(0, metaCalls)
        assertEquals("https://pool.apk.aptoide.com/example/a.apk", artifact?.downloadUrl)
        assertEquals(9220L, artifact?.versionCode)
        assertEquals(setOf("x86"), artifact?.nativeCodes)
        assertEquals(RemoteReleasedSource.Aptoide, artifact?.source)
    }

    @Test
    fun lookupForInstallUsesGetMetaWhenUnsigned() {
        val json = """{"data":{"updated":"2024-06-15 12:00:00","file":{"vername":"1.0","path":"https://pool.apk.aptoide.com/apps/u.apk"}}}"""
        var updateCalls = 0
        val artifact = AptoideScan.lookupForInstall(
            packageName = "app.unsigned",
            signingSha1 = null,
            updates = AptoideUpdatesFetcher { updateCalls += 1; error("batch") },
            meta = AptoideMetaFetcher { Result.success(json) },
            nowMs = 1_720_000_000_000L,
        )
        assertEquals(0, updateCalls)
        assertEquals("https://pool.apk.aptoide.com/apps/u.apk", artifact?.downloadUrl)
    }
}
