package dev.foss.goldenpath.ui.about

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import dev.foss.goldenpath.about.ProductReleaseFetcher
import dev.foss.goldenpath.about.ProductUpdate
import dev.foss.goldenpath.about.ProductUpdatePrefs

@Composable
fun ProductUpdateHost(
    context: Context,
    isOnline: Boolean,
    currentVersion: String,
) {
    val prefs = remember { ProductUpdatePrefs(context) }
    val uriHandler = LocalUriHandler.current
    var donate by remember { mutableStateOf(false) }
    var update by remember { mutableStateOf<ProductUpdate.ProductAsset?>(null) }

    LaunchedEffect(isOnline, currentVersion) {
        if (ProductUpdate.shouldNudgeDonate(prefs.lastSeenVersion(), currentVersion)) {
            donate = true
            return@LaunchedEffect
        }
        prefs.markVersionSeen(currentVersion)
        if (!isOnline) return@LaunchedEffect
        if (!ProductUpdate.shouldCheckDaily(prefs.lastCheckAt(), System.currentTimeMillis())) {
            return@LaunchedEffect
        }
        val release = ProductReleaseFetcher.fetchLatest()
        prefs.markChecked(System.currentTimeMillis())
        val asset = release?.let { ProductUpdate.selectApkAsset(it.assets) } ?: return@LaunchedEffect
        if (!ProductUpdate.shouldPromptUpdate(currentVersion, asset.version, prefs.dismissedVersion())) {
            return@LaunchedEffect
        }
        update = ProductUpdate.ProductAsset(
            asset.version,
            ProductUpdate.installUrl(asset.url, release.htmlUrl),
            ProductUpdate.changelog(currentVersion, asset.version, release.body),
        )
    }

    if (donate) {
        DonateNudgeDialog(
            onDonate = {
                prefs.markVersionSeen(currentVersion)
                donate = false
                runCatching { uriHandler.openUri(ProductUpdate.VENMO_URL) }
            },
            onNotNow = {
                prefs.markVersionSeen(currentVersion)
                donate = false
            },
        )
    } else {
        val prompt = update
        if (prompt != null) {
            UpdateAvailableDialog(
                version = prompt.version,
                notes = prompt.notes,
                onInstall = {
                    prefs.markChecked(System.currentTimeMillis(), prompt.version)
                    update = null
                    runCatching { uriHandler.openUri(prompt.url) }
                },
                onLater = {
                    prefs.markChecked(System.currentTimeMillis(), prompt.version)
                    update = null
                },
            )
        }
    }
}
