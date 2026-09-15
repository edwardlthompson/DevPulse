package dev.foss.goldenpath.inventory

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class UpdatePrefetchWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (ReleaseRefreshRuntime.running.value) return Result.retry()
        if (UpdateAllSession.busy.value) return Result.retry()
        val wanted = inputData.getString(UpdatePrefetchLaunch.EXTRA_PACKAGES)
            .orEmpty()
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        runCatching { UpdatePrefetchLive.run(applicationContext, wanted) }
            .getOrElse { return Result.retry() }
        return Result.success()
    }
}
