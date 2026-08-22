package dev.foss.goldenpath.inventory

data class PackageSnapshot(
    val packageName: String,
    val label: String,
    val versionName: String?,
    val versionCode: Long,
    val lastUpdateTimeMs: Long,
    val firstInstallTimeMs: Long,
    val minSdk: Int,
    val targetSdk: Int,
    val isSystemApp: Boolean,
    val apkLastModifiedMs: Long = 0L,
    val installerPackageName: String? = null,
    val signingSha1: String? = null,
)

object InstalledAppMapper {
    fun fromSnapshot(snapshot: PackageSnapshot, nowMs: Long = System.currentTimeMillis()): InstalledApp {
        val resolved = InstalledDateResolver.resolve(
            lastUpdateTimeMs = snapshot.lastUpdateTimeMs,
            firstInstallTimeMs = snapshot.firstInstallTimeMs,
            apkLastModifiedMs = snapshot.apkLastModifiedMs,
            nowMs = nowMs,
        )
        return InstalledApp(
            packageName = snapshot.packageName,
            label = snapshot.label,
            versionName = snapshot.versionName,
            versionCode = snapshot.versionCode,
            lastUpdateTimeMs = snapshot.lastUpdateTimeMs,
            firstInstallTimeMs = snapshot.firstInstallTimeMs,
            minSdk = snapshot.minSdk,
            targetSdk = snapshot.targetSdk,
            isSystemApp = snapshot.isSystemApp,
            origin = AppOriginResolver.fromInstaller(snapshot.installerPackageName),
            installedAtMs = resolved.ms,
            installedAtSource = resolved.source,
            signingSha1 = snapshot.signingSha1,
        )
    }
}
