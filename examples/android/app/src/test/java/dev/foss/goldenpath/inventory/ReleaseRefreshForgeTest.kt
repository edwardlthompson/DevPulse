package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.aptoide.AptoideMetaFetcher
import dev.foss.goldenpath.index.fdroid.FdroidAppRecord
import dev.foss.goldenpath.index.fdroid.FdroidIndexFetcher
import dev.foss.goldenpath.index.fdroid.FdroidRepo
import dev.foss.goldenpath.index.fdroid.FdroidRepoKind
import dev.foss.goldenpath.index.forge.GitHubReleaseClient
import dev.foss.goldenpath.index.forge.GitHubSearchClient
import dev.foss.goldenpath.index.forge.GitHubSearchPage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReleaseRefreshForgeTest {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
        UpdateNotesMemory.clear()
        UpdateArtifactMemory.clear()
        RefreshTrace.emit = {}
    }

    @Test
    fun fdroidSourceCodeDoesNotSeedListedForge() {
        val rec = FdroidAppRecord("com.example.wipefiles", 1L, "https://github.com/someone/WiseTimer", "official")
        val offers = ReleaseRefresh.fdroidOffers(listOf(rec), setOf(rec.packageName)).getValue(rec.packageName)
        assertTrue(offers.none { it.source == RemoteReleasedSource.Forge })
        assertTrue(offers.none { it.pageUrl.orEmpty().contains("WiseTimer") })
    }

    @Test
    fun fdroidWhatsNewFillsNotes() {
        val rec = FdroidAppRecord("app.one", 1L, null, "official", whatsNew = "Crash fix")
        ReleaseRefresh.fdroidOffers(listOf(rec), setOf(rec.packageName))
        assertEquals("Crash fix", UpdateNotesMemory.get("app.one")?.text)
        assertEquals(RemoteReleasedSource.Fdroid, UpdateNotesMemory.get("app.one")?.source)
    }

    @Test
    fun izzyGithubHintListsWithoutGithubHttp() {
        val hits = intArrayOf(0, 0)
        val pkg = "uk.org.platitudes.wipefiles"
        val json = """{"apps":[{"packageName":"$pkg","lastUpdated":1700000000000,"suggestedVersionName":"0.3","sourceCode":"https://github.com/peterhearty/WipeFiles"}]}"""
        val forge = runForge(pkg, json, izzy, countClient(hits)).getValue(pkg).offers.single { it.source == RemoteReleasedSource.Forge }
        assertTrue(forge.listed)
        assertEquals("https://github.com/peterhearty/WipeFiles/releases", forge.pageUrl)
        assertEquals(1_700_000_000_000L, forge.ms)
        assertEquals("0.3", forge.versionName)
        assertEquals(0, hits[0])
        assertEquals(0, hits[1])
    }

    @Test
    fun noHintSearchOffStaysUnknownWithoutHttp() {
        val hits = intArrayOf(0, 0)
        val pkg = "com.example.wipefiles"
        val json = """{"apps":[{"packageName":"$pkg","lastUpdated":1610000000000}]}"""
        val forge = runForge(pkg, json, official, countClient(hits)).getValue(pkg).offers.single { it.source == RemoteReleasedSource.Forge }
        assertFalse(forge.listed)
        assertFalse(forge.known)
        assertEquals(0, hits[0])
        assertEquals(0, hits[1])
    }

    @Test
    fun noHintEmptySearchIsKnownMissWhenOptInOn() {
        val pkg = "com.example.wipefiles"
        val json = """{"apps":[{"packageName":"$pkg","lastUpdated":1610000000000}]}"""
        val client = GitHubSearchClient { GitHubSearchPage(200, """{"items":[]}""") }
        val pick = runForge(pkg, json, official, client, searchUnknowns = true).getValue(pkg)
        val forge = pick.offers.single { it.source == RemoteReleasedSource.Forge }
        assertFalse(forge.listed)
        assertTrue(forge.known)
        val link = UpdateInventory.listingsFor(pick).first { it.source == RemoteReleasedSource.Forge }
        assertFalse(link.listed)
        assertTrue(link.known)
        assertEquals(null, link.url)
    }

    @Test
    fun officialHintBeatsVerifiedStore() {
        val pkg = "uk.org.platitudes.wipefiles"
        val rec = FdroidAppRecord(pkg, 1L, "https://github.com/plat/wipefiles", "official")
        val known = ReleaseRefresh.githubHints(listOf(rec), setOf(pkg), mapOf(pkg to "old/repo"))
        assertEquals("plat/wipefiles", known[pkg]?.ownerRepo)
        val pasted = ReleaseRefresh.githubHints(
            listOf(rec),
            setOf(pkg),
            mapOf(pkg to "old/repo"),
            pasted = mapOf(pkg to "https://github.com/paste/repo"),
        )
        assertEquals("paste/repo", pasted[pkg]?.ownerRepo)
    }

    @Test
    fun mergeOfferReplacesSameSource() {
        val bag = mutableListOf(RemoteReleaseOffer(RemoteReleasedSource.Forge, pageUrl = "https://github.com/a/b/releases"))
        ReleaseRefreshWaves.mergeOffer(bag, RemoteReleaseOffer(RemoteReleasedSource.Forge, listed = false, known = true))
        assertEquals(1, bag.size)
        assertFalse(bag.single().listed)
        assertTrue(bag.single().known)
        assertEquals(null, bag.single().pageUrl)
    }

    private val official = FdroidRepo("official", FdroidRepoKind.Official, "https://example/index.json", true)
    private val izzy = FdroidRepo("izzy", FdroidRepoKind.Izzy, "https://example/izzy.json", true)

    private fun countClient(hits: IntArray) = object : GitHubSearchClient, GitHubReleaseClient {
        override fun searchRepos(query: String): GitHubSearchPage {
            hits[0] += 1
            return GitHubSearchPage(200, """{"items":[]}""")
        }

        override fun listReleases(ownerRepo: String): GitHubSearchPage {
            hits[1] += 1
            return GitHubSearchPage(200, "[]")
        }
    }

    private fun runForge(
        pkg: String,
        json: String,
        repo: FdroidRepo,
        client: GitHubSearchClient,
        searchUnknowns: Boolean = false,
    ) = ReleaseRefresh.run(
        listOf(sampleApp(pkg, "Wipe Files", installedAtMs = 1_600_000_000_000L)),
        listOf(repo),
        false,
        FdroidIndexFetcher { Result.success(json.toByteArray()) },
        AptoideMetaFetcher { Result.success("") },
        1_720_000_000_000L,
        gitHubClient = client,
        sleepMs = {},
        searchUnknowns = searchUnknowns,
    )
}
