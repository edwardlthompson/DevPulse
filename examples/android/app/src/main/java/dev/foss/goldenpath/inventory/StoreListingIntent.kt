package dev.foss.goldenpath.inventory

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import dev.foss.goldenpath.index.apkpure.ApkPureLink
import dev.foss.goldenpath.index.aptoide.AptoideLink

object StoreListingIntent {
    fun storePackage(source: RemoteReleasedSource): String? = when (source) {
        RemoteReleasedSource.Aptoide -> AptoideLink.STORE_PACKAGE
        RemoteReleasedSource.ApkPure -> ApkPureLink.STORE_PACKAGE
        else -> null
    }

    fun viewIntent(
        url: String,
        source: RemoteReleasedSource,
        storeInstalled: Boolean,
        packageName: String? = null,
    ): Intent? {
        val page = url.trim().takeIf { it.startsWith("http") } ?: return null
        val store = storePackage(source)
        if (store != null && storeInstalled) {
            val uri = when (source) {
                RemoteReleasedSource.Aptoide -> AptoideLink.appOpenUri(page, packageName)
                RemoteReleasedSource.ApkPure -> ApkPureLink.appOpenUri(page, packageName)
                else -> page
            }
            return Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                .setPackage(store)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return Intent(Intent.ACTION_VIEW, Uri.parse(page)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun open(context: Context, url: String, source: RemoteReleasedSource, packageName: String? = null) {
        val store = storePackage(source)
        val installed = store != null && packageInstalled(context, store)
        val preferred = viewIntent(url, source, installed, packageName) ?: return
        Log.i("DevPulse", "store open $source app=${preferred.`package`} uri=${preferred.data}")
        runCatching { context.startActivity(preferred) }.onFailure {
            if (preferred.`package` == null) return
            val web = url.trim().takeIf { it.startsWith("http") } ?: return
            runCatching {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(web)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        }
    }

    fun openApkPure(context: Context, packageName: String) {
        open(context, ApkPureLink.webPage(packageName), RemoteReleasedSource.ApkPure, packageName)
    }

    private fun packageInstalled(context: Context, packageName: String): Boolean = runCatching {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    }.getOrDefault(false)
}
