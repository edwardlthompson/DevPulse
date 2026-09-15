package dev.foss.goldenpath.inventory

data class UpdateAllBar(
    val progress: Float,
    val indeterminate: Boolean = false,
    val error: Boolean = false,
)

/** Per-app download and install bars from a live snap. */
object UpdateAllRowFill {
    fun download(snap: UpdateAllSnap): UpdateAllBar = when (snap.phase) {
        UpdateAllPhase.Wait -> UpdateAllBar(0f)
        UpdateAllPhase.Fetch -> if (snap.expected > 0L) {
            UpdateAllBar((snap.received.toFloat() / snap.expected.toFloat()).coerceIn(0f, 1f))
        } else {
            UpdateAllBar(0f, indeterminate = true)
        }
        UpdateAllPhase.Ready, UpdateAllPhase.Apply, UpdateAllPhase.Ok -> UpdateAllBar(1f)
        UpdateAllPhase.Fail -> UpdateAllBar(1f, error = snap.failDownload)
    }

    fun install(snap: UpdateAllSnap): UpdateAllBar = when (snap.phase) {
        UpdateAllPhase.Wait, UpdateAllPhase.Fetch -> UpdateAllBar(0f)
        UpdateAllPhase.Ready -> UpdateAllBar(0f)
        UpdateAllPhase.Apply -> UpdateAllBar(0f, indeterminate = true)
        UpdateAllPhase.Ok -> UpdateAllBar(1f)
        UpdateAllPhase.Fail -> if (snap.failDownload) UpdateAllBar(0f) else UpdateAllBar(1f, error = true)
    }

    fun busy(phase: UpdateAllPhase): Boolean = when (phase) {
        UpdateAllPhase.Fetch, UpdateAllPhase.Apply, UpdateAllPhase.Ready -> true
        else -> false
    }
}
