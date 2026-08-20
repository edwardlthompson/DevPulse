package dev.foss.goldenpath.inventory

object UpdateInventory {
    fun hasUpdate(app: InstalledApp): Boolean =
        VersionCompare.isNewer(app.remoteVersionName, app.versionName)

    fun withUpdates(apps: List<InstalledApp>): List<InstalledApp> = apps.filter(::hasUpdate)

    fun canOpen(link: UpdateLink): Boolean = link.listed && !link.url.isNullOrBlank()

    fun listingsFor(pick: RemoteReleasePick): List<UpdateLink> =
        ListingChannels.complete(pick.offers.map(ListingChannels::relabel), ListingChannels.STANDARD)
            .filter { it.source != RemoteReleasedSource.None }
            .groupBy { it.source }
            .values
            .map { group -> group.firstOrNull { it.listed } ?: group.first() }
            .map { offer ->
                UpdateLink(
                    source = offer.source,
                    url = if (offer.listed) offer.pageUrl else null,
                    versionName = offer.versionName,
                    releasedAtMs = offer.ms,
                    listed = offer.listed,
                    known = offer.known,
                )
            }
            .sortedWith(::byHighestVersion)

    fun linksFor(installedVersion: String?, pick: RemoteReleasePick): List<UpdateLink> =
        listingsFor(pick).filter { canOpen(it) && VersionCompare.isNewer(it.versionName, installedVersion) }

    private fun byHighestVersion(left: UpdateLink, right: UpdateLink): Int {
        val leftName = left.versionName?.trim().orEmpty()
        val rightName = right.versionName?.trim().orEmpty()
        val compared = VersionCompare.compare(leftName, rightName)
        if (compared != 0) return -compared
        if (leftName.isEmpty() != rightName.isEmpty()) return if (leftName.isEmpty()) 1 else -1
        return UpdateLinkRank.rank(left.source).compareTo(UpdateLinkRank.rank(right.source))
    }
}
