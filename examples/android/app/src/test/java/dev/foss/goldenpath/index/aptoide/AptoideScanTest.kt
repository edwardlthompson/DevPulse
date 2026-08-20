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
    }
}
