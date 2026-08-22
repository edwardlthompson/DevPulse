package dev.foss.goldenpath.inventory

import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.File

object UpdateAllLaunch {
    const val EXTRA = "update_all"

    fun requested(intent: Intent?): Boolean =
        intent?.getBooleanExtra(EXTRA, false) == true

    fun run(context: Context) {
        IgnoredUpdates.hydrate(context.filesDir)
        RemoteReleaseMemory.hydrate(FileRemoteReleaseStore(File(context.filesDir, "remote_releases.json")))
        val apps = PackageManagerPackageCatalog(context.packageManager)
            .listInstalled()
            .map(RemoteReleaseMemory::merge)
        val groups = UpdateAllPick.groups(apps)
        val queue = groups.map { it.first() }
        Log.i("DevPulse", "update all start ${queue.size}")
        RefreshTrace.emit = { Log.i("DevPulse", it) }
        val result = UpdateAll.run(
            jobs = queue,
            groups = groups,
            prepare = { job, progress ->
                ListingInstallLive.prepare(context, job.packageName, job.source, job.pageUrl, progress)
            },
            install = { files ->
                InstallAwait.arm()
                val launched = ListingInstallLive.install(
                    context,
                    files,
                    InstallMethod.Session,
                ) == OneClickResult.Installed
                launched && InstallAwait.await()
            },
            filesDir = context.filesDir,
        )
        Log.i(
            "DevPulse",
            "update all done downloaded=${result.downloaded} installed=${result.installed} failDl=${result.failedDownload} failIns=${result.failedInstall}",
        )
    }
}
