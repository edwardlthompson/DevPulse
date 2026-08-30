package dev.foss.goldenpath.inventory

import android.content.Context
import java.io.File

sealed class ListingFetchOutcome {
    data object Ok : ListingFetchOutcome()
    data class Failed(val res: Int) : ListingFetchOutcome()
    data class Replace(val files: List<File>) : ListingFetchOutcome()
}

object ListingFetch {
    fun run(
        context: Context,
        packageName: String,
        link: UpdateLink,
        installedVersion: String?,
        installedCode: Long,
        method: InstallMethod,
        systemApp: Boolean,
        onProgress: (Long, Long) -> Unit,
    ): ListingFetchOutcome {
        if (!ListingNewer.allow(link.versionName, installedVersion, installedCode)) {
            return ListingFetchOutcome.Failed(InventoryCopy.failRes(InstallWhy.Older, link.source))
        }
        if (!ListingFit.allow(link, android.os.Build.VERSION.SDK_INT, android.os.Build.SUPPORTED_ABIS.toSet())) {
            return ListingFetchOutcome.Failed(InventoryCopy.failRes(InstallWhy.Sdk, link.source))
        }
        val files = ListingInstallLive.prepare(
            context,
            packageName,
            link.source,
            link.url,
            onProgress,
            installedVersion,
            installedCode,
        )
        if (files.isNullOrEmpty()) {
            val why = when {
                ListingFail.why == InstallWhy.PlayPurchase -> InstallWhy.PlayPurchase
                link.source == RemoteReleasedSource.Play && ListingFail.why == InstallWhy.NoFile ->
                    InstallWhy.PlayStore
                else -> ListingFail.why
            }
            if (why == InstallWhy.PlayPurchase || why == InstallWhy.PlayStore) {
                PlayStoreIntent.open(context, packageName)
            }
            return ListingFetchOutcome.Failed(InventoryCopy.failRes(why, link.source))
        }
        val apk = ApkArchiveIdentity.inspect(context.packageManager, files.first())
        val device = ApkArchiveIdentity.installed(context.packageManager, packageName)
        if (SignerClash.offer(packageName, apk.packageName, apk.signers, device?.signers.orEmpty(), systemApp)) {
            IgnoredUpdates.drop(packageName, context.filesDir)
            return ListingFetchOutcome.Replace(files)
        }
        return when (val result = ListingInstallLive.install(context, files, method)) {
            is OneClickResult.Failed -> {
                if (result.why == InstallWhy.Signing) {
                    ListingFetchOutcome.Failed(InventoryCopy.failRes(result.why, link.source))
                } else {
                    IgnoredUpdates.add(packageName, link.source, link.versionName, context.filesDir)
                    ListingFetchOutcome.Failed(InventoryCopy.failRes(result.why, link.source))
                }
            }
            else -> ListingFetchOutcome.Ok
        }
    }
}
