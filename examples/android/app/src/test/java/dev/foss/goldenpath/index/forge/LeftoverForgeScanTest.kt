package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LeftoverForgeScanTest {
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
}
