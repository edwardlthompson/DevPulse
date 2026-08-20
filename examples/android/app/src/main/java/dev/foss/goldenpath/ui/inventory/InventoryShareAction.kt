package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.InventoryExportFormat

@Composable
fun InventoryShareAction(onExport: (InventoryExportFormat) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = stringResource(R.string.inventory_export),
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            formatItem(R.string.inventory_export_html, InventoryExportFormat.Html, onExport) { open = false }
            formatItem(R.string.inventory_export_csv, InventoryExportFormat.Csv, onExport) { open = false }
            formatItem(R.string.inventory_export_xml, InventoryExportFormat.Xml, onExport) { open = false }
        }
    }
}

@Composable
private fun formatItem(
    labelRes: Int,
    format: InventoryExportFormat,
    onExport: (InventoryExportFormat) -> Unit,
    dismiss: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(stringResource(labelRes)) },
        onClick = {
            dismiss()
            onExport(format)
        },
    )
}
