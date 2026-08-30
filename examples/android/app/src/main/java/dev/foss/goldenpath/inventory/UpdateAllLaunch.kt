package dev.foss.goldenpath.inventory

import android.content.Context
import android.content.Intent
import android.util.Log
import dev.foss.goldenpath.index.aurora.AuroraPlayLive
import dev.foss.goldenpath.index.aurora.AuroraPlayWarm
import java.io.File

object UpdateAllLaunch {
    const val EXTRA = "update_all"

    fun requested(intent: Intent?): Boolean =
        intent?.getBooleanExtra(EXTRA, false) == true

    fun run(context: Context, selected: Set<String> = emptySet()) {
        IgnoredUpdates.hydrate(context.filesDir)
        AppliedUpdates.hydrate(context.filesDir)
        SignerReplaceQueue.hydrate(context.filesDir)
        RemoteReleaseMemory.hydrate(FileRemoteReleaseStore(File(context.filesDir, "remote_releases.json")))
        val apps = PackageManagerPackageCatalog(context.packageManager)
            .listInstalled()
            .map(RemoteReleaseMemory::merge)
        val groups = UpdateAllPick.groups(
            apps,
            selected,
            android.os.Build.VERSION.SDK_INT,
            android.os.Build.SUPPORTED_ABIS.toSet(),
        )
        val queue = groups.map { it.first() }
        Log.i("DevPulse", "update all start ${queue.size}")
        RefreshTrace.emit = { Log.i("DevPulse", it) }
        AuroraPlayWarm.session(context)
        val result = try {
            UpdateAll.run(
                jobs = queue,
                groups = groups,
                prepare = { job, progress ->
                    ListingInstallLive.prepare(context, job.packageName, job.source, job.pageUrl, progress)
                },
                install = { files ->
                    val used = InstallMethod.Session.effective(WelcomeNeeds.installGranted(context))
                    if (used == InstallMethod.Session) {
                        SessionThenSystem.run(context, files)
                    } else {
                        ListingInstallLive.install(context, files, used) == OneClickResult.Installed
                    }
                },
                clash = { job, files -> SignerReplaceLive.clash(context, job.packageName, files) },
                filesDir = context.filesDir,
            )
        } finally {
            AuroraPlayLive.releaseSession()
        }
        Log.i(
            "DevPulse",
            "update all done downloaded=${result.downloaded} installed=${result.installed} failDl=${result.failedDownload} failIns=${result.failedInstall}",
        )
    }
}
