package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.forge.GitHubSearchClient
import dev.foss.goldenpath.index.forge.GitHubSearchPage
import dev.foss.goldenpath.index.forge.GithubHint
import dev.foss.goldenpath.index.forge.LeftoverSearchClient
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ReleaseRefreshLeftoverSkipTest {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
    }

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
    fun leftoverUnlistedSearchesGithubNotGitlab() {
        val leftoverCalls = AtomicInteger(0)
        val searches = AtomicInteger(0)
        val leftover = LeftoverSearchClient { _, _ ->
            leftoverCalls.incrementAndGet()
            GitHubSearchPage(200, "[]")
        }
        val offer = ReleaseRefreshProbes.github(
            "org.app",
            "App",
            GitHubSearchClient { searches.incrementAndGet(); GitHubSearchPage(200, """{"items":[]}""") },
            leftover = leftover,
        )
        assertEquals(1, searches.get())
        assertEquals(0, leftoverCalls.get())
        assertEquals(true, offer.known)
        assertEquals(false, offer.listed)
    }
}
