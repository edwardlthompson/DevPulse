package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateInventoryTest {
    @Test
    fun linksOnlyWhenHighestIsNewer() {
        val pick = RemoteReleaseRollup.from(
            listOf(
                RemoteReleaseOffer(RemoteReleasedSource.Fdroid, 1L, "1.0", "https://f-droid.org/packages/a/"),
                RemoteReleaseOffer(RemoteReleasedSource.Forge, 1L, "1.0", "https://github.com/a/a"),
            ),
        )
        assertTrue(UpdateInventory.linksFor("1.0", pick).isEmpty())
        assertTrue(UpdateInventory.listingsFor(pick).any { it.source == RemoteReleasedSource.Fdroid })
        assertEquals(2, UpdateInventory.linksFor("0.9", pick).size)
    }

    @Test
    fun listingsIncludeEveryStandardStore() {
        val pick = RemoteReleaseRollup.from(
            listOf(
                RemoteReleaseOffer(RemoteReleasedSource.Play, 2L, "1.5", UpdateUrls.play("a"), listed = false),
                RemoteReleaseOffer(RemoteReleasedSource.Aptoide, 3L, "1.2", UpdateUrls.aptoide("a")),
                RemoteReleaseOffer(RemoteReleasedSource.Fdroid, 1L, "2.0", "https://f-droid.org/packages/a/"),
                RemoteReleaseOffer(RemoteReleasedSource.Forge, pageUrl = "https://github.com/a/a/releases"),
            ),
        )
        val listings = UpdateInventory.listingsFor(pick)
        assertTrue(ListingChannels.STANDARD.all { source -> listings.any { it.source == source } })
        assertTrue(listings.any { it.source == RemoteReleasedSource.Forge && it.listed })
        assertEquals(RemoteReleasedSource.Fdroid, listings.first().source)
        val play = listings.first { it.source == RemoteReleasedSource.Play }
        assertFalse(play.listed)
        assertTrue(play.known)
        assertNull(play.url)
        assertFalse(UpdateInventory.canOpen(play))
        assertTrue(UpdateInventory.canOpen(listings.first()))
    }

    @Test
    fun allNewerListedSourcesAreUpdateLinks() {
        val pick = RemoteReleaseRollup.from(
            listOf(
                RemoteReleaseOffer(RemoteReleasedSource.Play, 2L, "1.5", UpdateUrls.play("a")),
                RemoteReleaseOffer(RemoteReleasedSource.Fdroid, 1L, "2.0", "https://f-droid.org/packages/a/"),
                RemoteReleaseOffer(RemoteReleasedSource.Forge, 1L, "2.0", "https://github.com/a/a/releases"),
            ),
        )
        val links = UpdateInventory.linksFor("1.0", pick)
        assertEquals(
            listOf(RemoteReleasedSource.Fdroid, RemoteReleasedSource.Forge, RemoteReleasedSource.Play),
            links.map { it.source },
        )
        assertEquals(2L, links.first { it.source == RemoteReleasedSource.Play }.releasedAtMs)
        assertEquals("1.5", links.first { it.source == RemoteReleasedSource.Play }.versionName)
    }

    @Test
    fun missingPlayOfferIsUnknownNotDelisted() {
        val pick = RemoteReleaseRollup.from(
            listOf(RemoteReleaseOffer(RemoteReleasedSource.Fdroid, 1L, "1.0", "https://f-droid.org/packages/a/")),
        )
        val play = UpdateInventory.listingsFor(pick).first { it.source == RemoteReleasedSource.Play }
        assertFalse(play.listed)
        assertFalse(play.known)
        assertNull(play.url)
        assertFalse(UpdateInventory.canOpen(play))
    }

    @Test
    fun unlistedPlayNeverBecomesALink() {
        val pick = RemoteReleaseRollup.from(
            listOf(
                RemoteReleaseOffer(RemoteReleasedSource.Play, 1L, "3.0", UpdateUrls.play("a"), listed = false),
                RemoteReleaseOffer(RemoteReleasedSource.Fdroid, 1L, "3.0", "https://f-droid.org/packages/a/"),
            ),
        )
        val links = UpdateInventory.linksFor("1.0", pick)
        assertEquals(1, links.size)
        assertEquals(RemoteReleasedSource.Fdroid, links.single().source)
        assertTrue(UpdateInventory.listingsFor(pick).size >= ListingChannels.STANDARD.size)
    }
}
