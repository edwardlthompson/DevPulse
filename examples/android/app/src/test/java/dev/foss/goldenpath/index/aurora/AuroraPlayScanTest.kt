package dev.foss.goldenpath.index.aurora

import dev.foss.goldenpath.inventory.RemoteReleaseMemory
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuroraPlayScanTest {
    @Before
    fun reset() {
        RemoteReleaseMemory.clear()
    }

    @Test
    fun listedBecomesPlayOffer() {
        val offer = AuroraPlayScan.toOffer(
            "app.x",
            AuroraPlayApp(AuroraPlayStatus.Listed, "2.0", 1_700_000_000_000L),
        )
        assertTrue(offer.listed)
        assertEquals(RemoteReleasedSource.Play, offer.source)
        assertEquals("2.0", offer.versionName)
        assertEquals("https://play.google.com/store/apps/details?id=app.x", offer.pageUrl)
        assertEquals(1_700_000_000_000L, offer.ms)
    }

    @Test
    fun missingIsPlayDelist() {
        val offer = AuroraPlayScan.toOffer("app.gone", AuroraPlayApp(AuroraPlayStatus.Missing))
        assertFalse(offer.listed)
        assertTrue(offer.known)
        assertNull(offer.pageUrl)
    }

    @Test
    fun applyBatchStampsHitsAndMisses() {
        val details = AuroraPlayDetails { names ->
            names.associateWith { pkg ->
                if (pkg == "app.hit") AuroraPlayApp(AuroraPlayStatus.Listed, "1.0", 9L)
                else AuroraPlayApp(AuroraPlayStatus.Missing)
            }
        }
        val offers = AuroraPlayScan.applyBatch(listOf("app.hit", "app.gone"), details, 1_000L)
        assertEquals(true, offers.getValue("app.hit").listed)
        assertEquals(false, offers.getValue("app.gone").listed)
        assertEquals(true, offers.getValue("app.gone").known)
    }

    @Test
    fun parseUpdatedOnRejectsRelative() {
        assertNull(AuroraPlayLookup.parseUpdatedOn("2 days ago", 1_720_000_000_000L))
        assertEquals(
            true,
            AuroraPlayLookup.parseUpdatedOn("Jan 2, 2024", 1_720_000_000_000L) != null,
        )
    }

    @Test
    fun secondPassRelistsFirstWalkMiss() {
        var passes = 0
        val details = AuroraPlayDetails {
            passes += 1
            if (passes == 1) {
                mapOf("com.instagram.android" to AuroraPlayApp(AuroraPlayStatus.Missing))
            } else {
                mapOf("com.instagram.android" to AuroraPlayApp(AuroraPlayStatus.Listed, "1.0", 9L))
            }
        }
        val offers = AuroraPlayScan.applyBatch(listOf("com.instagram.android"), details, 1_000L)
        assertEquals(2, passes)
        assertEquals(true, offers.getValue("com.instagram.android").listed)
    }

    @Test
    fun emptyFieldsAreMissing() {
        assertEquals(AuroraPlayStatus.Missing, AuroraPlayLookup.fromFields(null, 0, null, 1L).status)
        assertEquals(AuroraPlayStatus.Listed, AuroraPlayLookup.fromFields("1.0", 0, null, 1L).status)
    }
}
