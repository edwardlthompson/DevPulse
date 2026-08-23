package dev.foss.goldenpath.index.apkmirror

import dev.foss.goldenpath.inventory.DumpChunkBook
import dev.foss.goldenpath.inventory.RefreshTrace
import dev.foss.goldenpath.inventory.RemoteReleaseMemory
import dev.foss.goldenpath.inventory.RemoteReleaseOffer
import dev.foss.goldenpath.inventory.RemoteReleaseRollup
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ApkMirrorScanTest {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
        DumpChunkBook.clear()
        DumpChunkBook.persistDir = null
        RefreshTrace.emit = {}
    }

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

    @Test
    fun fetchesEveryChunk() {
        val seen = java.util.concurrent.CopyOnWriteArrayList<Int>()
        val names = (1..201).map { "app.$it" }
        ApkMirrorScan.offersFor(
            names,
            ApkMirrorBatchFetcher { chunk ->
                seen += chunk.size
                Result.success("""{"data":[]}""")
            },
            1_720_000_000_000L,
        )
        assertEquals(listOf(100, 100, 1), seen.sortedDescending())
    }

    @Test
    fun freshListingsSkipHttp() {
        val fetches = java.util.concurrent.atomic.AtomicInteger(0)
        val now = 1_720_000_000_000L
        RemoteReleaseMemory.putAll(
            mapOf(
                "app.listed" to RemoteReleaseRollup.from(
                    listOf(
                        RemoteReleaseOffer(
                            RemoteReleasedSource.ApkMirror,
                            listed = true,
                            versionName = "2.4.0",
                            fetchedAtMs = now,
                        ),
                    ),
                ),
            ),
        )
        val offers = ApkMirrorScan.offersFor(
            listOf("app.listed"),
            ApkMirrorBatchFetcher {
                fetches.incrementAndGet()
                Result.success("")
            },
            now,
        )
        assertEquals(0, fetches.get())
        assertEquals("2.4.0", offers.getValue("app.listed").versionName)
    }
}
