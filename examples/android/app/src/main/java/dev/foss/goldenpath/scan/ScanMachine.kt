package dev.foss.goldenpath.scan

object ScanMachine {
    fun idle(): ScanProgress = ScanProgress(ScanPhase.Idle, 0, 0)

    fun start(total: Int): ScanProgress {
        val size = total.coerceAtLeast(0)
        return if (size == 0) {
            ScanProgress(ScanPhase.Completed, 0, 0)
        } else {
            ScanProgress(ScanPhase.Running, 0, size)
        }
    }

    fun pause(progress: ScanProgress): ScanProgress =
        if (progress.phase == ScanPhase.Running) progress.copy(phase = ScanPhase.Paused) else progress

    fun resume(progress: ScanProgress): ScanProgress =
        if (progress.phase == ScanPhase.Paused) progress.copy(phase = ScanPhase.Running) else progress

    fun advance(progress: ScanProgress): ScanProgress {
        if (progress.phase != ScanPhase.Running || progress.total <= 0) return progress
        val next = (progress.completed + 1).coerceAtMost(progress.total)
        val phase = if (next >= progress.total) ScanPhase.Completed else ScanPhase.Running
        return progress.copy(completed = next, phase = phase)
    }
}
