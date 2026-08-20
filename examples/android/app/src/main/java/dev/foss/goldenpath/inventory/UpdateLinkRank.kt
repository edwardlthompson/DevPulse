package dev.foss.goldenpath.inventory

import dev.foss.goldenpath.index.apkmirror.ApkMirrorLink
import dev.foss.goldenpath.index.apkpure.ApkPureLink
import dev.foss.goldenpath.index.aptoide.AptoideLink

object UpdateUrls {
    fun play(packageName: String): String =
        "https://play.google.com/store/apps/details?id=$packageName"

    fun aptoide(packageName: String, uname: String? = null): String =
        AptoideLink.webPage(packageName, uname)

    fun apkMirror(packageName: String, path: String? = null): String =
        ApkMirrorLink.webPage(packageName, path)

    fun apkPure(packageName: String): String = ApkPureLink.webPage(packageName)

    fun forFdroid(packageName: String, repoId: String, sourceCode: String?): String = when (repoId) {
        "official", "archive" -> "https://f-droid.org/packages/$packageName/"
        "izzy" -> "https://apt.izzysoft.de/fdroid/index/apk/$packageName"
        else -> sourceCode?.takeIf { it.startsWith("http") } ?: "https://f-droid.org/packages/$packageName/"
    }
}

object UpdateLinkRank {
    fun rank(source: RemoteReleasedSource): Int = when (source) {
        RemoteReleasedSource.Play -> 0
        RemoteReleasedSource.Fdroid -> 1
        RemoteReleasedSource.Archive -> 2
        RemoteReleasedSource.Izzy -> 3
        RemoteReleasedSource.Guardian -> 4
        RemoteReleasedSource.Calyx -> 5
        RemoteReleasedSource.Aptoide -> 6
        RemoteReleasedSource.ApkMirror -> 7
        RemoteReleasedSource.ApkPure -> 8
        RemoteReleasedSource.Forge -> 9
        RemoteReleasedSource.ExtraRepo -> 10
        RemoteReleasedSource.None -> 11
    }

    fun bestNonPlay(offers: List<RemoteReleasePick>): RemoteReleasePick? {
        val usable = offers.filter { !it.pageUrl.isNullOrBlank() && it.source != RemoteReleasedSource.None }
        if (usable.isEmpty()) return null
        val newest = usable.maxOf { it.ms ?: 0L }
        val tied = usable.filter { (it.ms ?: 0L) == newest }
        return tied.minBy { rank(it.source) }
    }
}
