package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.R
import org.junit.Assert.assertEquals
import org.junit.Test

class InventoryCopyTest {
    @Test
    fun unlistedResDistinguishesDelistedFromUnknown() {
        assertEquals(R.string.inventory_listing_delisted, InventoryCopy.unlistedRes(known = true))
        assertEquals(R.string.inventory_listing_unknown, InventoryCopy.unlistedRes(known = false))
    }

    @Test
    fun listingMarkNeedsEvidence() {
        assertEquals(ListingMark.Listed, InventoryCopy.listingMark(listed = true, known = true))
        assertEquals(ListingMark.Missing, InventoryCopy.listingMark(listed = false, known = true))
        assertEquals(ListingMark.Unknown, InventoryCopy.listingMark(listed = true, known = false))
        assertEquals(ListingMark.Unknown, InventoryCopy.listingMark(listed = false, known = false))
        assertEquals(ListingMark.Unknown, InventoryCopy.listingMark(listed = null, known = null))
        assertEquals("✅ ", InventoryCopy.listingMarkPrefix(ListingMark.Listed))
        assertEquals("❌ ", InventoryCopy.listingMarkPrefix(ListingMark.Missing))
        assertEquals("❓ ", InventoryCopy.listingMarkPrefix(ListingMark.Unknown))
        assertEquals(R.string.inventory_listing_status_listed, InventoryCopy.listingMarkStatusRes(ListingMark.Listed))
        assertEquals(R.string.inventory_listing_status_not_listed, InventoryCopy.listingMarkStatusRes(ListingMark.Missing))
        assertEquals(R.string.inventory_listing_status_unknown, InventoryCopy.listingMarkStatusRes(ListingMark.Unknown))
    }
}
