package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.fdroid.FdroidRepo

internal object ReleaseRefreshComplete {
    fun searched(
        repos: List<FdroidRepo>,
        playOn: Boolean,
        aptoideEnabled: Boolean,
        forgeOn: Boolean,
        apkMirrorEnabled: Boolean,
        apkPureEnabled: Boolean,
    ): Set<RemoteReleasedSource> {
        val out = repos.map { ListingChannels.sourceForRepo(it.id) }.toMutableSet()
        if (playOn) out += RemoteReleasedSource.Play
        if (aptoideEnabled) out += RemoteReleasedSource.Aptoide
        if (apkMirrorEnabled) out += RemoteReleasedSource.ApkMirror
        if (apkPureEnabled) out += RemoteReleasedSource.ApkPure
        if (forgeOn) out += RemoteReleasedSource.Forge
        return out
    }

    fun write(
        apps: List<InstalledApp>,
        snapshot: (String) -> List<RemoteReleaseOffer>,
        searched: Set<RemoteReleasedSource>,
    ) {
        apps.forEach { app ->
            val pkg = app.packageName
            RemoteReleaseMemory.putAll(
                mapOf(pkg to RemoteReleaseRollup.from(ListingChannels.complete(snapshot(pkg), searched))),
            )
        }
    }
}
