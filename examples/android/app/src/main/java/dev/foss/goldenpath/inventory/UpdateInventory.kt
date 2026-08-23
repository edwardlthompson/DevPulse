package dev.foss.goldenpath.inventory

object UpdateInventory {
    fun hasUpdate(app: InstalledApp): Boolean {
        if (AppliedUpdates.settled(app.packageName)) return false
        if (usable(app).isNotEmpty()) return true
        if (app.latestListings.any { it.listed }) return false
        val remote = app.remoteVersionName
        if (IgnoredUpdates.has(app.packageName, app.remoteVersionSource, remote)) return false
        return VersionCompare.isNewer(remote, app.versionName)
    }

    fun withUpdates(apps: List<InstalledApp>): List<InstalledApp> = apps.filter(::hasUpdate)

    fun usable(app: InstalledApp, deviceSdk: Int = 0, deviceAbis: Set<String> = emptySet()): List<UpdateLink> =
        app.latestListings.filter { link ->
            link.listed &&
                UpdateAll.fetchable(link.source, app.packageName) &&
                VersionCompare.isNewer(link.versionName, app.versionName) &&
                !IgnoredUpdates.has(app.packageName, link.source, link.versionName) &&
                ListingFit.allow(link, deviceSdk, deviceAbis)
        }

    fun canOpen(
        link: UpdateLink,
        packageName: String = "",
        installedVersion: String? = null,
        deviceSdk: Int = 0,
        deviceAbis: Set<String> = emptySet(),
    ): Boolean =
        link.listed &&
            !IgnoredUpdates.has(packageName, link.source, link.versionName) &&
            ListingNewer.allow(link.versionName, installedVersion) &&
            ListingFit.allow(link, deviceSdk, deviceAbis)

    fun listingsFor(pick: RemoteReleasePick, packageName: String = ""): List<UpdateLink> =
        ListingChannels.complete(pick.offers.map(ListingChannels::relabel), ListingChannels.STANDARD)
            .filter { it.source != RemoteReleasedSource.None }
            .groupBy { it.source }
            .values
            .map { group -> group.firstOrNull { it.listed } ?: group.first() }
            .map { offer ->
                val extra = ListingExtraBook.get(packageName, offer.source)
                UpdateLink(
                    source = offer.source,
                    url = if (offer.listed) offer.pageUrl else null,
                    versionName = offer.versionName,
                    releasedAtMs = offer.ms,
                    listed = offer.listed,
                    known = offer.known,
                    miss = offer.miss,
                    sizeBytes = extra?.sizeBytes,
                    antiFeatures = extra?.antiFeatures.orEmpty(),
                    minSdk = extra?.minSdk,
                    nativeCodes = extra?.nativeCodes.orEmpty(),
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
