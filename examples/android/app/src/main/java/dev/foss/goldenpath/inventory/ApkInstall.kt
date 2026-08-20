package dev.foss.goldenpath.inventory

import android.content.Context
import dev.foss.goldenpath.about.UpdateApplier
import java.io.File

object ApkInstall {
    fun apply(
        context: Context,
        apkFile: File,
        method: InstallMethod,
        shell: InstallShell = ProcessInstallShell,
    ): ApkInstallResult = apply(
        apkFile,
        method,
        system = { UpdateApplier.launchApkInstall(context, it) },
        session = { SessionApkInstall.start(context, it) },
        shell = shell,
    )

    fun apply(
        apkFile: File,
        method: InstallMethod,
        system: (File) -> Unit,
        session: (File) -> Unit,
        shell: InstallShell,
    ): ApkInstallResult {
        if (!apkFile.isFile) return ApkInstallResult.Failed("missing")
        return when (method) {
            InstallMethod.System -> {
                system(apkFile)
                ApkInstallResult.Launched
            }
            InstallMethod.Session -> {
                session(apkFile)
                ApkInstallResult.Launched
            }
            InstallMethod.Root -> {
                val args = RootPmInstall.args(apkFile.absolutePath)
                    ?: return ApkInstallResult.Failed("path")
                RootPmInstall.outcome(shell.run(args))
            }
        }
    }
}
