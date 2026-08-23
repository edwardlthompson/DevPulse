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
        assertEquals(
            setOf(RemoteReleasedSource.Fdroid, RemoteReleasedSource.Forge),
            UpdateInventory.linksFor("0.9", pick).map { it.source }.toSet(),
        )
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
        val listedPlay = UpdateLink(
            RemoteReleasedSource.Play,
            UpdateUrls.play("a"),
            listed = true,
            known = true,
        )
        assertTrue(UpdateInventory.canOpen(listedPlay))
        val listedMirror = UpdateLink(
            RemoteReleasedSource.ApkMirror,
            "https://www.apkmirror.com/apk/a/",
            listed = true,
            known = true,
        )
        assertTrue(UpdateInventory.canOpen(listedMirror))
        val listedPure = UpdateLink(
            RemoteReleasedSource.ApkPure,
            "https://apkpure.com/search?q=a",
            listed = true,
            known = true,
        )
        assertTrue(UpdateInventory.canOpen(listedPure))
        assertFalse(
            UpdateInventory.canOpen(
                UpdateLink(RemoteReleasedSource.Fdroid, versionName = "1.0", listed = true),
                installedVersion = "2.0",
            ),
        )
        assertFalse(
            UpdateInventory.canOpen(
                UpdateLink(RemoteReleasedSource.Fdroid, versionName = "3.0", listed = true, minSdk = 35),
                deviceSdk = 34,
            ),
        )
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
            setOf(RemoteReleasedSource.Play, RemoteReleasedSource.Fdroid, RemoteReleasedSource.Forge),
            links.map { it.source }.toSet(),
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
        assertFalse(links.any { it.source == RemoteReleasedSource.Play })
        assertTrue(links.any { it.source == RemoteReleasedSource.Fdroid })
        assertTrue(UpdateInventory.listingsFor(pick).size >= ListingChannels.STANDARD.size)
    }
}
