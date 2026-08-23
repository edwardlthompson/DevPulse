package dev.foss.goldenpath.ui.inventory

import android.content.Context
import android.util.Log
import dev.foss.goldenpath.inventory.InstallMethod
import dev.foss.goldenpath.inventory.ListingInstallLive
import dev.foss.goldenpath.inventory.OneClickResult
import dev.foss.goldenpath.inventory.RefreshTrace
import dev.foss.goldenpath.inventory.SessionThenSystem
import dev.foss.goldenpath.inventory.UpdateAll
import dev.foss.goldenpath.inventory.UpdateAllJob
import dev.foss.goldenpath.inventory.UpdateAllSnap
import dev.foss.goldenpath.inventory.WelcomeNeeds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal fun startUpdateAll(
    context: Context,
    scope: CoroutineScope,
    queue: List<UpdateAllJob>,
    groups: List<List<UpdateAllJob>>,
    method: InstallMethod,
    onSnap: (UpdateAllSnap) -> Unit,
    onDone: () -> Unit,
) {
    scope.launch {
        withContext(Dispatchers.IO) {
            Log.i("DevPulse", "update all start ${queue.size}")
            RefreshTrace.emit = { Log.i("DevPulse", it) }
            val result = UpdateAll.run(
                jobs = queue,
                groups = groups,
                prepare = { job, progress ->
                    ListingInstallLive.prepare(context, job.packageName, job.source, job.pageUrl, progress)
                },
                install = { files ->
                    val used = method.effective(WelcomeNeeds.installGranted(context))
                    if (used == InstallMethod.Session) {
                        SessionThenSystem.run(context, files)
                    } else {
                        ListingInstallLive.install(context, files, used) == OneClickResult.Installed
                    }
                },
                onSnap = onSnap,
                filesDir = context.filesDir,
            )
            Log.i(
                "DevPulse",
                "update all done downloaded=${result.downloaded} installed=${result.installed} failDl=${result.failedDownload} failIns=${result.failedInstall}",
            )
        }
        onDone()
    }
}
