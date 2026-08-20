package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.UpdateInventory
import dev.foss.goldenpath.ui.theme.SpacingMd
import dev.foss.goldenpath.ui.theme.SpacingSm
import java.text.DateFormat
import java.util.Date

@Composable
fun InventoryRow(
    app: InstalledApp,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateText = listReleaseDate(app)
    val hasUpdate = UpdateInventory.hasUpdate(app)
    val rowCd = buildString {
        append(stringResource(R.string.inventory_row_cd_short, app.label, dateText))
        if (hasUpdate) {
            append(". ")
            append(stringResource(R.string.inventory_update_available))
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onOpen)
            .semantics { contentDescription = rowCd }
            .padding(vertical = SpacingSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(packageName = app.packageName, label = app.label)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = SpacingMd),
        ) {
            Text(text = app.label, style = MaterialTheme.typography.titleMedium)
            Text(text = dateText, style = MaterialTheme.typography.bodySmall)
        }
        if (hasUpdate) {
            OneClickUpdateIcon(app)
        }
    }
}

@Composable
private fun listReleaseDate(app: InstalledApp): String {
    val ms = app.remoteReleasedAtMs
    if (ms == null || app.remoteReleasedSource == RemoteReleasedSource.None) {
        return stringResource(R.string.inventory_last_release_unknown)
    }
    return DateFormat.getDateInstance().format(Date(ms))
}
