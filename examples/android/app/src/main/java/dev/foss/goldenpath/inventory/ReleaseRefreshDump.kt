package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.apkmirror.ApkMirrorBatchFetcher
import dev.foss.goldenpath.index.apkmirror.ApkMirrorScan
import dev.foss.goldenpath.index.apkpure.ApkPureBatchFetcher
import dev.foss.goldenpath.index.apkpure.ApkPureScan

object ReleaseRefreshDump {
    fun apply(
        apps: List<InstalledApp>,
        apkMirrorEnabled: Boolean,
        apkPureEnabled: Boolean,
        apkMirrorFetcher: ApkMirrorBatchFetcher,
        apkPureFetcher: ApkPureBatchFetcher,
        nowMs: Long,
        clock: RefreshProgressClock,
        commit: (String, RemoteReleaseOffer) -> Unit,
    ) {
        RefreshTrace.line("dump stores mirror=$apkMirrorEnabled pure=$apkPureEnabled apps=${apps.size}")
        val packages = apps.map { it.packageName }
        if (apkMirrorEnabled) {
            val offers = ApkMirrorScan.offersFor(packages, apkMirrorFetcher, nowMs)
            apps.forEach { app ->
                val loc = RefreshLocations.label("ApkMirror", app.label, app.packageName)
                clock.begin(loc)
                commit(app.packageName, offers.getValue(app.packageName))
                clock.tick(loc)
            }
        }
        if (apkPureEnabled) {
            val offers = ApkPureScan.offersFor(packages, apkPureFetcher)
            apps.forEach { app ->
                val loc = RefreshLocations.label("ApkPure", app.label, app.packageName)
                clock.begin(loc)
                commit(app.packageName, offers.getValue(app.packageName))
                clock.tick(loc)
            }
        }
    }
}
