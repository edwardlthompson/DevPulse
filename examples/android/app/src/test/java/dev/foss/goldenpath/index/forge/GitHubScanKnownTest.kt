package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.inventory.RefreshOutletIds
import dev.foss.goldenpath.inventory.RefreshSkip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class GitHubScanKnownTest {
    @Before
    fun reset() {
        GitHubSearchPace.reset()
        RefreshSkip.reset()
    }

    @Test
    fun stoppedOutletSkipsSearch() {
        RefreshSkip.stop(RefreshOutletIds.GITHUB)
        var calls = 0
        val offer = GitHubScan.toOffer(
            "app.x",
            "X",
            GitHubSearchClient { calls += 1; GitHubSearchPage(200, """{"items":[]}""") },
            searchUnknowns = true,
        )
        assertEquals(0, calls)
        assertFalse(offer.known)
    }
}
