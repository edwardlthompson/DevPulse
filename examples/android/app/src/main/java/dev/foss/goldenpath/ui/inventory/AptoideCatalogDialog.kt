package dev.foss.goldenpath.ui.inventory

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.index.aptoide.AptoideCatalog

@Composable
internal fun AptoideCatalogDialog(onPicked: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.aptoide_title)) },
        text = { Text(stringResource(R.string.aptoide_body)) },
        confirmButton = {
            TextButton(
                onClick = {
                    AptoideCatalog.pick(games = false)
                    onPicked()
                },
            ) { Text(stringResource(R.string.inventory_source_aptoide)) }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    AptoideCatalog.pick(games = true)
                    onPicked()
                },
            ) { Text(stringResource(R.string.about_update_no_compatible)) }
        },
    )
}
