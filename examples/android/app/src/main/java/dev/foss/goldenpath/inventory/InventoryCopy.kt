package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.R

object InventoryCopy {
    fun originRes(app: InstalledApp): Int = when (app.origin) {
        AppOrigin.Play -> R.string.inventory_origin_play
        AppOrigin.Fdroid -> R.string.inventory_origin_fdroid
        AppOrigin.ExtraRepo -> R.string.inventory_origin_extra
        AppOrigin.SideloadedUnknown -> if (app.isSystemApp) {
            R.string.inventory_origin_preinstalled
        } else {
            R.string.inventory_origin_sideload
        }
        AppOrigin.Unknown -> R.string.inventory_origin_sideload
    }

    fun sourceRes(source: RemoteReleasedSource): Int = when (source) {
        RemoteReleasedSource.Play -> R.string.inventory_source_play
        RemoteReleasedSource.Fdroid -> R.string.inventory_source_fdroid
        RemoteReleasedSource.ExtraRepo -> R.string.inventory_source_extra
        RemoteReleasedSource.Izzy -> R.string.inventory_source_izzy
        RemoteReleasedSource.Guardian -> R.string.inventory_source_guardian
        RemoteReleasedSource.Calyx -> R.string.inventory_source_calyx
        RemoteReleasedSource.Archive -> R.string.inventory_source_archive
        RemoteReleasedSource.Aptoide -> R.string.inventory_source_aptoide
        RemoteReleasedSource.ApkMirror -> R.string.inventory_source_apkmirror
        RemoteReleasedSource.ApkPure -> R.string.inventory_source_apkpure
        RemoteReleasedSource.Forge -> R.string.inventory_source_forge
        RemoteReleasedSource.None -> R.string.inventory_source_unknown
    }

    fun unlistedRes(known: Boolean, miss: ListingMiss? = null): Int = when (miss) {
        ListingMiss.Forbidden -> R.string.inventory_listing_unknown
        ListingMiss.Parse -> R.string.inventory_listing_status_unknown
        ListingMiss.Never -> R.string.inventory_listing_delisted
        null -> if (known) R.string.inventory_listing_delisted else R.string.inventory_listing_status_unknown
    }

    fun listingMark(listed: Boolean?, known: Boolean?, ignored: Boolean = false): ListingMark {
        if (ignored) return ListingMark.Ignored
        if (known != true) return ListingMark.Unknown
        return if (listed == true) ListingMark.Listed else ListingMark.Missing
    }

    fun listingMarkPrefix(mark: ListingMark): String = when (mark) {
        ListingMark.Listed -> "✅ "
        ListingMark.Missing -> "❌ "
        ListingMark.Unknown -> "❓ "
        ListingMark.Ignored -> "⚠️ "
    }

    fun failRes(why: InstallWhy, source: RemoteReleasedSource = RemoteReleasedSource.None): Int {
        if (why == InstallWhy.Signing && source == RemoteReleasedSource.Aptoide) {
            return R.string.aptoide_body
        }
        return when (why) {
            InstallWhy.Permission -> R.string.install_method_failed
            InstallWhy.Signing -> R.string.sources_no_install
            InstallWhy.Timeout -> R.string.about_debug_navigation_mode
            InstallWhy.NoFile -> R.string.update_cache_failed
            InstallWhy.Older -> R.string.about_update_current
            InstallWhy.Sdk -> R.string.inventory_sdk_risk
        }
    }

    fun listingMarkStatusRes(mark: ListingMark): Int = when (mark) {
        ListingMark.Listed -> R.string.inventory_listing_status_listed
        ListingMark.Missing -> R.string.inventory_listing_status_not_listed
        ListingMark.Unknown -> R.string.inventory_listing_status_unknown
        ListingMark.Ignored -> R.string.update_cache_failed
    }
}

enum class ListingMark {
    Listed,
    Missing,
    Unknown,
    Ignored,
}
