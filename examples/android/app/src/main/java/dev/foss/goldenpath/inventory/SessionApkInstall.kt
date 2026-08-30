package dev.foss.goldenpath.inventory

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import java.io.File

object SessionApkInstall {
    const val ACTION = "dev.foss.goldenpath.INSTALL_SESSION"
    private const val NAME = "update.apk"

    fun start(context: Context, apkFile: File) = start(context, listOf(apkFile))

    fun commit(context: Context, apkFiles: List<File>): Boolean {
        if (!SignerClash.filesReady(apkFiles)) return false
        return runCatching { start(context.applicationContext, apkFiles) }.isSuccess
    }

    fun start(context: Context, apkFiles: List<File>) {
        val app = context.applicationContext
        val files = apkFiles.filter { it.isFile }
        if (files.isEmpty()) error("apk")
        val installer = app.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        applyOwnership(app, files.first(), params)
        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            files.forEachIndexed { index, apkFile ->
                val name = if (files.size == 1) NAME else "split-$index.apk"
                session.openWrite(name, 0, apkFile.length()).use { out ->
                    apkFile.inputStream().use { input -> input.copyTo(out) }
                    session.fsync(out)
                }
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0
            val sender = PendingIntent.getBroadcast(
                app,
                sessionId,
                Intent(app, InstallStatusReceiver::class.java).setAction(ACTION),
                flags,
            ).intentSender
            session.commit(sender)
        }
    }

    internal fun applyOwnership(context: Context, apkFile: File, params: PackageInstaller.SessionParams) {
        val pkg = ApkArchiveIdentity.inspect(context.packageManager, apkFile).packageName ?: return
        params.setAppPackageName(pkg)
        val installed = ApkArchiveIdentity.installed(context.packageManager, pkg) != null
        val owner = SessionUpdateOwnership.installerPackage(context.packageManager, pkg)
        if (SessionUpdateOwnership.requestNoUserAction(
                Build.VERSION.SDK_INT,
                installed,
                owner,
                context.packageName,
            )
        ) {
            params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        }
    }
}

class InstallStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        if (status == PackageInstaller.STATUS_SUCCESS) {
            InstalledAppsRevision.bump()
            InstallAwait.signal(true)
            return
        }
        if (status != PackageInstaller.STATUS_PENDING_USER_ACTION) {
            android.util.Log.i("DevPulse", "signer replace install status $status")
            InstallAwait.signal(false)
            return
        }
        InstallAwait.markPending()
        @Suppress("DEPRECATION")
        val confirm = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        if (confirm == null) {
            android.util.Log.i("DevPulse", "signer replace install ui missing")
            return
        }
        confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(confirm) }
            .onFailure { android.util.Log.i("DevPulse", "signer replace install ui ${it.message}") }
    }
}
