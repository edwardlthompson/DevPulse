package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.inventory.RefreshSkip
import dev.foss.goldenpath.inventory.UpdateNotesMemory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GitHubScanTest {
    @Before
    fun resetPace() {
        GitHubSearchPace.reset()
        RefreshSkip.reset()
    }

    @Test
    fun pickRejectsOwnerTailWithoutFullPackage() {
        val candidates = listOf(
            ForgeCandidate(ForgeHost.GitHub, "other/notes", null, "Notes", null, null, false),
            ForgeCandidate(ForgeHost.GitHub, "TeamNewPipe/NewPipe", null, "NewPipe", 2L, null, false),
        )
        assertNull(GitHubScan.pick("org.schabi.newpipe", "NewPipe", candidates))
    }

    @Test
    fun pickRejectsWiseTimerForWipeFiles() {
        val candidates = listOf(
            ForgeCandidate(ForgeHost.GitHub, "someone/WiseTimer", null, "WiseTimer", 2L, null, false),
        )
        assertNull(GitHubScan.pick("com.example.wipefiles", "Wipe Files", candidates))
    }

    @Test
    fun pickDoesNotGuessFirstNonArchived() {
        val candidates = listOf(
            ForgeCandidate(ForgeHost.GitHub, "someone/android-notes", null, "Notes", null, null, false),
            ForgeCandidate(ForgeHost.GitHub, "other/foo", null, "Foo", 1L, null, false),
        )
        assertNull(GitHubScan.pick("com.instagram.android", "Instagram", candidates))
    }

    @Test
    fun wiseTimerReleasesWithoutPackageNotListed() {
        val search = GitHubSearchClient {
            GitHubSearchPage(200, """{"items":[{"full_name":"someone/WiseTimer","name":"WiseTimer","archived":false}]}""")
        }
        val releases = GitHubReleaseClient {
            GitHubSearchPage(200, """[{"name":"1.0","tag_name":"v1.0","assets":[{"name":"WiseTimer.apk"}]}]""")
        }
        val offer = GitHubScan.toOffer("com.example.wipefiles", "Wipe Files", search, releases, searchUnknowns = true)
        assertFalse(offer.listed)
        assertTrue(offer.known)
    }

    @Test
    fun releaseAssetWithPackageIsListed() {
        val search = GitHubSearchClient {
            GitHubSearchPage(200, """{"items":[{"full_name":"platitudes/WipeFiles","name":"Wipe Files","archived":false}]}""")
        }
        val releases = GitHubReleaseClient {
            GitHubSearchPage(200, """[{"name":"0.3","tag_name":"v0.3","body":"Fixed crash","assets":[{"name":"uk.org.platitudes.wipefiles_0.3.apk"}]}]""")
        }
        UpdateNotesMemory.clear()
        val offer = GitHubScan.toOffer("uk.org.platitudes.wipefiles", "Wipe Files", search, releases, searchUnknowns = true)
        assertTrue(offer.listed)
        assertEquals("https://github.com/platitudes/WipeFiles/releases", offer.pageUrl)
        assertEquals("v0.3", offer.versionName)
        assertEquals("Fixed crash", UpdateNotesMemory.get("uk.org.platitudes.wipefiles")?.text)
    }

    @Test
    fun descriptionPackageWithoutReleaseAssetNotListed() {
        val client = GitHubSearchClient {
            GitHubSearchPage(200, """{"items":[{"full_name":"TeamNewPipe/NewPipe","description":"org.schabi.newpipe"}]}""")
        }
        val offer = GitHubScan.toOffer("org.schabi.newpipe", "NewPipe", client, searchUnknowns = true)
        assertFalse(offer.listed)
        assertTrue(offer.known)
        assertNull(offer.pageUrl)
    }

    @Test
    fun rateLimitUnknownEmptySearchKnownMiss() {
        val limited = GitHubScan.toOffer("app.x", "X", GitHubSearchClient { GitHubSearchPage(403, "") }, searchUnknowns = true)
        val empty = GitHubScan.toOffer("app.x", "X", GitHubSearchClient { GitHubSearchPage(200, """{"items":[]}""") }, searchUnknowns = true)
        assertFalse(limited.listed)
        assertFalse(limited.known)
        assertFalse(empty.listed)
        assertTrue(empty.known)
        assertNull(GitHubScan.pick("app.x", "X", emptyList()))
    }

    @Test
    fun releaseFetch429Unknown() {
        val search = GitHubSearchClient {
            GitHubSearchPage(200, """{"items":[{"full_name":"someone/WiseTimer","name":"WiseTimer","archived":false}]}""")
        }
        val offer = GitHubScan.toOffer("com.example.wipefiles", "Wipe Files", search, GitHubReleaseClient { GitHubSearchPage(429, "") }, searchUnknowns = true)
        assertFalse(offer.listed)
        assertFalse(offer.known)
    }

    @Test
    fun stopsReleaseWalkAfterFirst403() {
        ForgeRateLimit.reset()
        var releases = 0
        val search = GitHubSearchClient {
            GitHubSearchPage(
                200,
                """{"items":[
                  {"full_name":"a/one","name":"one","archived":false},
                  {"full_name":"a/two","name":"two","archived":false},
                  {"full_name":"a/three","name":"three","archived":false}
                ]}""",
            )
        }
        val offer = GitHubScan.toOffer(
            "app.x",
            "X",
            search,
            GitHubReleaseClient { releases += 1; GitHubSearchPage(403, "") },
            searchUnknowns = true,
        ) { }
        assertEquals(2, releases)
        assertFalse(offer.listed)
        assertFalse(offer.known)
    }

    @Test
    fun retriesOnceOn403ThenStaysUnknown() {
        var calls = 0
        val client = GitHubSearchClient { calls += 1; GitHubSearchPage(403, "") }
        val offer = GitHubScan.toOffer("app.x", "X", client, searchUnknowns = true) { }
        assertEquals(2, calls)
        assertFalse(offer.listed)
        assertFalse(offer.known)
    }
}
