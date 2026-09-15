package dev.foss.goldenpath.inventory

import android.content.Context
import java.io.File

/** Root `pm install` for one APK or a split set. No installer UI. */
object RootSessionInstall {
    const val WAIT_MS = 45_000L

    fun run(context: Context, files: List<File>, shell: InstallShell = ProcessInstallShell): Boolean =
        run(files, shell)

    fun run(files: List<File>, shell: InstallShell = ProcessInstallShell): Boolean = runCatching {
        val apks = files.filter { it.isFile && it.name.endsWith(".apk", ignoreCase = true) }
        if (apks.isEmpty()) return false
        if (apks.size == 1) {
            val args = RootPmInstall.args(apks.first().absolutePath) ?: return false
            return RootPmInstall.outcome(shell.run(args)) is ApkInstallResult.Ok
        }
        val created = shell.run(createArgs())
        val id = sessionId(created.output) ?: return false
        apks.forEachIndexed { index, file ->
            val args = writeArgs(id, index, file) ?: run {
                shell.run(abandonArgs(id))
                return false
            }
            val wrote = shell.run(args)
            if (wrote.exitCode != 0 && RootPmInstall.outcome(wrote) !is ApkInstallResult.Ok) {
                shell.run(abandonArgs(id))
                return false
            }
        }
        return RootPmInstall.outcome(shell.run(commitArgs(id))) is ApkInstallResult.Ok
    }.getOrDefault(false)

    internal fun waitInstalled(context: Context, files: List<File>): Boolean {
        val apk = files.firstOrNull { it.isFile } ?: return false
        val pkg = ApkArchiveIdentity.inspect(context.packageManager, apk).packageName ?: return false
        val want = ApkArchiveIdentity.archiveCode(context.packageManager, apk)
        val haveStart = ApkArchiveIdentity.versionCode(context.packageManager, pkg)
        if (pkg.isEmpty() || !waitNeeded(haveStart, want)) return false
        RefreshTrace.line("update all root wait")
        val end = System.currentTimeMillis() + WAIT_MS
        while (System.currentTimeMillis() < end) {
            if (waitOk(haveStart, want, ApkArchiveIdentity.versionCode(context.packageManager, pkg))) return true
            Thread.sleep(500)
        }
        return false
    }

    internal fun waitNeeded(haveStart: Long, want: Long): Boolean = want > 0L && haveStart < want

    internal fun waitOk(haveStart: Long, want: Long, haveNow: Long): Boolean =
        waitNeeded(haveStart, want) && haveNow >= want

    internal fun sessionId(output: String): String? =
        Regex("""\[(\d+)\]""").find(output)?.groupValues?.get(1)

    internal fun createArgs(): List<String> = listOf("su", "-c", "pm install-create -r --user 0")

    internal fun writeArgs(sessionId: String, index: Int, file: File): List<String>? {
        if (sessionId.any { !it.isDigit() }) return null
        val path = file.absolutePath
        if (RootPmInstall.args(path) == null) return null
        return listOf("su", "-c", "pm install-write -S ${file.length()} $sessionId split-$index.apk \"$path\"")
    }

    internal fun commitArgs(sessionId: String): List<String> =
        listOf("su", "-c", "pm install-commit $sessionId")

    internal fun abandonArgs(sessionId: String): List<String> =
        listOf("su", "-c", "pm install-abandon $sessionId")
}
