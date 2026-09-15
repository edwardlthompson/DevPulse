package dev.foss.goldenpath.inventory

import android.content.Context
import dev.foss.goldenpath.index.aptoide.AptoideHttpFetcher
import dev.foss.goldenpath.index.aptoide.AptoideMetaFetcher
import dev.foss.goldenpath.index.aptoide.AptoideScan
import dev.foss.goldenpath.index.aptoide.AptoideUpdatesFetcher
import dev.foss.goldenpath.index.aptoide.AptoideUpdatesHttp
import dev.foss.goldenpath.index.forge.EncryptedForgeTokenStore
import dev.foss.goldenpath.index.forge.GitHubSearchHttp

object ListingInstallFetch {
    fun aptoide(context: Context, packageName: String): UpdateArtifact? = aptoide(
        packageName,
        ApkArchiveIdentity.signingSha1(context.packageManager, packageName),
        ApkArchiveIdentity.versionCode(context.packageManager, packageName),
    )

    fun aptoide(
        packageName: String,
        signingSha1: String?,
        versionCode: Long = 0,
        updates: AptoideUpdatesFetcher = AptoideUpdatesHttp,
        meta: AptoideMetaFetcher = AptoideHttpFetcher,
        nowMs: Long = System.currentTimeMillis(),
    ): UpdateArtifact? = AptoideScan.lookupForInstall(
        packageName,
        signingSha1,
        versionCode,
        updates,
        meta,
        nowMs,
    )

    fun releases(context: Context, ownerRepo: String): String? {
        val page = GitHubSearchHttp(EncryptedForgeTokenStore.wrap(context).getToken()).listReleases(ownerRepo)
        return page.body.takeIf { page.statusCode in 200..299 }
    }
}
