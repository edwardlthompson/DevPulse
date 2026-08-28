package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.ListingDirect
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.UpdateArtifactMemory
import dev.foss.goldenpath.inventory.UpdateInventory
import dev.foss.goldenpath.inventory.UpdateLink
import dev.foss.goldenpath.inventory.UpdateNotesMemory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GitHubHintReleaseTest {
    @Before
    fun reset() {
        UpdateNotesMemory.clear()
        UpdateArtifactMemory.clear()
        GitHubSearchPace.reset()
        ForgeRateLimit.reset()
    }

    @Test
    fun boundRepoReadsTagWhenApkOmitsPackage() {
        var searches = 0
        var releases = 0
        val json = """
            [{"tag_name":"v1.2.0","body":"fix","published_at":"2024-06-01T00:00:00Z",
              "assets":[{"browser_download_url":"https://github.com/ImranR98/Obtainium/releases/download/v1.2.0/app-release.apk"}]}]
        """.trimIndent()
        val client = object : GitHubSearchClient, GitHubReleaseClient {
            override fun searchRepos(query: String): GitHubSearchPage {
                searches += 1
                return GitHubSearchPage(200, """{"items":[]}""")
            }

            override fun listReleases(ownerRepo: String): GitHubSearchPage {
                releases += 1
                return GitHubSearchPage(200, json)
            }
        }
        val offer = GitHubScan.toOffer(
            "dev.imranr.obtainium",
            "Obtainium",
            client,
            hint = GithubHint("ImranR98/Obtainium", 1L, "1.0"),
        )
        assertTrue(offer.listed)
        assertEquals("v1.2.0", offer.versionName)
        assertEquals(GitHubRepoParser.isoMs("2024-06-01T00:00:00Z"), offer.ms)
        assertEquals(0, searches)
        assertEquals(1, releases)
        assertEquals(
            "https://github.com/ImranR98/Obtainium/releases/download/v1.2.0/app-release.apk",
            UpdateArtifactMemory.forSource("dev.imranr.obtainium", RemoteReleasedSource.Forge)?.downloadUrl,
        )
    }

    @Test
    fun httpFailureKeepsHintListing() {
        val offer = GitHubScan.toOffer(
            "dev.imranr.obtainium",
            "Obtainium",
            GitHubSearchClient { GitHubSearchPage(200, """{"items":[]}""") },
            GitHubReleaseClient { GitHubSearchPage(403, "") },
            pause = {},
            hint = GithubHint("ImranR98/Obtainium", 9L, "1.0"),
        )
        assertTrue(offer.listed)
        assertEquals("1.0", offer.versionName)
        assertEquals(9L, offer.ms)
        assertEquals("https://github.com/ImranR98/Obtainium/releases", offer.pageUrl)
    }

    @Test
    fun listingTapFindsApkWhenFilenameOmitsPackage() {
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
    fun githubTagShowsHasUpdateAgainstInstalled() {
        val app = sampleHintApp("1.1.0", "v1.2.0")
        assertTrue(UpdateInventory.hasUpdate(app))
        assertFalse(UpdateInventory.hasUpdate(sampleHintApp("1.2.0", "v1.2.0")))
    }

    private fun sampleHintApp(installed: String, remote: String) =
        InstalledApp(
            packageName = "dev.imranr.obtainium",
            label = "Obtainium",
            versionName = installed,
            versionCode = 1L,
            lastUpdateTimeMs = 1L,
            firstInstallTimeMs = 1L,
            minSdk = 26,
            targetSdk = 37,
            isSystemApp = false,
            latestListings = listOf(
                UpdateLink(
                    RemoteReleasedSource.Forge,
                    url = "https://github.com/ImranR98/Obtainium/releases",
                    versionName = remote,
                    listed = true,
                    known = true,
                ),
            ),
        )
}
