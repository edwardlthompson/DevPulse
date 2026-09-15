package dev.foss.goldenpath.inventory

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class GitHubDiscoverWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (ReleaseRefreshRuntime.running.value) return Result.retry()
        if (UpdateAllSession.busy.value) return Result.retry()
        if (!GitHubDiscoverLive.allowed(applicationContext)) return Result.success()
        val tick = runCatching { GitHubDiscoverLive.run(applicationContext) }.getOrNull()
            ?: return Result.retry()
        if (tick.remaining > 0) return Result.retry()
        return Result.success()
    }
}
