package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateLinkRankTest {
    @Test
    fun nonPlayPicksBestRankedNewestSource() {
        val fdroid = RemoteReleasePick(10L, RemoteReleasedSource.Fdroid, "2", "https://f-droid.org/packages/a/")
        val forge = RemoteReleasePick(10L, RemoteReleasedSource.Forge, "2", "https://github.com/a/a")
        val aptoide = RemoteReleasePick(9L, RemoteReleasedSource.Aptoide, "1", UpdateUrls.aptoide("a"))
        val best = UpdateLinkRank.bestNonPlay(listOf(forge, aptoide, fdroid))
        assertEquals(RemoteReleasedSource.Fdroid, best?.source)
        assertTrue(best?.pageUrl?.contains("f-droid.org") == true)
    }

    @Test
    fun newerForgeBeatsOlderFdroid() {
        val fdroid = RemoteReleasePick(10L, RemoteReleasedSource.Fdroid, "1", "https://f-droid.org/packages/a/")
        val forge = RemoteReleasePick(20L, RemoteReleasedSource.Forge, "2", "https://github.com/a/a")
        assertEquals(RemoteReleasedSource.Forge, UpdateLinkRank.bestNonPlay(listOf(fdroid, forge))?.source)
    }
}
