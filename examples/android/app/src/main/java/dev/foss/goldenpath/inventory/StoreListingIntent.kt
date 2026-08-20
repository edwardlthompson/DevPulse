package dev.foss.goldenpath.inventory

import android.content.Context
import android.content.Intent
import android.net.Uri
import dev.foss.goldenpath.index.aptoide.AptoideLink

object StoreListingIntent {
    fun viewIntent(
        url: String,
        source: RemoteReleasedSource,
        aptoideInstalled: Boolean,
        packageName: String? = null,
    ): Intent? {
        val page = url.trim().takeIf { it.startsWith("http") } ?: return null
        if (source == RemoteReleasedSource.Aptoide && aptoideInstalled) {
            return Intent(Intent.ACTION_VIEW, Uri.parse(AptoideLink.appOpenUri(page, packageName)))
                .setPackage(AptoideLink.STORE_PACKAGE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return Intent(Intent.ACTION_VIEW, Uri.parse(page)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun open(context: Context, url: String, source: RemoteReleasedSource, packageName: String? = null) {
        val installed = aptoideInstalled(context)
        val preferred = viewIntent(url, source, installed, packageName) ?: return
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

    private fun aptoideInstalled(context: Context): Boolean = runCatching {
        context.packageManager.getPackageInfo(AptoideLink.STORE_PACKAGE, 0)
        true
    }.getOrDefault(false)
}
