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
        val jobs = buildList {
            if (apkMirrorEnabled) {
                add(
                    Thread {
                        applyOne(apps, RefreshOutletIds.MIRROR, "ApkMirror", clock) {
                            ApkMirrorScan.offersFor(packages, apkMirrorFetcher, nowMs)
                        }.forEach { (app, offer) -> commit(app.packageName, offer) }
                    },
                )
            }
            if (apkPureEnabled) {
                add(
                    Thread {
                        applyOne(apps, RefreshOutletIds.PURE, "ApkPure", clock) {
                            ApkPureScan.offersFor(packages, apkPureFetcher, nowMs)
                        }.forEach { (app, offer) -> commit(app.packageName, offer) }
                    },
                )
            }
        }
        jobs.forEach { it.start() }
        jobs.forEach { it.join() }
    }

    private fun applyOne(
        apps: List<InstalledApp>,
        id: String,
        source: String,
        clock: RefreshProgressClock,
        fetch: () -> Map<String, RemoteReleaseOffer>,
    ): List<Pair<InstalledApp, RemoteReleaseOffer>> {
        clock.planOutlet(id, source, apps.size)
        val offers = if (RefreshSkip.stopped(id)) emptyMap() else fetch()
        val rows = apps.mapNotNull { app ->
            val loc = RefreshLocations.label(source, app.label, app.packageName)
            clock.begin(loc)
            clock.outletAt(id, app.label)
            val offer = offers[app.packageName]
            clock.outletTick(id)
            clock.tick(loc)
            if (offer == null) null else app to offer
        }
        RefreshOutletBoard.noteFinished(id)
        return rows
    }
}
