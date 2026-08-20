package dev.foss.goldenpath.inventory

import android.content.pm.PackageManager
import android.os.Build
import java.io.File

object ApkArchiveIdentity {
    fun inspect(pm: PackageManager, file: File): ApkInspect {
        val info = archiveInfo(pm, file.absolutePath) ?: return ApkInspect(null, emptySet())
        return ApkInspect(info.packageName, signersOf(info), emptySet())
    }

    fun installed(pm: PackageManager, packageName: String): InstalledIdentity? = runCatching {
        val flags = if (Build.VERSION.SDK_INT >= 28) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_SIGNATURES
        }
        val info = pm.getPackageInfo(packageName, flags)
        InstalledIdentity(
            packageName = info.packageName,
            signers = signersOf(info),
            abis = Build.SUPPORTED_ABIS.filter { it.isNotBlank() }.toSet(),
        )
    }.getOrNull()

    private fun archiveInfo(pm: PackageManager, path: String) = if (Build.VERSION.SDK_INT >= 28) {
        pm.getPackageArchiveInfo(path, PackageManager.GET_SIGNING_CERTIFICATES)
    } else {
        @Suppress("DEPRECATION")
        pm.getPackageArchiveInfo(path, PackageManager.GET_SIGNATURES)
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
