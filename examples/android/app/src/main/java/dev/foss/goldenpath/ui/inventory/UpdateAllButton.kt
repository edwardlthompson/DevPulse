package dev.foss.goldenpath.ui.inventory

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
import android.util.Log
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.ApkArchiveIdentity
import dev.foss.goldenpath.inventory.ApkHttpFetcher
import dev.foss.goldenpath.inventory.ApkInstall
import dev.foss.goldenpath.inventory.InstallMethod
import dev.foss.goldenpath.inventory.InstalledIdentity
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.InventoryPreferences
import dev.foss.goldenpath.inventory.UpdateAll
import dev.foss.goldenpath.inventory.UpdateArtifactMemory
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun UpdateAllButton(apps: List<InstalledApp>, modifier: Modifier = Modifier) {
    val revision by UpdateArtifactMemory.revision.collectAsStateWithLifecycle()
    val queue = remember(apps, revision) { UpdateAll.artifacts(apps) }
    if (queue.isEmpty()) return
    val context = LocalContext.current
    val prefs = remember { InventoryPreferences(context) }
    val method by prefs.installMethod.collectAsStateWithLifecycle(InstallMethod.System)
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    val label = stringResource(if (busy) R.string.update_all_busy else R.string.update_all)
    TextButton(
        onClick = {
            if (busy) return@TextButton
            busy = true
            scope.launch {
                withContext(Dispatchers.IO) {
                    val artifacts = UpdateAll.artifacts(apps)
                    Log.i("DevPulse", "update all start ${artifacts.size}")
                    val result = UpdateAll.run(
                        artifacts = artifacts,
                        cacheDir = File(context.cacheDir, "updates"),
                        fetch = ApkHttpFetcher,
                        install = { file -> ApkInstall.apply(context, file, method) },
                        inspect = { file -> ApkArchiveIdentity.inspect(context.packageManager, file) },
                        installedOf = { pkg ->
                            ApkArchiveIdentity.installed(context.packageManager, pkg)
                                ?: InstalledIdentity(pkg, emptySet())
                        },
                    )
                    Log.i(
                        "DevPulse",
                        "update all done downloaded=${result.downloaded} installed=${result.installed} failDl=${result.failedDownload} failIns=${result.failedInstall}",
                    )
                }
                busy = false
            }
        },
        enabled = !busy,
        modifier = modifier.semantics { contentDescription = label },
    ) { Text(label) }
}
