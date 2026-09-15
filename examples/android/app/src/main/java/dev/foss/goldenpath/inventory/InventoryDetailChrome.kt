package dev.foss.goldenpath.inventory

object InventoryDetailChrome {
    enum class Slot { Identity, Status, Listings, Advanced }

    fun slot(name: String): Slot = when (name) {
        "package", "icon", "label", "pin" -> Slot.Identity
        "last_release" -> Slot.Status
        "listings" -> Slot.Listings
        else -> Slot.Advanced
    }
}
