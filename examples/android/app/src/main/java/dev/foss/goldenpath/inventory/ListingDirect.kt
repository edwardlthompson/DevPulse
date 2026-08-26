package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.apkmirror.ApkMirrorDirect
import dev.foss.goldenpath.index.fdroid.FdroidListing
import dev.foss.goldenpath.index.forge.ForgeListing
import dev.foss.goldenpath.index.forge.GithubAppOpt

object ListingDirect {
    fun resolve(
        packageName: String,
        source: RemoteReleasedSource,
        pageUrl: String? = null,
        fetchPage: (String) -> String? = { null },
        fetchReleases: (String) -> String? = { null },
        resolveApkPure: (String) -> UpdateArtifact? = { null },
        resolveAptoide: (String) -> UpdateArtifact? = { null },
        resolvePlay: (String) -> UpdateArtifact? = { null },
        fdroidCache: (String, RemoteReleasedSource) -> UpdateArtifact? = { _, _ -> null },
        githubOpt: GithubAppOpt? = null,
        directApkUrl: String? = null,
    ): UpdateArtifact? {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || source == RemoteReleasedSource.None) return null
        UpdateArtifactMemory.forSource(pkg, source)?.let { return it }
        return when (source) {
            RemoteReleasedSource.Fdroid,
            RemoteReleasedSource.Izzy,
            RemoteReleasedSource.Archive,
            RemoteReleasedSource.Guardian,
            RemoteReleasedSource.Calyx,
            RemoteReleasedSource.ExtraRepo,
            -> fdroid(pkg, source, pageUrl, fetchPage, fdroidCache)
            RemoteReleasedSource.Forge -> {
                directApkUrl?.let { href ->
                    ApkDownloadUrl.httpsFile(href)?.let { url ->
                        return UpdateArtifact(pkg, RemoteReleasedSource.Forge, url, null)
                    }
                }
                forge(pkg, pageUrl, fetchReleases, githubOpt)
            }
            RemoteReleasedSource.ApkPure -> resolveApkPure(pkg)
            RemoteReleasedSource.Aptoide -> resolveAptoide(pkg)
            RemoteReleasedSource.Play -> resolvePlay(pkg)
            RemoteReleasedSource.ApkMirror -> ApkMirrorDirect.resolve(pkg, pageUrl, fetchPage)
            RemoteReleasedSource.None -> null
        }
    }

    private fun fdroid(
        pkg: String,
        source: RemoteReleasedSource,
        pageUrl: String?,
        fetchPage: (String) -> String?,
        fdroidCache: (String, RemoteReleasedSource) -> UpdateArtifact?,
    ): UpdateArtifact? {
        val htmlUrl = htmlUrl(source, pkg, pageUrl)
        val fromPage = htmlUrl?.let(fetchPage)?.let { FdroidListing.fromHtml(pkg, source, it) }
        return fromPage ?: fdroidCache(pkg, source)
    }

    private fun htmlUrl(source: RemoteReleasedSource, pkg: String, pageUrl: String?): String? {
        FdroidListing.pageUrl(source, pkg)?.let { return it }
        val listed = pageUrl?.trim().orEmpty()
        if (!listed.startsWith("https://")) return null
        if (source != RemoteReleasedSource.Fdroid && listed.contains("f-droid.org/packages/")) return null
        return listed
    }

    private fun forge(
        pkg: String,
        pageUrl: String?,
        fetchReleases: (String) -> String?,
        opt: GithubAppOpt?,
    ): UpdateArtifact? {
        val repo = ForgeListing.ownerRepo(pageUrl) ?: return null
        val json = fetchReleases(repo) ?: return null
        return ForgeListing.fromReleases(pkg, json, opt)
    }
}
