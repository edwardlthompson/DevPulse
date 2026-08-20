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

    fun start(context: Context, apkFile: File) {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        applyOwnership(context, apkFile, params)
        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            session.openWrite(NAME, 0, apkFile.length()).use { out ->
                apkFile.inputStream().use { input -> input.copyTo(out) }
                session.fsync(out)
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0
            val sender = PendingIntent.getBroadcast(
                context,
                sessionId,
                Intent(context, InstallStatusReceiver::class.java).setAction(ACTION),
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
            return
        }
        if (status != PackageInstaller.STATUS_PENDING_USER_ACTION) return
        @Suppress("DEPRECATION")
        val confirm = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT) ?: return
        confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(confirm)
    }
}
