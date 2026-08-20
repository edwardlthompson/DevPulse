package dev.foss.goldenpath.inventory

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object AppDetailsIntent {
    fun forPackage(packageName: String): Intent? {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return null
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", pkg, null))
    }

    fun open(context: Context, packageName: String) {
        val intent = forPackage(packageName) ?: return
        runCatching { context.startActivity(intent) }
    }
}
