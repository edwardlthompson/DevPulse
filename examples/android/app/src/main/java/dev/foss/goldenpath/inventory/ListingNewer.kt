package dev.foss.goldenpath.inventory

object ListingNewer {
    fun allow(listing: String?, installed: String?): Boolean {
        val want = listing?.trim().orEmpty()
        val have = installed?.trim().orEmpty()
        if (want.isEmpty() || have.isEmpty()) return true
        return VersionCompare.isNewer(want, have)
    }
}
