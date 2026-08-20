package dev.foss.goldenpath.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.InventoryExportFormat
import dev.foss.goldenpath.ui.theme.SpacingMd

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsExportSection(
    onExport: (InventoryExportFormat) -> Unit,
    modifier: Modifier = Modifier,
) {
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
    }
}
