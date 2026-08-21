package dev.foss.goldenpath.inventory

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.io.File

class PackageManagerPackageCatalog(
    private val packageManager: PackageManager,
) : PackageCatalog {
    override fun listInstalled(): List<InstalledApp> =
        runCatching { installedPackages() }.getOrDefault(emptyList()).mapNotNull { info ->
            runCatching { toSnapshot(info)?.let(InstalledAppMapper::fromSnapshot) }.getOrNull()
        }

    private fun installedPackages(): List<PackageInfo> =
        if (Build.VERSION.SDK_INT >= 33) {
            packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledPackages(0)
        }

    private fun toSnapshot(info: PackageInfo): PackageSnapshot? {
        val appInfo = info.applicationInfo ?: return null
        return PackageSnapshot(
            packageName = info.packageName,
            label = appInfo.loadLabel(packageManager).toString(),
            versionName = info.versionName,
            versionCode = packageVersionCode(info),
            lastUpdateTimeMs = info.lastUpdateTime,
            firstInstallTimeMs = info.firstInstallTime,
            minSdk = appInfo.minSdkVersion,
            targetSdk = appInfo.targetSdkVersion,
            isSystemApp = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0,
            apkLastModifiedMs = apkLastModified(appInfo.sourceDir),
            installerPackageName = installerOf(info.packageName),
        )
    }

    private fun installerOf(packageName: String): String? =
        if (Build.VERSION.SDK_INT >= 30) {
            runCatching { packageManager.getInstallSourceInfo(packageName).installingPackageName }.getOrNull()
                ?: runCatching { packageManager.getInstallSourceInfo(packageName).initiatingPackageName }.getOrNull()
        } else {
            @Suppress("DEPRECATION")
            runCatching { packageManager.getInstallerPackageName(packageName) }.getOrNull()
        }

    private fun apkLastModified(sourceDir: String?): Long =
        runCatching { sourceDir?.let { File(it).lastModified() } ?: 0L }.getOrDefault(0L)

    private fun packageVersionCode(info: PackageInfo): Long =
        if (Build.VERSION.SDK_INT >= 28) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
}
