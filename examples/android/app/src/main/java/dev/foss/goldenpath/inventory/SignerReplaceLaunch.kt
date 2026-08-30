package dev.foss.goldenpath.inventory

import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SignerReplaceLaunch {
    const val EXTRA_APK = "signer_replace_apk"

    fun maybeStart(activity: ComponentActivity, intent: Intent?) {
        val name = intent?.getStringExtra(EXTRA_APK)?.trim().orEmpty()
        intent?.removeExtra(EXTRA_APK)
        if (name.isEmpty() || name.contains('/') || name.contains('\\') || ".." in name) return
        val apk = File(activity.cacheDir, "updates/$name")
        activity.lifecycleScope.launch(Dispatchers.IO) {
            if (!apk.isFile || apk.length() <= 0L) {
                Log.i("DevPulse", "signer replace smoke missing $name")
                return@launch
            }
            val inspect = ApkArchiveIdentity.inspect(activity.packageManager, apk)
            val pkg = inspect.packageName?.trim().orEmpty()
            val installed = pkg.takeIf { it.isNotEmpty() }?.let {
                ApkArchiveIdentity.installed(activity.packageManager, it)
            }
            val system = SignerReplaceLive.systemApp(activity, pkg)
            if (installed == null || !SignerClash.offer(pkg, inspect.packageName, inspect.signers, installed.signers, system)) {
                Log.i("DevPulse", "signer replace smoke refused $pkg")
                return@launch
            }
            val label = runCatching {
                activity.packageManager.getApplicationLabel(
                    activity.packageManager.getApplicationInfo(pkg, 0),
                ).toString()
            }.getOrDefault(pkg)
            SignerReplaceStore.capture(activity.filesDir, pkg, label, RemoteReleasedSource.Fdroid, listOf(apk))
            Log.i("DevPulse", "signer replace smoke offer $pkg")
        }
    }
}
