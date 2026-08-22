package dev.foss.goldenpath.index.fdroid

import dev.foss.goldenpath.inventory.ListingChannels
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.UpdateArtifact
import dev.foss.goldenpath.inventory.UpdateArtifactMemory
import java.net.URLEncoder

object FdroidListing {
    fun pageUrl(source: RemoteReleasedSource, packageName: String): String? {
        val pkg = pathSeg(packageName) ?: return null
        return when (source) {
            RemoteReleasedSource.Fdroid -> "https://f-droid.org/packages/$pkg/"
            RemoteReleasedSource.Izzy -> "https://apt.izzysoft.de/fdroid/index/apk/$pkg"
            else -> null
        }
    }

    fun fromHtml(packageName: String, source: RemoteReleasedSource, html: String): UpdateArtifact? {
        val pkg = packageName.trim()
        val repo = ListingChannels.repoId(source) ?: return null
        if (pkg.isEmpty()) return null
        val name = FdroidPackagePage.parse(html, pkg).apkName ?: return null
        val url = FdroidApkUrl.of(repo, name) ?: return null
        UpdateArtifactMemory.add(UpdateArtifact(pkg, source, url))
        return UpdateArtifactMemory.forSource(pkg, source)
    }

    private fun pathSeg(raw: String): String? {
        val pkg = raw.trim()
        if (pkg.isEmpty()) return null
        return URLEncoder.encode(pkg, Charsets.UTF_8.name()).replace("+", "%20")
    }
}
