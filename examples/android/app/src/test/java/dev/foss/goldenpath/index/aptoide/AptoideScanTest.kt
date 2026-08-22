package dev.foss.goldenpath.index.aptoide

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AptoideScanTest {
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
}
