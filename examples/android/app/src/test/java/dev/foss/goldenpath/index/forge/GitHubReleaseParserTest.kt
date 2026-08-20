package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubReleaseParserTest {
    @Test
    fun parseReadsAssetNameAndDate() {
        val json = """
            [{"name":"0.3","tag_name":"v0.3","body":"notes","assets":[{"name":"uk.org.platitudes.wipefiles_0.3.apk"}],"published_at":"2024-06-01T00:00:00Z"}]
        """.trimIndent()
        val hit = GitHubReleaseParser.firstWithPackage("uk.org.platitudes.wipefiles", json)
        assertEquals(GitHubRepoParser.isoMs("2024-06-01T00:00:00Z"), hit?.publishedAtMs)
        assertTrue(ForgePackageEvidence.inText("uk.org.platitudes.wipefiles", hit?.haystack.orEmpty()))
    }

    @Test
    fun emptyAndUnrelatedReleasesAreSafe() {
        assertEquals(emptyList<GitHubReleaseRecord>(), GitHubReleaseParser.parse("[]"))
        assertEquals(emptyList<GitHubReleaseRecord>(), GitHubReleaseParser.parse("{}"))
        assertNull(GitHubReleaseParser.firstWithPackage("com.example.wipefiles", """[{"name":"1.0","assets":[{"name":"WiseTimer.apk"}]}]"""))
    }
}
