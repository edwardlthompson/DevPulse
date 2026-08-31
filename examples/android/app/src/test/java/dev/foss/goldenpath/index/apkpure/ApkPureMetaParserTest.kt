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

    @Test
    fun parsesNestedMultiAppResponseWithComplexObjects() {
        val json = """
            {
              "app_update_response": [
                {
                  "package_name": "io.mapgenie.division2map",
                  "version_name": "2.4.2",
                  "version_code": 35,
                  "developer_open_config": {
                    "title": "MapGenie",
                    "icon": "https://image.winudf.com/icon.png"
                  },
                  "asset": {
                    "type": "APK",
                    "url": "https://d.apkpure.com/b/APK/io.mapgenie.division2map?versionCode=35"
                  },
                  "sign": {
                    "sha256": "abcdef"
                  }
                },
                {
                  "package_name": "de.komoot.android",
                  "version_name": "2026.34.2",
                  "version_code": 1234,
                  "developer_open_config": {
                    "title": "Komoot"
                  },
                  "asset": {
                    "type": "XAPK",
                    "url": "https://d.apkpure.com/b/XAPK/de.komoot.android?versionCode=1234"
                  }
                }
              ]
            }
        """.trimIndent()

        val offers = ApkPureMetaParser.parseMany(json)
        assertEquals(2, offers.size)
        assertEquals("2.4.2", offers["io.mapgenie.division2map"]?.versionName)
        assertEquals(35L, offers["io.mapgenie.division2map"]?.versionCode)
        assertEquals(
            "https://d.apkpure.com/b/APK/io.mapgenie.division2map?versionCode=35",
            UpdateArtifactMemory.best("io.mapgenie.division2map")?.downloadUrl,
        )

        assertEquals("2026.34.2", offers["de.komoot.android"]?.versionName)
        assertEquals(1234L, offers["de.komoot.android"]?.versionCode)
        assertEquals(
            "https://d.apkpure.com/b/APK/de.komoot.android?versionCode=1234",
            UpdateArtifactMemory.best("de.komoot.android")?.downloadUrl,
        )
    }
}
