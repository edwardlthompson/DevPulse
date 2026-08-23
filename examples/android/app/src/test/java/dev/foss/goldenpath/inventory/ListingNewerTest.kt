package dev.foss.goldenpath.inventory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ListingNewerTest {
    @Test
    fun unknownVersionsStayAllowed() {
        assertTrue(ListingNewer.allow(null, "1.0"))
        assertTrue(ListingNewer.allow("2.0", null))
        assertTrue(ListingNewer.allow("  ", "1.0"))
    }

    @Test
    fun olderOrSameIsRefused() {
        assertFalse(ListingNewer.allow("1.0", "1.0"))
        assertFalse(ListingNewer.allow("1.0", "2.0"))
        assertTrue(ListingNewer.allow("2.0", "1.0"))
    }
}
