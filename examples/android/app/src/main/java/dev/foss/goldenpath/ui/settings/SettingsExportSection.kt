package dev.foss.goldenpath.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.CacheWipe
import dev.foss.goldenpath.inventory.IgnoreBackup
import dev.foss.goldenpath.inventory.IgnoredUpdates
import dev.foss.goldenpath.inventory.InventoryExportFormat
import dev.foss.goldenpath.inventory.SettingsPersistence
import dev.foss.goldenpath.ui.theme.SpacingMd
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsExportSection(
    onExport: (InventoryExportFormat) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = stringResource(R.string.inventory_export))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            OutlinedButton(onClick = { onExport(InventoryExportFormat.Html) }) {
                Text(stringResource(R.string.inventory_export_html))
            }
            OutlinedButton(onClick = { onExport(InventoryExportFormat.Csv) }) {
                Text(stringResource(R.string.inventory_export_csv))
            }
            OutlinedButton(onClick = { onExport(InventoryExportFormat.Xml) }) {
                Text(stringResource(R.string.inventory_export_xml))
            }
        }
        TextButton(onClick = { IgnoredUpdates.clearPersisted(context.filesDir) }) {
            Text(stringResource(R.string.history_retry_ignored))
        }
        TextButton(
            onClick = {
                val raw = IgnoreBackup.export(context.filesDir)
                File(context.filesDir, "settings_backup.txt").writeText(raw)
                scope.launch { SettingsPersistence.backup(context) }
            },
        ) { Text(stringResource(R.string.export_title)) }
        TextButton(onClick = { CacheWipe.remotes(context.filesDir) }) {
            Text(stringResource(R.string.about_update_restarting))
        }
    }
}
