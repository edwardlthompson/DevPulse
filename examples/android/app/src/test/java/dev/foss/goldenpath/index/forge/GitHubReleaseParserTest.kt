package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubReleaseParserTest {
    @Test
    fun parseReadsAssetNameAndDate() {
        val json = """
            [{"name":"0.3","tag_name":"v0.3","body":"notes","assets":[{"name":"uk.org.platitudes.wipefiles_0.3.apk","browser_download_url":"https://github.com/plat/wipefiles/releases/download/v0.3/uk.org.platitudes.wipefiles_0.3.apk"}],"published_at":"2024-06-01T00:00:00Z"}]
        """.trimIndent()
        val hit = GitHubReleaseParser.firstWithPackage("uk.org.platitudes.wipefiles", json)
        assertEquals(GitHubRepoParser.isoMs("2024-06-01T00:00:00Z"), hit?.publishedAtMs)
        assertTrue(ForgePackageEvidence.inText("uk.org.platitudes.wipefiles", hit?.haystack.orEmpty()))
        assertEquals("notes", hit?.notes)
        assertEquals(
            "https://github.com/plat/wipefiles/releases/download/v0.3/uk.org.platitudes.wipefiles_0.3.apk",
            hit?.apkUrl,
        )
    }

    @Test
    fun missingBodyYieldsNoNotes() {
        val json = """[{"name":"1.0","tag_name":"v1.0","assets":[{"name":"com.example.app.apk"}]}]"""
        val hit = GitHubReleaseParser.firstWithPackage("com.example.app", json)
        assertNull(hit?.notes)
    }

    @Test
    fun emptyAndUnrelatedReleasesAreSafe() {
        assertEquals(emptyList<GitHubReleaseRecord>(), GitHubReleaseParser.parse("[]"))
        assertEquals(emptyList<GitHubReleaseRecord>(), GitHubReleaseParser.parse("{}"))
        assertNull(GitHubReleaseParser.firstWithPackage("com.example.wipefiles", """[{"name":"1.0","assets":[{"name":"WiseTimer.apk"}]}]"""))
    }

    @Test
    fun prereleaseAndFilenameRegexFilterAssets() {
        val json = """
            [
              {"name":"com.example.app","prerelease":true,"tag_name":"v1-pre",
               "assets":[{"browser_download_url":"https://github.com/a/b/releases/download/v1/com.example.app-pre.apk"}]},
              {"name":"com.example.app","prerelease":false,"tag_name":"v1",
               "assets":[{"browser_download_url":"https://github.com/a/b/releases/download/v1/com.example.app-arm64.apk"}]}
            ]
        """.trimIndent()
        assertNull(GitHubReleaseParser.firstWithPackage("com.example.app", json, includePrereleases = false, apkRegex = "pre"))
        val arm = GitHubReleaseParser.firstWithPackage("com.example.app", json, includePrereleases = false, apkRegex = "arm64")
        assertEquals(
            "https://github.com/a/b/releases/download/v1/com.example.app-arm64.apk",
            arm?.apkUrl,
        )
        val overlong = GitHubReleaseParser.firstWithPackage(
            "com.example.app",
            json,
            apkRegex = "a".repeat(GithubAppOptCodec.MAX_REGEX + 1),
        )
        assertEquals(
            "https://github.com/a/b/releases/download/v1/com.example.app-pre.apk",
            overlong?.apkUrl,
        )
    }

    @Test
    fun firstApkAllowsFilenameWithoutPackage() {
        val json = """
            [{"tag_name":"v2.0","assets":[{"browser_download_url":"https://github.com/a/b/releases/download/v2.0/app-release.apk"}]}]
        """.trimIndent()
        assertNull(GitHubReleaseParser.firstWithPackage("com.example.app", json))
        val hit = GitHubReleaseParser.firstApk(json)
        assertEquals("v2.0", hit?.versionName)
        assertEquals(
            "https://github.com/a/b/releases/download/v2.0/app-release.apk",
            GitHubReleasePick.bound("com.example.app", json)?.apkUrl,
        )
    }

    @Test
    fun obtainiumFlavorsSelectCorrectApkForPackage() {
        val json = """
            [{
              "tag_name":"v1.6.14",
              "assets":[
                {"browser_download_url":"https://github.com/ImranR98/Obtainium/releases/download/v1.6.14/app-arm64-v8a-fdroid-release.apk"},
                {"browser_download_url":"https://github.com/ImranR98/Obtainium/releases/download/v1.6.14/app-arm64-v8a-release.apk"},
                {"browser_download_url":"https://github.com/ImranR98/Obtainium/releases/download/v1.6.14/app-release.apk"},
                {"browser_download_url":"https://github.com/ImranR98/Obtainium/releases/download/v1.6.14/app-x86_64-release.apk"}
              ]
            }]
        """.trimIndent()

        val mainAppHit = GitHubReleasePick.bound("dev.imranr.obtainium", json)
        assertEquals(
            "https://github.com/ImranR98/Obtainium/releases/download/v1.6.14/app-arm64-v8a-release.apk",
            mainAppHit?.apkUrl,
        )

        val fdroidAppHit = GitHubReleasePick.bound("dev.imranr.obtainium.fdroid", json)
        assertEquals(
            "https://github.com/ImranR98/Obtainium/releases/download/v1.6.14/app-arm64-v8a-fdroid-release.apk",
            fdroidAppHit?.apkUrl,
        )
    }
}
