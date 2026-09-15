package dev.foss.goldenpath.inventory

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

object UpdatePrefetchLaunch {
    const val WORK_NAME = "devpulse_update_prefetch"
    const val EXTRA_PACKAGES = "packages"

    fun enqueue(context: Context, wanted: Collection<String> = emptySet()) {
        val request = OneTimeWorkRequestBuilder<UpdatePrefetchWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build(),
            )
            .setInputData(workDataOf(EXTRA_PACKAGES to wanted.joinToString(",")))
            .build()
        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }
}
