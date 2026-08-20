package dev.foss.goldenpath.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
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
import dev.foss.goldenpath.inventory.InstallMethod
import dev.foss.goldenpath.inventory.InventoryPreferences
import dev.foss.goldenpath.ui.theme.SpacingMd
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InstallMethodSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { InventoryPreferences(context) }
    val scope = rememberCoroutineScope()
    val method by prefs.installMethod.collectAsStateWithLifecycle(InstallMethod.System)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = stringResource(R.string.install_method_title), style = MaterialTheme.typography.titleMedium)
        Text(text = stringResource(R.string.install_method_body), style = MaterialTheme.typography.bodySmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            InstallMethod.entries.forEach { option ->
                FilterChip(
                    selected = method == option,
                    onClick = { scope.launch { prefs.setInstallMethod(option) } },
                    label = { Text(stringResource(labelRes(option))) },
                )
            }
        }
    }
}

private fun labelRes(method: InstallMethod): Int = when (method) {
    InstallMethod.System -> R.string.install_method_system
    InstallMethod.Session -> R.string.install_method_session
    InstallMethod.Root -> R.string.install_method_root
}
