package dev.foss.goldenpath.inventory

import android.content.Context
import dev.foss.goldenpath.index.fdroid.FdroidApkUrl
import dev.foss.goldenpath.index.fdroid.FdroidIndexStore
import dev.foss.goldenpath.index.fdroid.FdroidPackageVersions
import dev.foss.goldenpath.index.fdroid.FdroidRepoCatalog
import dev.foss.goldenpath.index.forge.FileGithubVerifiedStore
import dev.foss.goldenpath.index.forge.FilePastedRepoStore
import dev.foss.goldenpath.index.forge.GitHubReleaseParser
import java.io.File

object AppVersionHistory {
    fun query(
        context: Context,
        app: InstalledApp,
        cachedReleasesJson: String? = null,
    ): List<AppVersionItem> {
        val items = mutableListOf<AppVersionItem>()
        val pkg = app.packageName
        val installedVer = app.versionName?.trim().orEmpty()
        val installedCode = app.versionCode

        // 1. F-Droid Cache (all repos)
        val fdroidDir = File(context.filesDir, "fdroid-index")
        val store = FdroidIndexStore(fdroidDir)
        val nowMs = System.currentTimeMillis()
        FdroidRepoCatalog.defaults().forEach { repo ->
            val raw = store.load(repo.id, nowMs)
            if (raw != null) {
                val versions = FdroidPackageVersions.allFor(raw, pkg)
                val source = ListingChannels.sourceForRepo(repo.id)
                versions.forEach { rec ->
                    val url = FdroidApkUrl.of(repo.id, rec.apkName)
                    items += AppVersionItem(
                        versionName = rec.versionName,
                        versionCode = rec.versionCode,
                        releasedAtMs = rec.addedMs,
                        source = source,
                        downloadUrl = url,
                        state = resolveState(rec.versionName, rec.versionCode, installedVer, installedCode),
                    )
                }
            }
        }

        // 2. GitHub Releases
        val releasesJson = cachedReleasesJson ?: runCatching {
            val verified = FileGithubVerifiedStore(File(context.filesDir, "github_verified.tsv")).load()[pkg]
            val pasted = FilePastedRepoStore(File(context.filesDir, "pasted_repos.tsv")).load()[pkg]
            val ownerRepo = verified ?: pasted
            if (!ownerRepo.isNullOrBlank()) {
                ListingInstallFetch.releases(context, ownerRepo)
            } else null
        }.getOrNull()

        if (!releasesJson.isNullOrBlank()) {
            val records = GitHubReleaseParser.parse(releasesJson)
            records.forEach { rec ->
                val vName = rec.versionName
                val apk = GitHubReleaseParser.bestApkUrl(rec.apkUrls, pkg) ?: rec.apkUrl ?: rec.apkUrls.firstOrNull()
                if (!vName.isNullOrBlank()) {
                    items += AppVersionItem(
                        versionName = vName,
                        versionCode = null,
                        releasedAtMs = rec.publishedAtMs,
                        source = RemoteReleasedSource.Forge,
                        downloadUrl = apk,
                        state = resolveState(vName, null, installedVer, installedCode),
                    )
                }
            }
        }

        // 3. Known UpdateArtifacts
        UpdateArtifactMemory.byPackage[pkg]?.forEach { art ->
            if (!art.versionName.isNullOrBlank() && !art.downloadUrl.isNullOrBlank()) {
                items += AppVersionItem(
                    versionName = art.versionName,
                    versionCode = art.versionCode,
                    releasedAtMs = null,
                    source = art.source,
                    downloadUrl = art.downloadUrl,
                    state = resolveState(art.versionName, art.versionCode, installedVer, installedCode),
                )
            }
        }

        // 4. Latest Listings
        app.latestListings.forEach { link ->
            if (link.listed && !link.versionName.isNullOrBlank()) {
                val direct = UpdateArtifactMemory.forSource(pkg, link.source)?.downloadUrl ?: link.url
                items += AppVersionItem(
                    versionName = link.versionName,
                    versionCode = null,
                    releasedAtMs = link.releasedAtMs,
                    source = link.source,
                    downloadUrl = direct,
                    state = resolveState(link.versionName, null, installedVer, installedCode),
                )
            }
        }

        // 5. Always include currently installed version
        if (installedVer.isNotEmpty()) {
            items += AppVersionItem(
                versionName = installedVer,
                versionCode = installedCode,
                releasedAtMs = app.installedAtMs,
                source = app.remoteVersionSource.takeIf { it != RemoteReleasedSource.None } ?: RemoteReleasedSource.None,
                downloadUrl = null,
                state = AppVersionState.Current,
            )
        }

        return rankAndCap(items, installedVer, installedCode)
    }

    fun rankAndCap(
        items: List<AppVersionItem>,
        installedVersion: String?,
        installedCode: Long = 0,
        maxCount: Int = 5,
    ): List<AppVersionItem> = AppVersionRanking.rankAndCap(items, installedVersion, installedCode, maxCount)

    fun resolveState(
        candidateVersion: String?,
        candidateCode: Long?,
        installedVersion: String?,
        installedCode: Long = 0,
    ): AppVersionState = AppVersionRanking.resolveState(candidateVersion, candidateCode, installedVersion, installedCode)
}
