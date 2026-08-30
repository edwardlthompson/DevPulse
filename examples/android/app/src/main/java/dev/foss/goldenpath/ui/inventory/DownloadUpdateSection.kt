package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.ApkArchiveIdentity
import dev.foss.goldenpath.inventory.ApkHttpFetcher
import dev.foss.goldenpath.inventory.ApkInstall
import dev.foss.goldenpath.inventory.IgnoredUpdates
import dev.foss.goldenpath.inventory.InstallMethod
import dev.foss.goldenpath.inventory.InstalledIdentity
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.InstallWhy
import dev.foss.goldenpath.inventory.InventoryCopy
import dev.foss.goldenpath.inventory.InventoryPreferences
import dev.foss.goldenpath.inventory.OneClickKind
import dev.foss.goldenpath.inventory.OneClickResult
import dev.foss.goldenpath.inventory.OneClickUpdate
import dev.foss.goldenpath.inventory.PlayStoreIntent
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.SignerClash
import dev.foss.goldenpath.inventory.SignerReplaceStore
import dev.foss.goldenpath.index.apkpure.ApkPureDirect
import dev.foss.goldenpath.index.apkpure.ApkPureHttpFetcher
import dev.foss.goldenpath.index.aurora.AuroraPlayDirect
import dev.foss.goldenpath.index.aurora.AuroraPlayLive
import dev.foss.goldenpath.inventory.UpdateArtifactMemory
import dev.foss.goldenpath.inventory.WelcomeNeeds
import dev.foss.goldenpath.ui.theme.SpacingSm
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DownloadUpdateSection(app: InstalledApp, modifier: Modifier = Modifier) {
    val revision by UpdateArtifactMemory.revision.collectAsStateWithLifecycle()
    val kind = remember(app.packageName, app.latestListings, revision) {
        OneClickUpdate.kind(app.packageName, app.latestListings)
    }
    if (kind is OneClickKind.None) return
    val context = LocalContext.current
    val prefs = remember { InventoryPreferences(context) }
    val method by prefs.installMethod.collectAsStateWithLifecycle(InstallMethod.System)
    val aurora by prefs.auroraPlayEnabled.collectAsStateWithLifecycle(false)
    val scope = rememberCoroutineScope()
    var busy by remember(app.packageName) { mutableStateOf(false) }
    var failRes by remember(app.packageName) { mutableStateOf<Int?>(null) }
    val label = stringResource(
        when {
            busy -> R.string.update_one_click_busy
            kind is OneClickKind.Play && aurora -> R.string.update_one_click_aurora
            kind is OneClickKind.Play -> R.string.update_one_click_play
            kind is OneClickKind.ApkPure -> R.string.update_one_click_apkpure
            else -> R.string.update_one_click
        },
    )
    val from = when (kind) {
        is OneClickKind.Direct -> stringResource(InventoryCopy.sourceRes(kind.artifact.source))
        is OneClickKind.Play -> stringResource(R.string.inventory_source_play)
        is OneClickKind.ApkPure -> stringResource(R.string.inventory_source_apkpure)
        OneClickKind.None -> ""
    }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        Text(text = stringResource(R.string.update_cache_title), style = MaterialTheme.typography.titleMedium)
        Text(text = stringResource(R.string.update_cache_from, from), style = MaterialTheme.typography.bodySmall)
        TextButton(
            onClick = {
                if (busy) return@TextButton
                if (method != InstallMethod.Session && !WelcomeNeeds.ensureInstall(context)) return@TextButton
                busy = true
                failRes = null
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
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
                    failRes = when (result) {
                        is OneClickResult.Failed -> {
                            val apk = result.files.firstOrNull()?.let {
                                ApkArchiveIdentity.inspect(context.packageManager, it)
                            }
                            val device = ApkArchiveIdentity.installed(context.packageManager, app.packageName)
                            if (
                                result.why == InstallWhy.Signing &&
                                result.files.isNotEmpty() &&
                                apk != null &&
                                SignerClash.offer(
                                    app.packageName,
                                    apk.packageName,
                                    apk.signers,
                                    device?.signers.orEmpty(),
                                    app.isSystemApp,
                                ) &&
                                SignerReplaceStore.capture(
                                    context.filesDir,
                                    app.packageName,
                                    app.label,
                                    when (kind) {
                                        is OneClickKind.Direct -> kind.artifact.source
                                        is OneClickKind.Play -> RemoteReleasedSource.Play
                                        is OneClickKind.ApkPure -> RemoteReleasedSource.ApkPure
                                        OneClickKind.None -> RemoteReleasedSource.None
                                    },
                                    result.files,
                                )
                            ) {
                                IgnoredUpdates.drop(app.packageName, context.filesDir)
                                null
                            } else {
                                InventoryCopy.failRes(result.why)
                            }
                        }
                        else -> null
                    }
                }
            },
            enabled = !busy,
            modifier = Modifier.semantics { contentDescription = label },
        ) { Text(label) }
        if (kind is OneClickKind.Play && aurora) {
            val playLabel = stringResource(R.string.update_one_click_play)
            TextButton(
                onClick = { PlayStoreIntent.open(context, app.packageName) },
                enabled = !busy,
                modifier = Modifier.semantics { contentDescription = playLabel },
            ) { Text(playLabel) }
        }
        failRes?.let { res ->
            Text(text = stringResource(res), color = MaterialTheme.colorScheme.error)
            if (res == R.string.update_all_play_purchase || res == R.string.update_all_play_store) {
                TextButton(onClick = { PlayStoreIntent.open(context, app.packageName) }) {
                    Text(text = stringResource(R.string.update_all_play_purchase_open))
                }
            }
        }
    }
}
