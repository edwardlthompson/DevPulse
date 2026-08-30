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
        assertFalse(ListingNewer.allow("2026080501", "2026.08.05", 2026080501L))
        assertTrue(ListingNewer.allow("2026090101", "2026.08.05", 2026080501L))
        assertFalse(ListingNewer.allow("2023", "2023", 8L))
    }
}
