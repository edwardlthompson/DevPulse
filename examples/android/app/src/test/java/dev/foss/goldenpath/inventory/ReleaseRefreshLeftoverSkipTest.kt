package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.forge.GitHubSearchClient
import dev.foss.goldenpath.index.forge.GitHubSearchPage
import dev.foss.goldenpath.index.forge.GithubHint
import dev.foss.goldenpath.index.forge.LeftoverKind
import dev.foss.goldenpath.index.forge.LeftoverSearchClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ReleaseRefreshLeftoverSkipTest {
    @Test
    fun leftoverSkippedWhenHintPresent() {
        val leftoverCalls = AtomicInteger(0)
        val leftover = LeftoverSearchClient { _, _ ->
            leftoverCalls.incrementAndGet()
            GitHubSearchPage(200, "[]")
        }
        val client = GitHubSearchClient { GitHubSearchPage(200, """{"items":[]}""") }
        ReleaseRefreshProbes.github(
            packageName = "org.app",
            label = "App",
            client = client,
            hint = GithubHint("acme/app"),
            leftover = leftover,
        )
        assertEquals(0, leftoverCalls.get())
    }

    @Test
    fun leftoverRunsWhenNoHint() {
        val leftoverCalls = AtomicInteger(0)
        val leftover = LeftoverSearchClient { kind, _ ->
            leftoverCalls.incrementAndGet()
            if (kind == LeftoverKind.GitLabSearch) {
                GitHubSearchPage(200, """[{"path_with_namespace":"acme/app","name":"app"}]""")
            } else {
                GitHubSearchPage(200, "[]")
            }
        }
        val client = GitHubSearchClient { GitHubSearchPage(200, """{"items":[]}""") }
        ReleaseRefreshProbes.github("org.app", "App", client, leftover = leftover)
        assertTrue(leftoverCalls.get() > 0)
    }
}
