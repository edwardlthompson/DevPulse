package dev.foss.goldenpath.index.apkpure

import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.UpdateArtifact
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

    @Test
    fun resolveDoesNotReuseAnotherSource() {
        UpdateArtifactMemory.add(
            UpdateArtifact("app.listed", RemoteReleasedSource.Fdroid, "https://f-droid.org/repo/app.listed_1.apk"),
        )
        assertNull(ApkPureDirect.resolve("app.listed", ApkPureBatchFetcher { Result.failure(IllegalStateException("net")) }))
        val json = checkNotNull(javaClass.classLoader?.getResourceAsStream("apkpure/update-ok.json"))
            .bufferedReader().use { it.readText() }
        val artifact = ApkPureDirect.resolve("app.listed", ApkPureBatchFetcher { Result.success(json) })
        assertEquals("https://d.apkpure.com/b/APK/app.listed?versionCode=31", artifact?.downloadUrl)
        assertEquals(RemoteReleasedSource.ApkPure, artifact?.source)
    }
}
