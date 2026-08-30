package dev.foss.goldenpath.inventory

import android.content.Intent
import java.io.File

enum class SignerReplaceNext { Confirm, Install, Cancelled, MissingFile }

object SignerClash {
    fun offer(
        expectedPackage: String,
        apkPackage: String?,
        apkSigners: Set<String>,
        installedSigners: Set<String>,
        systemApp: Boolean,
    ): Boolean {
        if (systemApp) return false
        val pkg = expectedPackage.trim()
        if (pkg.isEmpty() || apkPackage != pkg) return false
        if (apkSigners.isEmpty() || installedSigners.isEmpty()) return false
        return apkSigners.intersect(installedSigners).isEmpty()
    }

    fun filesReady(files: List<File>): Boolean =
        files.isNotEmpty() && files.all { it.isFile && it.length() > 0L }

    fun resume(installed: Boolean, filesReady: Boolean): SignerReplaceNext = when {
        !installed && filesReady -> SignerReplaceNext.Install
        installed && filesReady -> SignerReplaceNext.Confirm
        else -> SignerReplaceNext.MissingFile
    }

    fun afterUninstall(installed: Boolean, filesReady: Boolean): SignerReplaceNext = when {
        !installed && filesReady -> SignerReplaceNext.Install
        installed -> SignerReplaceNext.Cancelled
        else -> SignerReplaceNext.MissingFile
    }

    fun uninstallDone(action: String?, removedPackage: String?, replacing: Boolean, heldPackage: String): Boolean {
        if (replacing) return false
        if (action != Intent.ACTION_PACKAGE_REMOVED) return false
        val held = heldPackage.trim()
        return held.isNotEmpty() && removedPackage?.trim() == held
    }
}
