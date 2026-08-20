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
}
