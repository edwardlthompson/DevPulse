package dev.foss.goldenpath.inventory

import android.content.Intent

object PackageChange {
    fun shouldReload(
        action: String?,
        packageName: String?,
        replacing: Boolean,
        selfPackage: String?,
    ): Boolean {
        val pkg = packageName?.trim().orEmpty()
        if (pkg.isEmpty()) return false
        if (selfPackage != null && pkg == selfPackage) return false
        return when (action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REMOVED,
            -> !replacing
            Intent.ACTION_PACKAGE_REPLACED -> true
            else -> false
        }
    }
}
