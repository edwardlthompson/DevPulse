package dev.foss.goldenpath.inventory

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

enum class StoreClientId {
    Play, ApkMirror
}

enum class StoreUrlKind { Play, Web }

enum class StoreClientAction { Open }

data class StoreUrl(val kind: StoreUrlKind, val url: String)

data class StoreClient(
    val id: StoreClientId,
    val packageName: String? = null,
    val urls: List<StoreUrl>,
)

/** External pages only for Play fallback and APKMirror. Everything else stays in-app. */
object StoreClients {
    fun all(): List<StoreClient> = listOf(
        StoreClient(
            StoreClientId.Play,
            PlayStoreIntent.STORE_PACKAGE,
            listOf(StoreUrl(StoreUrlKind.Play, PlayStoreIntent.marketUri(PlayStoreIntent.STORE_PACKAGE))),
        ),
        StoreClient(
            StoreClientId.ApkMirror,
            urls = listOf(StoreUrl(StoreUrlKind.Web, "https://www.apkmirror.com/")),
        ),
    )

    @Suppress("UNUSED_PARAMETER")
    fun action(installed: Boolean, hasPackage: Boolean): StoreClientAction = StoreClientAction.Open

    fun installed(pm: PackageManager, packageName: String?): Boolean {
        val pkg = packageName?.trim()?.takeIf { it.isNotEmpty() } ?: return false
        return runCatching { pm.getPackageInfo(pkg, 0); true }.getOrDefault(false)
    }

    fun open(context: Context, client: StoreClient, url: String? = null) {
        if (url != null) {
            startView(context, url)
            return
        }
        val pkg = client.packageName
        if (pkg != null) {
            context.packageManager.getLaunchIntentForPackage(pkg)?.let {
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
