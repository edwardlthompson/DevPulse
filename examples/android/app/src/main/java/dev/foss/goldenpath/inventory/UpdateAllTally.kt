package dev.foss.goldenpath.inventory

data class UpdateAllSegment(
    val ok: Int,
    val fail: Int,
    val pending: Int,
) {
    val total: Int get() = ok + fail + pending
}

data class UpdateAllCounts(
    val total: Int,
    val downloadedOk: Int,
    val downloadedFail: Int,
    val installedOk: Int,
    val installedFail: Int,
) {
    fun downloadBar(): UpdateAllSegment {
        val pending = (total - downloadedOk - downloadedFail).coerceAtLeast(0)
        return UpdateAllSegment(downloadedOk, downloadedFail, pending)
    }

    fun installBar(): UpdateAllSegment {
        val pending = (downloadedOk - installedOk - installedFail).coerceAtLeast(0)
        return UpdateAllSegment(installedOk, installedFail, pending)
    }
}

object UpdateAllTally {
    fun of(snaps: List<UpdateAllSnap>): UpdateAllCounts {
        var downloadedOk = 0
        var downloadedFail = 0
        var installedOk = 0
        var installedFail = 0
        for (snap in snaps) {
            when (snap.phase) {
                UpdateAllPhase.Wait, UpdateAllPhase.Fetch -> Unit
                UpdateAllPhase.Ready, UpdateAllPhase.Apply -> downloadedOk++
                UpdateAllPhase.Ok -> {
                    downloadedOk++
                    installedOk++
                }
                UpdateAllPhase.Fail -> if (snap.failDownload) {
                    downloadedFail++
                } else {
                    downloadedOk++
                    installedFail++
                }
            }
        }
        return UpdateAllCounts(snaps.size, downloadedOk, downloadedFail, installedOk, installedFail)
    }

    fun ranked(snaps: List<UpdateAllSnap>): List<UpdateAllSnap> = snaps.sortedBy { snap ->
        when (snap.phase) {
            UpdateAllPhase.Fetch -> 0
            UpdateAllPhase.Apply -> 1
            UpdateAllPhase.Ready -> 2
            UpdateAllPhase.Wait -> 3
            UpdateAllPhase.Fail -> 4
            UpdateAllPhase.Ok -> 5
        }
    }
}
