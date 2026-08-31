package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionCompareTest {
    @Test
    fun detectsNewerRemote() {
        assertTrue(VersionCompare.isNewer("2.0", "1.9"))
        assertTrue(VersionCompare.isNewer("1.10.0", "1.9.9"))
        assertTrue(VersionCompare.isNewer("v1.2.0", "1.1.0"))
        assertFalse(VersionCompare.isNewer("v1.2.0", "1.2.0"))
        assertFalse(VersionCompare.isNewer("1.2.0", "1.2.0"))
        assertFalse(VersionCompare.isNewer("1.1", "1.2"))
        assertFalse(VersionCompare.isNewer(null, "1.0"))
        assertTrue(VersionCompare.isNewer(null, "1.0", installedCode = 118L, remoteCode = 120L))
        assertTrue(VersionCompare.isNewer("2.4.1", "2.4.1", installedCode = 118L, remoteCode = 120L))
    }

    @Test
    fun apkFilenameIsNotNewerThanEmbeddedVersion() {
        assertFalse(
            VersionCompare.isNewer("FairEmail-v1.2332a-large-release.apk", "1.2332"),
        )
        assertFalse(
            VersionCompare.isNewer("04_RapidRAW_v1.6.2_android_aarch64.apk", "1.6.2"),
        )
        assertTrue(VersionCompare.isNewer("Point-and-Shoot-0.14.1.apk", "0.14.0"))
    }

    @Test
    fun githubPathIsNotNewerThanSameRelease() {
        assertFalse(
            VersionCompare.isNewer(
                "com.akylas.documentscanner/android/github/1.25.0/160",
                "1.25.0.160",
            ),
        )
        assertTrue(
            VersionCompare.isNewer(
                "com.akylas.documentscanner/android/github/1.26.0/170",
                "1.25.0.160",
            ),
        )
    }

    @Test
    fun versionCodeRemoteMatchesInstalledCode() {
        assertFalse(VersionCompare.isNewer("2026080501", "2026.08.05", 2026080501L))
        assertTrue(VersionCompare.isNewer("2026090101", "2026.08.05", 2026080501L))
        assertEquals("1.6.2", VersionCompare.canonical("04_RapidRAW_v1.6.2_android_aarch64.apk"))
        assertFalse(VersionCompare.isNewer("2023", "2023", 8))
    }

    @Test
    fun forgeTagPrefixIsNotNewerThanSameRelease() {
        assertFalse(VersionCompare.isNewer("fdroid-v2.3.6", "2.3.6"))
        assertFalse(VersionCompare.isNewer("v1.1.0", "1.1.0"))
        assertEquals("2.3.6", VersionCompare.canonical("fdroid-v2.3.6"))
        assertTrue(VersionCompare.isNewer("fdroid-v2.4.0", "2.3.6"))
    }
}
