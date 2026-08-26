package dev.foss.goldenpath.inventory

import android.content.Context
import dev.foss.goldenpath.index.aptoide.AptoideHttpFetcher
import dev.foss.goldenpath.index.aptoide.AptoideScan
import dev.foss.goldenpath.index.forge.EncryptedForgeTokenStore
import dev.foss.goldenpath.index.forge.GitHubSearchHttp

object ListingInstallFetch {
    fun aptoide(packageName: String): UpdateArtifact? {
        AptoideScan.toPick(
            AptoideScan.lookupOne(packageName, AptoideHttpFetcher, System.currentTimeMillis(), force = true),
            packageName,
        )
        return UpdateArtifactMemory.forSource(packageName, RemoteReleasedSource.Aptoide)
    }

    fun releases(context: Context, ownerRepo: String): String? {
        val page = GitHubSearchHttp(EncryptedForgeTokenStore.wrap(context).getToken()).listReleases(ownerRepo)
        return page.body.takeIf { page.statusCode in 200..299 }
    }
}
