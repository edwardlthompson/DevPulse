package dev.foss.goldenpath.index.apkpure

import dev.foss.goldenpath.inventory.DumpChunkBook
import dev.foss.goldenpath.inventory.RemoteReleaseMemory
import dev.foss.goldenpath.inventory.UpdateArtifactMemory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ApkPureScanTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
        RemoteReleaseMemory.clear()
        DumpChunkBook.clear()
        DumpChunkBook.persistDir = null
    }
    @Test
    fun listedAndUnknownStayHonest() {
        val json = checkNotNull(javaClass.classLoader?.getResourceAsStream("apkpure/update-ok.json"))
            .bufferedReader().use { it.readText() }
        val offers = ApkPureScan.offersFor(
            listOf("app.listed", "app.other"),
            ApkPureBatchFetcher { Result.success(json) },
            nowMs = 1_720_000_000_000L,
        )
        assertTrue(offers.getValue("app.listed").listed)
        assertFalse(offers.getValue("app.other").listed)
        assertTrue(offers.getValue("app.other").known)
    }
}
