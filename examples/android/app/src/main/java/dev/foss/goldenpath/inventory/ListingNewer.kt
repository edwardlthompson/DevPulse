package dev.foss.goldenpath.inventory

object ListingNewer {
    fun allow(listing: String?, installed: String?, installedCode: Long = 0): Boolean {
        val want = listing?.trim().orEmpty()
        val have = installed?.trim().orEmpty()
        if (want.isEmpty() || (have.isEmpty() && installedCode <= 0L)) return true
        return VersionCompare.isNewer(want, have, installedCode)
    }
}
