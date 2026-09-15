package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.R

enum class InventoryEmptyKind { None, Search, Filters, NoApps, Blocked }

object InventoryEmpty {
    fun kind(
        canScan: Boolean,
        query: String,
        filtersOn: Boolean,
        visibleCount: Int,
    ): InventoryEmptyKind {
        if (visibleCount > 0) return InventoryEmptyKind.None
        if (!canScan) return InventoryEmptyKind.Blocked
        if (query.isNotBlank()) return InventoryEmptyKind.Search
        if (filtersOn) return InventoryEmptyKind.Filters
        return InventoryEmptyKind.NoApps
    }

    fun res(kind: InventoryEmptyKind): Int = when (kind) {
        InventoryEmptyKind.None, InventoryEmptyKind.NoApps -> R.string.inventory_empty
        InventoryEmptyKind.Search -> R.string.inventory_empty_search
        InventoryEmptyKind.Filters -> R.string.inventory_empty_filters
        InventoryEmptyKind.Blocked -> R.string.inventory_empty_blocked
    }
}
