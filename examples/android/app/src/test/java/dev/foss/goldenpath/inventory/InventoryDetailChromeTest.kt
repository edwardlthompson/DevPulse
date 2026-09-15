package dev.foss.goldenpath.inventory

import org.junit.Assert.assertEquals
import org.junit.Test

class InventoryDetailChromeTest {
    @Test
    fun surfaceKeepsIdentityStatusListings() {
        assertEquals(InventoryDetailChrome.Slot.Identity, InventoryDetailChrome.slot("package"))
        assertEquals(InventoryDetailChrome.Slot.Identity, InventoryDetailChrome.slot("icon"))
        assertEquals(InventoryDetailChrome.Slot.Identity, InventoryDetailChrome.slot("label"))
        assertEquals(InventoryDetailChrome.Slot.Identity, InventoryDetailChrome.slot("pin"))
        assertEquals(InventoryDetailChrome.Slot.Status, InventoryDetailChrome.slot("last_release"))
        assertEquals(InventoryDetailChrome.Slot.Listings, InventoryDetailChrome.slot("listings"))
    }

    @Test
    fun pasteRegexApkAreAdvanced() {
        assertEquals(InventoryDetailChrome.Slot.Advanced, InventoryDetailChrome.slot("paste"))
        assertEquals(InventoryDetailChrome.Slot.Advanced, InventoryDetailChrome.slot("regex"))
        assertEquals(InventoryDetailChrome.Slot.Advanced, InventoryDetailChrome.slot("apk"))
        assertEquals(InventoryDetailChrome.Slot.Advanced, InventoryDetailChrome.slot("origin"))
        assertEquals(InventoryDetailChrome.Slot.Advanced, InventoryDetailChrome.slot("sdk"))
    }
}
