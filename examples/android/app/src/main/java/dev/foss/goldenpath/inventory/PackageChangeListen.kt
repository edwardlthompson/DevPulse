package dev.foss.goldenpath.inventory

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

object PackageChangeListen {
    fun start(context: Context): () -> Unit {
        val app = context.applicationContext
        val receiver = Receiver()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        runCatching {
            if (Build.VERSION.SDK_INT >= 33) {
                app.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                app.registerReceiver(receiver, filter)
            }
        }
        return { runCatching { app.unregisterReceiver(receiver) } }
    }

    internal class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (PackageChange.shouldReload(
                    action = intent?.action,
                    packageName = intent?.data?.schemeSpecificPart,
                    replacing = intent?.getBooleanExtra(Intent.EXTRA_REPLACING, false) == true,
                    selfPackage = context?.packageName,
                )
            ) {
                InstalledAppsRevision.bump()
            }
        }
    }
}
