package dev.foss.goldenpath.inventory

import android.content.Context
import android.os.Handler
import android.os.Looper
import dev.foss.goldenpath.about.UpdateApplier
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object ApkInstall {
    fun apply(
        context: Context,
        apkFile: File,
        method: InstallMethod,
        shell: InstallShell = ProcessInstallShell,
    ): ApkInstallResult {
        val work = {
            apply(
                apkFile,
                method,
                system = { UpdateApplier.launchApkInstall(context, it) },
                session = { SessionApkInstall.start(context, it) },
                shell = shell,
            )
        }
        if (method == InstallMethod.Root || Looper.myLooper() == Looper.getMainLooper()) {
            return runCatching(work).getOrElse { ApkInstallResult.Failed(it.javaClass.simpleName) }
        }
        val latch = CountDownLatch(1)
        var out: ApkInstallResult = ApkInstallResult.Failed("ui")
        Handler(Looper.getMainLooper()).post {
            out = runCatching(work).getOrElse { ApkInstallResult.Failed(it.javaClass.simpleName) }
            latch.countDown()
        }
        return if (latch.await(20, TimeUnit.SECONDS)) out else ApkInstallResult.Failed("ui")
    }

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
                runCatching { system(apkFile) }.getOrElse {
                    return ApkInstallResult.Failed(it.javaClass.simpleName)
                }
                ApkInstallResult.Launched
            }
            InstallMethod.Session -> {
                runCatching { session(apkFile) }.getOrElse {
                    return ApkInstallResult.Failed(it.javaClass.simpleName)
                }
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
