package dev.foss.goldenpath.index.apkpure

import dev.foss.goldenpath.inventory.UpdateArtifactMemory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ApkPureMetaParserTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
    }

    @Test
    fun readsVersionWithoutGuessingADate() {
        val json = checkNotNull(javaClass.classLoader?.getResourceAsStream("apkpure/update-ok.json"))
            .bufferedReader().use { it.readText() }
        val offer = ApkPureMetaParser.parseMany(json).getValue("app.listed")
        assertTrue(offer.listed)
        assertEquals("3.1.0", offer.versionName)
        assertEquals(null, offer.ms)
        assertEquals("https://apkpure.com/search?q=app.listed", offer.pageUrl)
        assertEquals(
            "https://d.apkpure.com/b/APK/app.listed?versionCode=31",
            UpdateArtifactMemory.best("app.listed")?.downloadUrl,
        )
    }

    @Test
    fun keepsCdnUrlAndPrefersApkOverXapk() {
        val amp = "\\u0026"
        val json =
            """{"app_update_response":[{"package_name":"app.cdn","version_name":"1.2","version_code":12,"asset":{"trackers":[],"type":"XAPK","url":"https://download.cdnpure.com/b/XAPK/app.cdn?c=1${amp}as=z"}}]}"""
        ApkPureMetaParser.parseMany(json)
        assertEquals(
            "https://download.cdnpure.com/b/APK/app.cdn?c=1&as=z",
            UpdateArtifactMemory.best("app.cdn")?.downloadUrl,
        )
    }
}
