package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.aptoide.AptoideMetaFetcher
import dev.foss.goldenpath.index.fdroid.FdroidIndexFetcher
import dev.foss.goldenpath.index.fdroid.FdroidRepo
import dev.foss.goldenpath.index.fdroid.FdroidRepoKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScanPerf200Test {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
        RefreshTrace.emit = {}
        ScanPerf.reset()
    }

    @Test
    fun twoHundredAppScanRecordsDurationAndPeakMemory() {
        val apps = (1..200).map { i ->
            sampleApp("app.perf.$i", "Perf $i", installedAtMs = 1_600_000_000_000L + i)
        }
        val appsJson = apps.joinToString(",") { a ->
            """{"packageName":"${a.packageName}","lastUpdated":1610000000000}"""
        }
        val repo = FdroidRepo("official", FdroidRepoKind.Official, "https://example/index.json", true)
        val fdroid = FdroidIndexFetcher {
            Result.success("""{"apps":[$appsJson]}""".toByteArray())
        }
        ScanPerf.markColdStart(420L)
        ScanPerf.beginScan()
        val picks = ReleaseRefresh.run(
            apps = apps,
            repos = listOf(repo),
            aptoideEnabled = false,
            fdroidFetcher = fdroid,
            aptoideFetcher = AptoideMetaFetcher { Result.success("") },
            nowMs = 1_720_000_000_000L,
            sleepMs = {},
        )
        ScanPerf.sampleMemory()
        val snap = ScanPerf.finishScan(apps.size)
        assertEquals(200, picks.size)
        assertEquals(200, snap.appCount)
        assertEquals(420L, snap.coldStartMs)
        assertTrue("duration should be positive", snap.durationMs >= 0L)
        assertTrue("peak memory should be positive", snap.peakMemoryBytes > 0L)
        println(
            "PERF_200 appCount=${snap.appCount} coldStartMs=${snap.coldStartMs} " +
                "durationMs=${snap.durationMs} peakMemoryBytes=${snap.peakMemoryBytes}",
        )
    }
}
