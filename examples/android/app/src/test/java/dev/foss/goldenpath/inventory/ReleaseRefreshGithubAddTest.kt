package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.fdroid.FdroidAppRecord
import dev.foss.goldenpath.index.forge.GitHubSearchClient
import dev.foss.goldenpath.index.forge.GitHubSearchPage
import dev.foss.goldenpath.index.forge.GithubHint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ReleaseRefreshGithubAddTest {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
        UpdateNotesMemory.clear()
        UpdateArtifactMemory.clear()
        RefreshTrace.emit = {}
    }

    @Test
    fun officialHintBeatsVerifiedStoreAndPastedWins() {
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
    fun obtainiumGithubIdAliasesFdroidLibraryWithoutVersion() {
        val githubPkg = "dev.imranr.obtainium"
        val fdroidPkg = "dev.imranr.obtainium.fdroid"
        val rec = FdroidAppRecord(
            fdroidPkg,
            9L,
            "https://github.com/ImranR98/Obtainium",
            "official",
            suggestedVersionName = "1.4.3",
        )
        val fromVerified = ReleaseRefresh.githubHints(
            listOf(rec),
            setOf(githubPkg),
            verified = mapOf(fdroidPkg to "ImranR98/Obtainium"),
        )
        val fromRecord = ReleaseRefresh.githubHints(listOf(rec), setOf(githubPkg))
        listOf(fromVerified, fromRecord).forEach { hints ->
            assertEquals("ImranR98/Obtainium", hints[githubPkg]?.ownerRepo)
            assertEquals(null, hints[githubPkg]?.versionName)
            assertEquals(null, hints[githubPkg]?.ms)
        }
    }

    @Test
    fun pastedWinsOverHarvestedForSamePackage() {
        val pkg = "org.app"
        val rec = FdroidAppRecord(pkg, 1L, "https://github.com/harvest/app", "official")
        val hints = ReleaseRefresh.githubHints(
            listOf(rec),
            setOf(pkg),
            verified = mapOf(pkg to "harvest/app"),
            pasted = mapOf(pkg to "https://github.com/paste/repo"),
        )
        assertEquals("paste/repo", hints[pkg]?.ownerRepo)
    }

    @Test
    fun aliasedHintListsForgeDespitePlayMiss() {
        val now = 1_720_000_000_000L
        val fetches = AtomicInteger(0)
        RemoteReleaseMemory.putAll(
            mapOf(
                "dev.imranr.obtainium" to RemoteReleaseRollup.from(
                    listOf(
                        RemoteReleaseOffer(
                            RemoteReleasedSource.Play,
                            listed = false,
                            known = true,
                            fetchedAtMs = now,
                        ),
                    ),
                ),
            ),
        )
        val offer = ReleaseRefreshProbes.github(
            "dev.imranr.obtainium",
            "Obtainium",
            GitHubSearchClient { fetches.incrementAndGet(); GitHubSearchPage(200, """{"items":[]}""") },
            hint = GithubHint("ImranR98/Obtainium"),
            nowMs = now + 1_000L,
        )
        assertTrue(offer.listed)
        assertEquals(null, offer.versionName)
        assertEquals(0, fetches.get())
        assertEquals("https://github.com/ImranR98/Obtainium/releases", offer.pageUrl)
    }
}
