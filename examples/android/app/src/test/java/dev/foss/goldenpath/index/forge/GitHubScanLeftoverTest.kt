package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.inventory.ListingMiss
import dev.foss.goldenpath.inventory.RefreshSkip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GitHubScanLeftoverTest {
    @Before
    fun reset() {
        GitHubSearchPace.reset()
        RefreshSkip.reset()
        ForgeRateLimit.reset()
    }

    @Test
    fun leftoverWalksToOlderApkWithoutPackageId() {
        val json = """
            [{"tag_name":"v1.2.0","assets":[]},
             {"tag_name":"v1.1.1","assets":[{"browser_download_url":"https://github.com/owner/continuum-calendar/releases/download/v1.1.1/app-release.apk"}]}]
        """.trimIndent()
        val hit = GitHubReleasePick.leftover(
            "org.continuumcalendar.app",
            "Continuum Calendar",
            "owner/continuum-calendar",
            json,
        )
        assertEquals("v1.1.1", hit?.versionName)
        assertTrue(hit?.apkUrl?.endsWith("app-release.apk") == true)
    }

    @Test
    fun slugMatchListsOlderApkWhenLatestHasNone() {
        val searchJson = """{"items":[
          {"full_name":"other/continuum-calendar","name":"continuum-calendar","pushed_at":"2025-01-01T00:00:00Z","archived":false},
          {"full_name":"owner/continuum-calendar","name":"continuum-calendar","pushed_at":"2026-09-15T00:00:00Z","archived":false}
        ]}"""
        val client = object : GitHubSearchClient, GitHubReleaseClient {
            override fun searchRepos(query: String): GitHubSearchPage =
                GitHubSearchPage(200, searchJson)

            override fun listReleases(ownerRepo: String): GitHubSearchPage =
                if (ownerRepo.startsWith("other/")) {
                    GitHubSearchPage(200, """[{"tag_name":"v9","assets":[]}]""")
                } else {
                    GitHubSearchPage(
                        200,
                        """[{"tag_name":"v1.2.0","assets":[]},{"tag_name":"v1.1.1","assets":[{"browser_download_url":"https://github.com/owner/continuum-calendar/releases/download/v1.1.1/app-release.apk"}]}]""",
                    )
                }
        }
        val offer = GitHubScan.toOffer(
            packageName = "org.continuumcalendar.app",
            label = "Continuum Calendar",
            client = client,
            searchUnknowns = true,
        )
        assertTrue(offer.listed)
        assertEquals("v1.1.1", offer.versionName)
        assertEquals("https://github.com/owner/continuum-calendar/releases", offer.pageUrl)
    }

    @Test
    fun emptySearchIsSearchedMiss() {
        val client = object : GitHubSearchClient, GitHubReleaseClient {
            override fun searchRepos(query: String): GitHubSearchPage =
                GitHubSearchPage(200, """{"items":[]}""")

            override fun listReleases(ownerRepo: String): GitHubSearchPage =
                GitHubSearchPage(200, "[]")
        }
        val offer = GitHubScan.toOffer(
            packageName = "org.none.app",
            label = "None",
            client = client,
            searchUnknowns = true,
        )
        assertFalse(offer.listed)
        assertTrue(offer.known)
        assertEquals(ListingMiss.Searched, offer.miss)
    }
}
