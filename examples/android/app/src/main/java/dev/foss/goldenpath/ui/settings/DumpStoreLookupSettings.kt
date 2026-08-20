package dev.foss.goldenpath.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.InventoryPreferences
import dev.foss.goldenpath.ui.theme.SpacingMd
import kotlinx.coroutines.launch

@Composable
fun DumpStoreLookupSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { InventoryPreferences(context) }
    val scope = rememberCoroutineScope()
    val mirror by prefs.apkMirrorLookupEnabled.collectAsStateWithLifecycle(false)
    val pure by prefs.apkPureLookupEnabled.collectAsStateWithLifecycle(false)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = stringResource(R.string.dump_store_title), style = MaterialTheme.typography.titleMedium)
        Text(text = stringResource(R.string.dump_store_body), style = MaterialTheme.typography.bodySmall)
        PreferenceSwitch(
            label = stringResource(R.string.apkmirror_enable),
            checked = mirror,
            onCheckedChange = { value -> scope.launch { prefs.setApkMirrorLookupEnabled(value) } },
        )
        PreferenceSwitch(
            label = stringResource(R.string.apkpure_enable),
            checked = pure,
            onCheckedChange = { value -> scope.launch { prefs.setApkPureLookupEnabled(value) } },
        )
    }
}
