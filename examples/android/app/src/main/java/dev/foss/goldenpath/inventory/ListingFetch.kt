package dev.foss.goldenpath.inventory

import android.content.Context

object ListingFetch {
    fun run(
        context: Context,
        packageName: String,
        link: UpdateLink,
        installedVersion: String?,
        method: InstallMethod,
        onProgress: (Long, Long) -> Unit,
    ): Int? {
        if (!ListingNewer.allow(link.versionName, installedVersion)) {
            return InventoryCopy.failRes(InstallWhy.Older, link.source)
        }
        if (!ListingFit.allow(link, android.os.Build.VERSION.SDK_INT, android.os.Build.SUPPORTED_ABIS.toSet())) {
            return InventoryCopy.failRes(InstallWhy.Sdk, link.source)
        }
        val files = ListingInstallLive.prepare(
            context,
            packageName,
            link.source,
            link.url,
            onProgress,
            installedVersion,
        )
        return when (val result = ListingInstallLive.install(context, files, method)) {
            is OneClickResult.Failed -> {
                IgnoredUpdates.add(packageName, link.source, link.versionName, context.filesDir)
                InventoryCopy.failRes(result.why, link.source)
            }
            else -> null
        }
    }
}
