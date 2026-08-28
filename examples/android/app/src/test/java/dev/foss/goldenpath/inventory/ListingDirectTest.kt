package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ListingDirectTest {
    @Before
    fun reset() {
        UpdateArtifactMemory.clear()
    }

    @Test
    fun listedFdroidUsesPackagePageFile() {
        val html = """<a href="/repo/org.btcmap_42.apk">apk</a>"""
        val artifact = ListingDirect.resolve(
            "org.btcmap",
            RemoteReleasedSource.Fdroid,
            pageUrl = "https://f-droid.org/packages/org.btcmap/",
            fetchPage = { html },
        )
        assertEquals("https://f-droid.org/repo/org.btcmap_42.apk", artifact?.downloadUrl)
        assertEquals(RemoteReleasedSource.Fdroid, artifact?.source)
    }

    @Test
    fun rememberedSourceWinsOverPage() {
        UpdateArtifactMemory.add(
            UpdateArtifact("org.btcmap", RemoteReleasedSource.Fdroid, "https://f-droid.org/repo/org.btcmap_1.apk"),
        )
        val artifact = ListingDirect.resolve(
            "org.btcmap",
            RemoteReleasedSource.Fdroid,
            fetchPage = { error("should not fetch") },
        )
        assertEquals("https://f-droid.org/repo/org.btcmap_1.apk", artifact?.downloadUrl)
    }

    @Test
    fun otherSourceMemoryDoesNotSatisfyTap() {
        UpdateArtifactMemory.add(
            UpdateArtifact("org.btcmap", RemoteReleasedSource.ApkPure, "https://d.apkpure.com/b/APK/org.btcmap"),
        )
        assertNull(
            ListingDirect.resolve(
                "org.btcmap",
                RemoteReleasedSource.Fdroid,
                fetchPage = { "" },
            ),
        )
    }

    @Test
    fun forgeUsesReleaseAssetOnListedRepo() {
        val json = """
            [{"name":"org.btcmap","tag_name":"v1","assets":[{"browser_download_url":"https://github.com/btc/map/releases/download/v1/org.btcmap.apk"}]}]
        """.trimIndent()
        val artifact = ListingDirect.resolve(
            "org.btcmap",
            RemoteReleasedSource.Forge,
            pageUrl = "https://github.com/btc/map/releases",
            fetchReleases = { json },
        )
        assertEquals("https://github.com/btc/map/releases/download/v1/org.btcmap.apk", artifact?.downloadUrl)
    }

    @Test
    fun forgeUsesApkWhenFilenameOmitsPackage() {
        val json = """
            [{"tag_name":"v2","assets":[{"browser_download_url":"https://github.com/o/r/releases/download/v2/app-release.apk"}]}]
        """.trimIndent()
        val artifact = ListingDirect.resolve(
            "dev.imranr.obtainium",
            RemoteReleasedSource.Forge,
            pageUrl = "https://github.com/ImranR98/Obtainium/releases",
            fetchReleases = { json },
        )
        assertEquals(
            "https://github.com/o/r/releases/download/v2/app-release.apk",
            artifact?.downloadUrl,
        )
    }

    @Test
    fun forgeDirectApkSkipsReleasesAndRejectsLinkLocal() {
        val href = "https://github.com/o/r/releases/download/v1/app.apk"
        val artifact = ListingDirect.resolve(
            "org.app",
            RemoteReleasedSource.Forge,
            fetchReleases = { error("no releases") },
            directApkUrl = href,
        )
        assertEquals(href, artifact?.downloadUrl)
        assertNull(
            ListingDirect.resolve(
                "org.app",
                RemoteReleasedSource.Forge,
                fetchReleases = { null },
                directApkUrl = "http://169.254.0.1/x.apk",
            ),
        )
    }

    @Test
    fun aptoideAndPlayUseInjectedResolvers() {
        val aptoide = UpdateArtifact("a", RemoteReleasedSource.Aptoide, "https://pool.apk.aptoide.com/a.apk")
        val play = UpdateArtifact("a", RemoteReleasedSource.Play, "https://redirector.gvt1.com/edgedl/android/market/a")
        assertEquals(aptoide, ListingDirect.resolve("a", RemoteReleasedSource.Aptoide, resolveAptoide = { aptoide }))
        assertEquals(play, ListingDirect.resolve("a", RemoteReleasedSource.Play, resolvePlay = { play }))
    }

    @Test
    fun mirrorUsesDownloadPhpFromPage() {
        val artifact = ListingDirect.resolve(
            "app.listed",
            RemoteReleasedSource.ApkMirror,
            pageUrl = "https://www.apkmirror.com/apk/listed/",
            fetchPage = {
                """<a href="/wp-content/themes/APKMirror/download.php?id=42">apk</a>"""
            },
        )
        assertEquals(
            "https://www.apkmirror.com/wp-content/themes/APKMirror/download.php?id=42",
            artifact?.downloadUrl,
        )
    }

    @Test
    fun mirrorAndEmptyHtmlFailHonestly() {
        assertNull(
            ListingDirect.resolve(
                "a",
                RemoteReleasedSource.ApkMirror,
                pageUrl = "https://www.apkmirror.com/apk/a/",
                fetchPage = { "no download here" },
            ),
        )
        assertNull(ListingDirect.resolve("org.btcmap", RemoteReleasedSource.Fdroid, fetchPage = { "no apk here" }))
        assertNull(ListingDirect.resolve("  ", RemoteReleasedSource.Fdroid, fetchPage = { "<a href='x.apk'>" }))
    }

    @Test
    fun archiveDoesNotUseOfficialPackagesPage() {
        var fetched = ""
        assertNull(
            ListingDirect.resolve(
                "org.btcmap",
                RemoteReleasedSource.Archive,
                pageUrl = "https://f-droid.org/packages/org.btcmap/",
                fetchPage = { url -> fetched = url; "<a href='/archive/org.btcmap_1.apk'>apk</a>" },
            ),
        )
        assertEquals("", fetched)
    }
}
