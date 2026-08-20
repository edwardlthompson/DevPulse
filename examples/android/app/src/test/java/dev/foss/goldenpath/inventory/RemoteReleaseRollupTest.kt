package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteReleaseRollupTest {
    @Test
    fun dateAndHighestVersionCanComeFromDifferentSources() {
        val pick = RemoteReleaseRollup.from(
            listOf(
                RemoteReleaseOffer(RemoteReleasedSource.Play, 200L, "1.0", UpdateUrls.play("app.x")),
                RemoteReleaseOffer(RemoteReleasedSource.Fdroid, 100L, "2.0", "https://f-droid.org/packages/app.x/"),
            ),
        )
        assertEquals(200L, pick.ms)
        assertEquals(RemoteReleasedSource.Play, pick.source)
        assertEquals("2.0", pick.versionName)
        assertEquals(RemoteReleasedSource.Fdroid, pick.versionSource)
    }

    @Test
    fun unlistedPlayDoesNotWinDateOrVersion() {
        val pick = RemoteReleaseRollup.from(
            listOf(
                RemoteReleaseOffer(RemoteReleasedSource.Play, 400L, "9.0", UpdateUrls.play("app.x"), listed = false),
                RemoteReleaseOffer(RemoteReleasedSource.Fdroid, 100L, "2.0", "https://f-droid.org/packages/app.x/"),
            ),
        )
        assertEquals(100L, pick.ms)
        assertEquals(RemoteReleasedSource.Fdroid, pick.source)
        assertEquals("2.0", pick.versionName)
        assertEquals(RemoteReleasedSource.Fdroid, pick.versionSource)
    }

    @Test
    fun delistedPlayDateUsedWhenNothingListed() {
        val pick = RemoteReleaseRollup.from(
            listOf(RemoteReleaseOffer(RemoteReleasedSource.Play, 400L, "1.2", UpdateUrls.play("app.x"), listed = false)),
        )
        assertEquals(400L, pick.ms)
        assertEquals(RemoteReleasedSource.Play, pick.source)
        assertEquals("1.2", pick.versionName)
    }

    @Test
    fun emptyUsableOffersAreNone() {
        val pick = RemoteReleaseRollup.from(
            listOf(RemoteReleaseOffer(RemoteReleasedSource.Play, listed = false)),
        )
        assertNull(pick.ms)
        assertEquals(RemoteReleasedSource.None, pick.source)
        assertNull(pick.versionName)
    }
}
