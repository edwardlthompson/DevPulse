package dev.foss.goldenpath.notify

import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.RefreshProgress

object RefreshNotifyCopy {
    const val CHANNEL_ID = "release_refresh"
    const val PROGRESS_ID = 71
    const val DONE_ID = 72

    fun lookedUpCount(progress: RefreshProgress): Int =
        progress.total.coerceAtLeast(progress.done)

    fun firstScanHintRes(firstScan: Boolean): Int? =
        if (firstScan) R.string.inventory_refresh_first_hint else null
}
