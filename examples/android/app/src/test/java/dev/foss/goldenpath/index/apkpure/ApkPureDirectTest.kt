package dev.foss.goldenpath.index.apkpure

import dev.foss.goldenpath.inventory.UpdateArtifactMemory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ApkPureDirectTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
    }

    @Test
    fun resolveReadsAssetUrlFromUpdateJson() {
        val json = checkNotNull(javaClass.classLoader?.getResourceAsStream("apkpure/update-ok.json"))
            .bufferedReader().use { it.readText() }
        val artifact = ApkPureDirect.resolve("app.listed", ApkPureBatchFetcher { Result.success(json) })
        assertEquals("https://d.apkpure.com/b/APK/app.listed?versionCode=31", artifact?.downloadUrl)
    }

    @Test
    fun resolveSkipsBlankAndFailedFetch() {
        assertNull(ApkPureDirect.resolve("  ", ApkPureBatchFetcher { Result.success("") }))
        assertNull(ApkPureDirect.resolve("app.x", ApkPureBatchFetcher { Result.failure(IllegalStateException("net")) }))
    }
}
