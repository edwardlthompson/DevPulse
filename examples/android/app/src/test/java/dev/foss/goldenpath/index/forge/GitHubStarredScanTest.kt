package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.inventory.InstalledApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubStarredScanTest {
    @Test
    fun emptyPagesAreTimeoutWithZeroBinds() {
        val (matches, result) = GitHubStarredScan.bind(emptyList(), listOf(app("org.app")), emptyMap())
        assertTrue(matches.isEmpty())
        assertEquals(0, result.stars)
        assertEquals(0, result.matched)
        assertEquals(408, result.statusCode)
    }

    @Test
    fun emptyBodyBindsNothing() {
        val page = GitHubSearchPage(200, "")
        val (matches, result) = GitHubStarredScan.bind(listOf(page), listOf(app("org.app")), emptyMap())
        assertTrue(matches.isEmpty())
        assertEquals(0, result.matched)
        assertEquals(200, result.statusCode)
        assertEquals(5, GitHubStarredScan.MAX_PAGES)
    }

    @Test
    fun suffixVariantAutoBindsWithoutReleaseHttp() {
        val page = GitHubSearchPage(200, """[{"full_name":"ImranR98/Obtainium"}]""")
        val library = mapOf("dev.imranr.obtainium.fdroid" to "ImranR98/Obtainium")
        val (matches, result) = GitHubStarredScan.bind(
            listOf(page),
            listOf(app("dev.imranr.obtainium")),
            library,
        )
        assertEquals(1, matches.size)
        assertEquals(LibraryMatchRank.SuffixVariant, matches.single().rank)
        assertEquals(1, result.matched)
        assertEquals(1, result.stars)
    }

    private fun app(pkg: String) = InstalledApp(
        packageName = pkg,
        label = pkg,
        versionName = "1.0",
        versionCode = 1L,
        lastUpdateTimeMs = 1L,
        firstInstallTimeMs = 1L,
        minSdk = 26,
        targetSdk = 37,
        isSystemApp = false,
    )
}
