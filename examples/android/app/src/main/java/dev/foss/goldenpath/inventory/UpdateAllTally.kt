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
    fun visible(snaps: List<UpdateAllSnap>): List<UpdateAllSnap> = snaps.filter { snap ->
        snap.stay ||
            snap.phase == UpdateAllPhase.Ok ||
            snap.phase == UpdateAllPhase.Fail ||
            snap.phase == UpdateAllPhase.Ready ||
            snap.phase == UpdateAllPhase.Apply ||
            snap.phase == UpdateAllPhase.Fetch
    }

    fun of(snaps: List<UpdateAllSnap>): UpdateAllCounts {
        val rows = visible(snaps)
        var downloadedOk = 0
        var downloadedFail = 0
        var installedOk = 0
        var installedFail = 0
        for (snap in rows) {
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
        return UpdateAllCounts(rows.size, downloadedOk, downloadedFail, installedOk, installedFail)
    }

    fun ranked(snaps: List<UpdateAllSnap>): List<UpdateAllSnap> = visible(snaps).sortedBy { snap ->
        when (snap.phase) {
            UpdateAllPhase.Fetch, UpdateAllPhase.Apply -> 0
            UpdateAllPhase.Ready -> 1
            UpdateAllPhase.Wait -> 2
            UpdateAllPhase.Fail -> 3
            UpdateAllPhase.Ok -> 4
        }
    }
}
