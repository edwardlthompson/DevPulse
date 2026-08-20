package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubScanKnownTest {
    @Test
    fun izzyHintListsWithoutGithubHttp() {
        var searched = 0
        var listed = 0
        val search = GitHubSearchClient {
            searched += 1
            GitHubSearchPage(200, """{"items":[]}""")
        }
        val releases = GitHubReleaseClient {
            listed += 1
            GitHubSearchPage(200, "[]")
        }
        val offer = GitHubScan.toOffer(
            "uk.org.platitudes.wipefiles",
            "Wipe Files",
            search,
            releases,
            {},
            GithubHint("peterhearty/WipeFiles", 1_700_000_000_000L, "0.3"),
        )
        assertTrue(offer.listed)
        assertEquals("https://github.com/peterhearty/WipeFiles/releases", offer.pageUrl)
        assertEquals(1_700_000_000_000L, offer.ms)
        assertEquals("0.3", offer.versionName)
        assertEquals(0, searched)
        assertEquals(0, listed)
    }

    @Test
    fun noHintDoesNotSearchWhenOptInOff() {
        var searched = 0
        var listed = 0
        val search = GitHubSearchClient {
            searched += 1
            GitHubSearchPage(200, """{"items":[]}""")
        }
        val releases = GitHubReleaseClient {
            listed += 1
            GitHubSearchPage(200, "[]")
        }
        val offer = GitHubScan.toOffer("app.x", "X", search, releases)
        assertFalse(offer.listed)
        assertFalse(offer.known)
        assertNull(offer.pageUrl)
        assertEquals(0, searched)
        assertEquals(0, listed)
    }

    @Test
    fun noHintEmptySearchWhenOptInOn() {
        var searched = 0
        val search = GitHubSearchClient {
            searched += 1
            GitHubSearchPage(200, """{"items":[]}""")
        }
        val offer = GitHubScan.toOffer(
            "app.x",
            "X",
            search,
            searchUnknowns = true,
        )
        assertFalse(offer.listed)
        assertTrue(offer.known)
        assertEquals(1, searched)
    }
}
