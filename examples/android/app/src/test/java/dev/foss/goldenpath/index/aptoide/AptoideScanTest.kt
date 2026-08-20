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
    fun unameBecomesAppViewListing() {
        val json = """{"data":{"uname":"wipefiles","updated":"2024-01-02 00:00:00","file":{"vername":"1"}}}"""
        val pick = AptoideScan.toPick(
            AptoideMetaParser.parse(json, 1_720_000_000_000L),
            "uk.org.platitudes.wipefiles",
        )
        assertEquals("https://wipefiles.en.aptoide.com/", pick?.pageUrl)
    }
}
