package dev.foss.goldenpath.inventory

import kotlinx.coroutines.flow.MutableStateFlow

/** Shared scan+update window state. The button publishes; the host dialog reads. */
object UpdateAllSession {
    val snaps = MutableStateFlow<List<UpdateAllSnap>>(emptyList())
    val busy = MutableStateFlow(false)
    val visible = MutableStateFlow(false)
    val rootInstall = MutableStateFlow(false)

    fun hide() {
        visible.value = false
    }

    fun show() {
        visible.value = true
    }

    fun seedWait(jobs: List<UpdateAllJob>) {
        if (busy.value) return
        val current = snaps.value.associateBy { it.packageName }
        snaps.value = jobs.map { job ->
            current[job.packageName]
                ?: UpdateAllSnap(job.packageName, job.label, job.source, UpdateAllPhase.Wait)
        }
    }
}
