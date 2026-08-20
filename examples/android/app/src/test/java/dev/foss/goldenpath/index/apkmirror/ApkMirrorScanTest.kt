package dev.foss.goldenpath.index.apkmirror

import dev.foss.goldenpath.inventory.RefreshTrace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApkMirrorScanTest {
    @Test
    fun batchMapsHitsAndMisses() {
        val json = checkNotNull(javaClass.classLoader?.getResourceAsStream("apkmirror/exists-ok.json"))
            .bufferedReader().use { it.readText() }
        val offers = ApkMirrorScan.offersFor(
            listOf("app.listed", "app.other"),
            ApkMirrorBatchFetcher { Result.success(json) },
            1_720_000_000_000L,
        )
        assertTrue(offers.getValue("app.listed").listed)
        assertFalse(offers.getValue("app.other").listed)
        assertTrue(offers.getValue("app.other").known)
        assertEquals("2.4.0", offers.getValue("app.listed").versionName)
    }

    @Test
    fun failedFetchIsUnknown() {
        val offers = ApkMirrorScan.offersFor(
            listOf("app.x"),
            ApkMirrorBatchFetcher { Result.failure(IllegalStateException("down")) },
            1_720_000_000_000L,
        )
        assertFalse(offers.getValue("app.x").listed)
        assertFalse(offers.getValue("app.x").known)
    }

    @Test
    fun failedFetchTracesChunk() {
        val lines = mutableListOf<String>()
        RefreshTrace.emit = { lines += it }
        ApkMirrorScan.offersFor(
            listOf("app.x"),
            ApkMirrorBatchFetcher { Result.failure(IllegalStateException("down")) },
            1_720_000_000_000L,
        )
        RefreshTrace.emit = {}
        assertTrue(lines.any { it.contains("apkmirror chunk") && it.contains("fail") })
    }
}
