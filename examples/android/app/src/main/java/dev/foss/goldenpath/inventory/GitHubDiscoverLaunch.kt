package dev.foss.goldenpath.inventory

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object GitHubDiscoverLaunch {
    const val WORK_NAME = "devpulse_github_discover"

    fun enqueue(context: Context, delaySec: Long = 0, replace: Boolean = false) {
        val builder = OneTimeWorkRequestBuilder<GitHubDiscoverWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
            )
        if (delaySec > 0) builder.setInitialDelay(delaySec, TimeUnit.SECONDS)
        val policy = if (replace) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP
        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(WORK_NAME, policy, builder.build())
    }
}
