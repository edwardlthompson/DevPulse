package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.R
import org.junit.Assert.assertEquals
import org.junit.Test

class InventoryEmptyKindTest {
    @Test
    fun noneWhenVisible() {
        assertEquals(
            InventoryEmptyKind.None,
            InventoryEmpty.kind(canScan = true, query = "x", filtersOn = true, visibleCount = 1),
        )
    }

    @Test
    fun blockedBeforeSearch() {
        assertEquals(
            InventoryEmptyKind.Blocked,
            InventoryEmpty.kind(canScan = false, query = "x", filtersOn = false, visibleCount = 0),
        )
        assertEquals(R.string.inventory_empty_blocked, InventoryEmpty.res(InventoryEmptyKind.Blocked))
    }

    @Test
    fun searchThenFiltersThenNoApps() {
        assertEquals(
            InventoryEmptyKind.Search,
            InventoryEmpty.kind(canScan = true, query = "foo", filtersOn = true, visibleCount = 0),
        )
        assertEquals(
            InventoryEmptyKind.Filters,
            InventoryEmpty.kind(canScan = true, query = "", filtersOn = true, visibleCount = 0),
        )
        assertEquals(
            InventoryEmptyKind.NoApps,
            InventoryEmpty.kind(canScan = true, query = "  ", filtersOn = false, visibleCount = 0),
        )
        assertEquals(R.string.inventory_empty_search, InventoryEmpty.res(InventoryEmptyKind.Search))
        assertEquals(R.string.inventory_empty_filters, InventoryEmpty.res(InventoryEmptyKind.Filters))
        assertEquals(R.string.inventory_empty, InventoryEmpty.res(InventoryEmptyKind.NoApps))
    }
}
