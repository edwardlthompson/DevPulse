package dev.foss.goldenpath.inventory

import java.io.File

enum class InstallWhy { Permission, Signing, Timeout, NoFile, Older, Sdk, NoSpace, PlayPurchase, PlayStore }

object ListingFail {
    private val held = ThreadLocal.withInitial { InstallWhy.NoFile }

    var why: InstallWhy
        get() = held.get() ?: InstallWhy.NoFile
        set(value) { held.set(value) }

    fun none(): List<File>? {
        why = InstallWhy.NoFile
        return null
    }

    fun space(): List<File>? {
        why = InstallWhy.NoSpace
        return null
    }

    fun signing(): List<File>? {
        why = InstallWhy.Signing
        return null
    }

    fun older(): List<File>? {
        why = InstallWhy.Older
        return null
    }

    fun sdk(): List<File>? {
        why = InstallWhy.Sdk
        return null
    }

    fun playPurchase(): List<File>? {
        why = InstallWhy.PlayPurchase
        return null
    }
}

fun ApkInstallResult.toClick(): OneClickResult = when (this) {
    is ApkInstallResult.Failed -> OneClickResult.Failed(
        when (reason) {
            "ui" -> InstallWhy.Timeout
            "missing", "path" -> InstallWhy.NoFile
            else -> InstallWhy.Permission
        },
    )
    else -> OneClickResult.Installed
}
