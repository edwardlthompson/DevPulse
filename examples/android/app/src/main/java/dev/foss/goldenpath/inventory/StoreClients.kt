package dev.foss.goldenpath.inventory

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import dev.foss.goldenpath.index.apkpure.ApkPureLink
import dev.foss.goldenpath.index.aptoide.AptoideLink

enum class StoreClientId {
    Play, Fdroid, Droidify, Izzy, Guardian, Calyx, Aptoide, ApkMirror, ApkPure, GitHub
}

enum class StoreUrlKind { Play, Fdroid, Apk, Web, Repo }

data class StoreUrl(val kind: StoreUrlKind, val url: String)

data class StoreClient(
    val id: StoreClientId,
    val packageName: String? = null,
    val urls: List<StoreUrl>,
)

object StoreClients {
    fun all(): List<StoreClient> = listOf(
        StoreClient(
            StoreClientId.Play,
            PlayStoreIntent.STORE_PACKAGE,
            listOf(StoreUrl(StoreUrlKind.Play, PlayStoreIntent.marketUri(PlayStoreIntent.STORE_PACKAGE))),
        ),
        StoreClient(
            StoreClientId.Fdroid,
            "org.fdroid.fdroid",
            listOf(
                StoreUrl(StoreUrlKind.Apk, "https://f-droid.org/F-Droid.apk"),
                StoreUrl(StoreUrlKind.Fdroid, "https://f-droid.org/packages/org.fdroid.fdroid/"),
                StoreUrl(StoreUrlKind.Web, "https://f-droid.org/"),
            ),
        ),
        StoreClient(
            StoreClientId.Droidify,
            "com.looker.droidify",
            listOf(
                StoreUrl(StoreUrlKind.Fdroid, "https://f-droid.org/packages/com.looker.droidify/"),
                StoreUrl(StoreUrlKind.Web, "https://github.com/Droid-ify/client/releases"),
            ),
        ),
        StoreClient(
            StoreClientId.Izzy,
            urls = listOf(
                StoreUrl(
                    StoreUrlKind.Repo,
                    "fdroidrepo://apt.izzysoft.de/fdroid/repo?fingerprint=3BF0D6ABFEAE2F401707B6D966BE743BF0EEE49C2561B9BA39041BB262C76DAE",
                ),
                StoreUrl(StoreUrlKind.Web, "https://apt.izzysoft.de/fdroid/index/info"),
            ),
        ),
        StoreClient(
            StoreClientId.Guardian,
            urls = listOf(StoreUrl(StoreUrlKind.Web, "https://guardianproject.info/fdroid/")),
        ),
        StoreClient(
            StoreClientId.Calyx,
            urls = listOf(StoreUrl(StoreUrlKind.Web, "https://calyxos.org/calyx-fdroid-repo/")),
        ),
        StoreClient(
            StoreClientId.Aptoide,
            AptoideLink.STORE_PACKAGE,
            listOf(
                StoreUrl(StoreUrlKind.Web, AptoideLink.INSTALL_PAGE),
                StoreUrl(StoreUrlKind.Play, PlayStoreIntent.marketUri(AptoideLink.STORE_PACKAGE)),
                StoreUrl(StoreUrlKind.Web, "https://play.google.com/store/apps/details?id=${AptoideLink.STORE_PACKAGE}"),
            ),
        ),
        StoreClient(
            StoreClientId.ApkMirror,
            "com.apkmirror.helper.prod",
            listOf(
                StoreUrl(StoreUrlKind.Web, "https://www.apkmirror.com/"),
                StoreUrl(StoreUrlKind.Web, "https://www.apkmirror.com/apk/apkmirror/apkmirror-installer/"),
            ),
        ),
        StoreClient(
            StoreClientId.ApkPure,
            ApkPureLink.STORE_PACKAGE,
            listOf(
                StoreUrl(StoreUrlKind.Web, "https://apkpure.com/apkpure/com.apkpure.aegon"),
                StoreUrl(StoreUrlKind.Play, PlayStoreIntent.marketUri(ApkPureLink.STORE_PACKAGE)),
            ),
        ),
        StoreClient(
            StoreClientId.GitHub,
            urls = listOf(StoreUrl(StoreUrlKind.Web, "https://github.com/")),
        ),
    )

    fun isAptoideGames(pm: PackageManager): Boolean {
        val launch = pm.getLaunchIntentForPackage(AptoideLink.STORE_PACKAGE) ?: return false
        return AptoideLink.isGamesClient(launch.component?.className)
    }

    fun installed(pm: PackageManager, packageName: String?): Boolean {
        val pkg = packageName?.trim()?.takeIf { it.isNotEmpty() } ?: return false
        return runCatching { pm.getPackageInfo(pkg, 0); true }.getOrDefault(false)
    }

    fun open(context: Context, client: StoreClient, url: String? = null) {
        val pm = context.packageManager
        if (url != null) {
            startView(context, url)
            return
        }
        val pkg = client.packageName
        val games = client.id == StoreClientId.Aptoide && isAptoideGames(pm)
        if (!games && pkg != null) {
            pm.getLaunchIntentForPackage(pkg)?.let {
                runCatching { context.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                return
            }
        }
        client.urls.firstOrNull()?.url?.let { startView(context, it) }
    }

    private fun startView(context: Context, url: String) {
        val uri = url.trim().takeIf { it.isNotEmpty() } ?: return
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
