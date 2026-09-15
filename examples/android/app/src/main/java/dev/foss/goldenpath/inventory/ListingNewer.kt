package dev.foss.goldenpath.inventory

object ListingNewer {
    fun allow(
        listing: String?,
        installed: String?,
        installedCode: Long = 0,
        remoteCode: Long? = null,
    ): Boolean {
        val want = listing?.trim().orEmpty()
        val have = installed?.trim().orEmpty()
        if (remoteCode != null && remoteCode > 0 && installedCode > 0) {
            return remoteCode > installedCode
        }
        if (want.isEmpty() || (have.isEmpty() && installedCode <= 0L)) return true
        return VersionCompare.isNewer(want, have, installedCode, remoteCode)
    }
}
