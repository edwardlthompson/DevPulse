package dev.foss.goldenpath.inventory

import android.content.pm.PackageManager
import android.os.Build
import java.io.File

object ApkArchiveIdentity {
    fun inspect(pm: PackageManager, file: File, signing: Boolean = true): ApkInspect {
        if (!ApkZip.ok(file)) return ApkInspect(null, emptySet())
        val info = archiveInfo(pm, file.absolutePath, signing) ?: return ApkInspect(null, emptySet())
        return ApkInspect(info.packageName, if (signing) signersOf(info) else emptySet(), emptySet())
    }

    fun installed(pm: PackageManager, packageName: String): InstalledIdentity? = runCatching {
        val info = pm.getPackageInfo(packageName, signingFlags())
        InstalledIdentity(
            packageName = info.packageName,
            signers = signersOf(info),
            abis = Build.SUPPORTED_ABIS.filter { it.isNotBlank() }.toSet(),
        )
    }.getOrNull()

    fun signingSha1(pm: PackageManager, packageName: String): String? = runCatching {
        val info = pm.getPackageInfo(packageName, signingFlags())
        val cert = if (Build.VERSION.SDK_INT >= 28) {
            info.signingInfo?.apkContentsSigners?.firstOrNull()?.toByteArray()
        } else {
            @Suppress("DEPRECATION")
            info.signatures?.firstOrNull()?.toByteArray()
        }
        ApkSigningSha1.of(cert)
    }.getOrNull()

    fun versionName(pm: PackageManager, packageName: String): String? = runCatching {
        pm.getPackageInfo(packageName, 0).versionName?.trim()?.ifEmpty { null }
    }.getOrNull()

    fun archiveCode(pm: PackageManager, file: File): Long = runCatching {
        if (!ApkZip.ok(file)) return 0L
        val info = archiveInfo(pm, file.absolutePath, signing = false) ?: return 0L
        if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
    }.getOrDefault(0L)

    fun versionCode(pm: PackageManager, packageName: String): Long = runCatching {
        val info = pm.getPackageInfo(packageName, 0)
        if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
    }.getOrDefault(0L)

    private fun archiveInfo(pm: PackageManager, path: String, signing: Boolean) = if (!signing) {
        pm.getPackageArchiveInfo(path, 0)
    } else if (Build.VERSION.SDK_INT >= 28) {
        pm.getPackageArchiveInfo(path, PackageManager.GET_SIGNING_CERTIFICATES)
    } else {
        @Suppress("DEPRECATION")
        pm.getPackageArchiveInfo(path, PackageManager.GET_SIGNATURES)
    }

    private fun signingFlags(): Int = if (Build.VERSION.SDK_INT >= 28) {
        PackageManager.GET_SIGNING_CERTIFICATES
    } else {
        @Suppress("DEPRECATION")
        PackageManager.GET_SIGNATURES
    }

    private fun signersOf(info: android.content.pm.PackageInfo): Set<String> {
        val certs = if (Build.VERSION.SDK_INT >= 28) {
            info.signingInfo?.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            info.signatures
        }
        return certs.orEmpty().map { ApkIdentity.digest(it.toByteArray()) }.toSet()
    }
}
