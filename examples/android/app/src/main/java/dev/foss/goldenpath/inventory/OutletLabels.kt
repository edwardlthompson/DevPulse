package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.R

object OutletLabels {
    fun nameRes(id: String): Int? = when (id) {
        RefreshOutletIds.PLAY -> R.string.inventory_source_play
        RefreshOutletIds.APTOIDE -> R.string.inventory_source_aptoide
        RefreshOutletIds.GITHUB -> R.string.inventory_source_forge
        RefreshOutletIds.MIRROR -> R.string.inventory_source_apkmirror
        RefreshOutletIds.PURE -> R.string.inventory_source_apkpure
        RefreshOutletIds.LEFTOVER -> R.string.inventory_source_leftover
        else -> if (id.startsWith("fdroid:")) R.string.inventory_source_fdroid else null
    }

    fun fallback(id: String): String = id.removePrefix("fdroid:")
}
