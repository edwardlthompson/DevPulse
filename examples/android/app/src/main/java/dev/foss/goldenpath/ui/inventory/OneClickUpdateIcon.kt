package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.ApkArchiveIdentity
import dev.foss.goldenpath.inventory.ApkHttpFetcher
import dev.foss.goldenpath.inventory.ApkInstall
import dev.foss.goldenpath.inventory.InstallMethod
import dev.foss.goldenpath.inventory.InstalledIdentity
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.InventoryPreferences
import dev.foss.goldenpath.index.apkpure.ApkPureDirect
import dev.foss.goldenpath.index.apkpure.ApkPureHttpFetcher
import dev.foss.goldenpath.index.aurora.AuroraPlayDirect
import dev.foss.goldenpath.index.aurora.AuroraPlayLive
import dev.foss.goldenpath.inventory.OneClickKind
import dev.foss.goldenpath.inventory.OneClickUpdate
import dev.foss.goldenpath.inventory.PlayStoreIntent
import dev.foss.goldenpath.inventory.UpdateArtifactMemory
import dev.foss.goldenpath.inventory.WelcomeNeeds
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun OneClickUpdateIcon(app: InstalledApp, modifier: Modifier = Modifier) {
    val revision by UpdateArtifactMemory.revision.collectAsStateWithLifecycle()
    val kind = remember(app.packageName, app.latestListings, revision) {
        OneClickUpdate.kind(app.packageName, app.latestListings)
    }
    val context = LocalContext.current
    val prefs = remember { InventoryPreferences(context) }
    val method by prefs.installMethod.collectAsStateWithLifecycle(InstallMethod.System)
    val aurora by prefs.auroraPlayEnabled.collectAsStateWithLifecycle(false)
    val scope = rememberCoroutineScope()
    var busy by remember(app.packageName) { mutableStateOf(false) }
    val label = stringResource(
        when (kind) {
            is OneClickKind.Play -> if (aurora) R.string.update_one_click_aurora else R.string.update_one_click_play
            is OneClickKind.ApkPure -> R.string.update_one_click_apkpure
            else -> R.string.update_one_click
        },
    )
    Icon(
        imageVector = Icons.Filled.SystemUpdate,
        contentDescription = label,
        tint = MaterialTheme.colorScheme.error,
        modifier = modifier.clickable(enabled = !busy && kind !is OneClickKind.None, role = Role.Button) {
            if (busy) return@clickable
            if (!WelcomeNeeds.ensureInstall(context)) return@clickable
            busy = true
            scope.launch {
                withContext(Dispatchers.IO) {
                    OneClickUpdate.apply(
                        kind = kind,
                        cacheDir = File(context.cacheDir, "updates"),
                        fetch = ApkHttpFetcher,
                        install = { file -> ApkInstall.apply(context, file, method) },
                        openPlay = { PlayStoreIntent.open(context, it) },
                        resolveApkPure = { pkg -> ApkPureDirect.resolve(pkg, ApkPureHttpFetcher) },
                        resolveAurora = { pkg ->
                            if (aurora) AuroraPlayDirect.resolve(pkg, AuroraPlayLive.files(context)) else null
                        },
                        inspect = { file -> ApkArchiveIdentity.inspect(context.packageManager, file) },
                            installed = ApkArchiveIdentity.installed(context.packageManager, app.packageName)
                                ?: InstalledIdentity(app.packageName, emptySet()),
                            filesDir = context.filesDir,
                    )
                }
                busy = false
            }
        },
    )
}
