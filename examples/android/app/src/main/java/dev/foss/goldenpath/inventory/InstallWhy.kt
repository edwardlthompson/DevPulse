package dev.foss.goldenpath.inventory

import java.io.File

enum class InstallWhy { Permission, Signing, Timeout, NoFile, Older, Sdk }

object ListingFail {
    @Volatile
    var why: InstallWhy = InstallWhy.NoFile

    fun none(): List<File>? {
        why = InstallWhy.NoFile
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
