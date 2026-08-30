package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.R
import org.junit.Assert.assertEquals
import org.junit.Test

class InventoryCopyTest {
    @Test
    fun unlistedResDistinguishesDelistedFromUnknown() {
        assertEquals(R.string.inventory_listing_delisted, InventoryCopy.unlistedRes(known = true))
        assertEquals(R.string.inventory_listing_status_unknown, InventoryCopy.unlistedRes(known = false))
        assertEquals(R.string.inventory_listing_unknown, InventoryCopy.unlistedRes(known = false, miss = ListingMiss.Forbidden))
        assertEquals(R.string.inventory_listing_status_unknown, InventoryCopy.unlistedRes(known = false, miss = ListingMiss.Parse))
        assertEquals(R.string.inventory_listing_delisted, InventoryCopy.unlistedRes(known = true, miss = ListingMiss.Never))
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
        assertEquals(ListingMark.Ignored, InventoryCopy.listingMark(listed = true, known = true, ignored = true))
        assertEquals("⚠️ ", InventoryCopy.listingMarkPrefix(ListingMark.Ignored))
        assertEquals(R.string.inventory_listing_status_listed, InventoryCopy.listingMarkStatusRes(ListingMark.Listed))
        assertEquals(R.string.inventory_listing_status_not_listed, InventoryCopy.listingMarkStatusRes(ListingMark.Missing))
        assertEquals(R.string.inventory_listing_status_unknown, InventoryCopy.listingMarkStatusRes(ListingMark.Unknown))
    }

    @Test
    fun failResMatchesWhy() {
        assertEquals(R.string.install_method_failed, InventoryCopy.failRes(InstallWhy.Permission))
        assertEquals(R.string.sources_no_install, InventoryCopy.failRes(InstallWhy.Signing))
        assertEquals(R.string.about_debug_navigation_mode, InventoryCopy.failRes(InstallWhy.Timeout))
        assertEquals(R.string.update_cache_failed, InventoryCopy.failRes(InstallWhy.NoFile))
        assertEquals(R.string.update_all_no_space, InventoryCopy.failRes(InstallWhy.NoSpace))
        assertEquals(R.string.about_update_current, InventoryCopy.failRes(InstallWhy.Older))
        assertEquals(R.string.inventory_sdk_risk, InventoryCopy.failRes(InstallWhy.Sdk))
        assertEquals(R.string.update_all_play_purchase, InventoryCopy.failRes(InstallWhy.PlayPurchase))
        assertEquals(R.string.update_all_play_store, InventoryCopy.failRes(InstallWhy.PlayStore))
        assertEquals(
            R.string.aptoide_body,
            InventoryCopy.failRes(InstallWhy.Signing, RemoteReleasedSource.Aptoide),
        )
    }
}
