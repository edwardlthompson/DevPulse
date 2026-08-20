package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ListingChannelsTest {
    @Test
    fun mapsNamedRepos() {
        assertEquals(RemoteReleasedSource.Fdroid, ListingChannels.sourceForRepo("official"))
        assertEquals(RemoteReleasedSource.Archive, ListingChannels.sourceForRepo("archive"))
        assertEquals(RemoteReleasedSource.Izzy, ListingChannels.sourceForRepo("izzy"))
        assertEquals(RemoteReleasedSource.Guardian, ListingChannels.sourceForRepo("guardian"))
        assertEquals(RemoteReleasedSource.Calyx, ListingChannels.sourceForRepo("calyx"))
        assertEquals(RemoteReleasedSource.ExtraRepo, ListingChannels.sourceForRepo("custom"))
    }

    @Test
    fun padsSearchedChannelsThatMissed() {
        val hits = listOf(
            RemoteReleaseOffer(RemoteReleasedSource.Izzy, 1L, "2.0", "https://apt.izzysoft.de/fdroid/index/apk/a"),
        )
        val complete = ListingChannels.complete(
            hits,
            setOf(RemoteReleasedSource.Fdroid, RemoteReleasedSource.Izzy, RemoteReleasedSource.Play),
        )
        assertTrue(complete.any { it.source == RemoteReleasedSource.Izzy && it.listed })
        val official = complete.first { it.source == RemoteReleasedSource.Fdroid }
        assertFalse(official.listed)
        assertFalse(official.known)
        val play = complete.first { it.source == RemoteReleasedSource.Play }
        assertFalse(play.listed)
        assertFalse(play.known)
        assertEquals(3, complete.size)
    }

    @Test
    fun relabelTurnsIzzyUrlIntoIzzySource() {
        val offer = RemoteReleaseOffer(
            RemoteReleasedSource.ExtraRepo,
            pageUrl = "https://apt.izzysoft.de/fdroid/index/apk/a",
        )
        assertEquals(RemoteReleasedSource.Izzy, ListingChannels.relabel(offer).source)
    }
}
