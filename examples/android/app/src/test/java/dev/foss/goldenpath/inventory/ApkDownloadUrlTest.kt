package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApkDownloadUrlTest {
    @Test
    fun upgradesHttpAndRejectsXapk() {
        assertEquals(
            "https://d.apkpure.com/b/APK/app.one?versionCode=2",
            ApkDownloadUrl.httpsFile("http://d.apkpure.com/b/APK/app.one?versionCode=2"),
        )
        assertNull(ApkDownloadUrl.httpsFile("https://d.apkpure.com/b/XAPK/app.one?version=latest"))
        assertNull(ApkDownloadUrl.httpsFile("ftp://evil.example/app.apk"))
    }

    @Test
    fun acceptsGithubAndFdroidApk() {
        assertEquals(
            "https://github.com/o/r/releases/download/v1/app.apk",
            ApkDownloadUrl.httpsFile("https://github.com/o/r/releases/download/v1/app.apk"),
        )
        assertEquals(
            "https://f-droid.org/repo/org.example_1.apk",
            ApkDownloadUrl.httpsFile("https://f-droid.org/repo/org.example_1.apk"),
        )
    }

    @Test
    fun acceptsVendorFdroidHosts() {
        assertEquals(
            "https://microg.org/fdroid/repo/com.google.android.gms_1.apk",
            ApkDownloadUrl.httpsFile("https://microg.org/fdroid/repo/com.google.android.gms_1.apk"),
        )
        assertEquals(
            "https://fdroid.iode.tech/repo/app_1.apk",
            ApkDownloadUrl.httpsFile("https://fdroid.iode.tech/repo/app_1.apk"),
        )
    }

    @Test
    fun acceptsPlayCdnWithoutApkSuffix() {
        assertEquals(
            "https://redirector.gvt1.com/edgedl/android/market/app.one",
            ApkDownloadUrl.httpsFile("https://redirector.gvt1.com/edgedl/android/market/app.one"),
        )
    }
}
