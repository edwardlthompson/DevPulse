package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.apkmirror.ApkMirrorBatchFetcher
import dev.foss.goldenpath.index.apkpure.ApkPureBatchFetcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseRefreshDumpTest {
    @Test
    fun batchTicksOncePerAppPerEnabledDump() {
        val ticks = mutableListOf<String>()
        val clock = RefreshProgressClock { ticks += it.location }
        clock.addWork(2)
        val committed = mutableListOf<RemoteReleasedSource>()
        val mirrorJson = """{"data":[{"pname":"app.one","exists":true,"release":{"version":"1.0","publish_date":"2024-01-02"}}]}"""
        ReleaseRefreshDump.apply(
            apps = listOf(sampleApp("app.one", "One", installedAtMs = 1_600_000_000_000L)),
            apkMirrorEnabled = true,
            apkPureEnabled = false,
            apkMirrorFetcher = ApkMirrorBatchFetcher { Result.success(mirrorJson) },
            apkPureFetcher = ApkPureBatchFetcher { Result.success("") },
            nowMs = 1_720_000_000_000L,
            clock = clock,
        ) { _, offer -> committed += offer.source }
        assertEquals(listOf(RemoteReleasedSource.ApkMirror), committed)
        assertTrue(ticks.any { it.contains("ApkMirror · One (app.one)") })
    }
}
