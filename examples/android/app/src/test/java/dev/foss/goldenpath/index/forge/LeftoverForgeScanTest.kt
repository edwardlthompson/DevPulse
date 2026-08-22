package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.inventory.RefreshOutletIds
import dev.foss.goldenpath.inventory.RefreshSkip
import java.net.SocketTimeoutException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LeftoverForgeScanTest {
    @Before
    fun resetLimits() {
        ForgeRateLimit.reset()
        RefreshSkip.reset()
    }

    @Test
    fun gitlabReleaseWithPackageLists() {
        val client = LeftoverSearchClient { kind, query ->
            when (kind) {
                LeftoverKind.GitLabSearch -> GitHubSearchPage(
                    200,
                    """[{"path_with_namespace":"acme/app","name":"app"}]""",
                )
                LeftoverKind.GitLabReleases -> {
                    assertEquals("acme/app", query)
                    GitHubSearchPage(200, """[{"tag_name":"1.0","description":"org.acme.app apk","released_at":"2024-06-01T00:00:00Z"}]""")
                }
                else -> GitHubSearchPage(200, "[]")
            }
        }
        val offer = LeftoverForgeScan.first("org.acme.app", "Acme", client)
        assertTrue(offer?.listed == true)
        assertEquals("https://gitlab.com/acme/app/-/releases", offer?.pageUrl)
    }

    @Test
    fun skipsGitlabAfterRepeatedTimeouts() {
        var gitlab = 0
        var codeberg = 0
        val client = LeftoverSearchClient { kind, _ ->
            if (kind == LeftoverKind.GitLabSearch) {
                gitlab += 1
                throw SocketTimeoutException("gitlab")
            }
            if (kind == LeftoverKind.CodebergSearch) codeberg += 1
            GitHubSearchPage(200, "[]")
        }
        repeat(ForgeRateLimit.LEFTOVER_SKIP_AFTER + 2) {
            LeftoverForgeScan.first("org.acme.app$it", "Acme", client)
        }
        assertEquals(ForgeRateLimit.LEFTOVER_SKIP_AFTER, gitlab)
        assertEquals(ForgeRateLimit.LEFTOVER_SKIP_AFTER, codeberg)
    }

    @Test
    fun emptySearchIsKnownMiss() {
        val offer = LeftoverForgeScan.first(
            "org.acme.app",
            "Acme",
            LeftoverSearchClient { _, _ -> GitHubSearchPage(200, "[]") },
        )
        assertEquals(false, offer?.listed)
        assertEquals(true, offer?.known)
    }

    @Test
    fun fromHintListsWithoutHttp() {
        val offer = LeftoverForgeScan.fromHint(
            "org.acme.app",
            LeftoverHint(ForgeHost.GitLab, "acme/app", 4L, "2.0"),
        )
        assertEquals("https://gitlab.com/acme/app/-/releases", offer.pageUrl)
        assertEquals(4L, offer.ms)
        assertEquals("2.0", offer.versionName)
        assertEquals(true, offer.listed)
    }

    @Test
    fun stopSkipsGitlab() {
        RefreshSkip.stop(RefreshOutletIds.LEFTOVER)
        var calls = 0
        LeftoverForgeScan.first("org.acme.app", "Acme", LeftoverSearchClient { _, _ -> calls += 1; GitHubSearchPage(200, "[]") })
        assertEquals(0, calls)
        assertNull(LeftoverForgeScan.first("org.acme.app", "Acme", LeftoverSearchClient { _, _ -> GitHubSearchPage(200, "[]") }))
    }
}
