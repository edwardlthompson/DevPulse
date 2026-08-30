package dev.foss.goldenpath.inventory

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.util.Log

object UninstallIntent {
    const val ACTION = "dev.foss.goldenpath.UNINSTALL_SESSION"

    fun forPackage(packageName: String): Intent? {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return null
        return Intent(Intent.ACTION_DELETE).setData(Uri.fromParts("package", pkg, null))
    }

    fun launch(context: Context, packageName: String): Boolean {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return false
        if (session(context, pkg)) return true
        val intent = forPackage(pkg) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            activity(context)?.startActivity(intent) ?: context.startActivity(intent)
        }.isSuccess
    }

    private fun session(context: Context, packageName: String): Boolean {
        val app = context.applicationContext
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0
        val sender = PendingIntent.getBroadcast(
            app,
            packageName.hashCode() and 0x7fffffff,
            Intent(app, UninstallStatusReceiver::class.java).setAction(ACTION),
            flags,
        ).intentSender
        return runCatching {
            app.packageManager.packageInstaller.uninstall(packageName, sender)
        }.isSuccess
    }

    private fun activity(context: Context): Activity? {
        var cur: Context? = context
        while (cur is ContextWrapper) {
            if (cur is Activity) return cur
            cur = cur.baseContext
        }
        return null
    }
}

class UninstallStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        if (status == PackageInstaller.STATUS_SUCCESS) {
            InstalledAppsRevision.bump()
            return
        }
        if (status != PackageInstaller.STATUS_PENDING_USER_ACTION) {
            Log.i("DevPulse", "signer replace uninstall status $status")
            return
        }
        @Suppress("DEPRECATION")
        val confirm = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT) ?: return
        confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(confirm) }
            .onFailure { Log.i("DevPulse", "signer replace uninstall ui ${it.message}") }
    }
}
