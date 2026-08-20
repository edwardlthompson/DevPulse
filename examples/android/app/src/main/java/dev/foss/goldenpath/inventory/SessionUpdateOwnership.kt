package dev.foss.goldenpath.inventory

import android.content.pm.PackageManager
import android.os.Build

object SessionUpdateOwnership {
    const val MIN_SDK = 31

    fun requestNoUserAction(
        sdk: Int,
        packageInstalled: Boolean,
        installerPackage: String?,
        selfPackage: String,
    ): Boolean {
        val self = selfPackage.trim()
        if (sdk < MIN_SDK || !packageInstalled || self.isEmpty()) return false
        return installerPackage == self
    }

    fun installerPackage(pm: PackageManager, packageName: String): String? = runCatching {
        if (Build.VERSION.SDK_INT >= 30) {
            pm.getInstallSourceInfo(packageName).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            pm.getInstallerPackageName(packageName)
        }
    }.getOrNull()?.trim()?.ifEmpty { null }
}
