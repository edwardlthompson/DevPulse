package dev.foss.goldenpath.ui.forge

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.GithubAddLive
import dev.foss.goldenpath.inventory.GithubAddResult
import dev.foss.goldenpath.inventory.InstalledApp

@Composable
fun AddRepoDialog(
    installed: List<InstalledApp>,
    onDismiss: () -> Unit,
    onMessage: (String) -> Unit,
) {
    val context = LocalContext.current
    var url by remember { mutableStateOf("") }
    var pickRepo by remember { mutableStateOf<String?>(null) }
    val bound = stringResource(R.string.forge_add_bound, 1)
    val watched = stringResource(R.string.forge_add_watched)
    val invalid = stringResource(R.string.forge_add_invalid)
    if (pickRepo != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.forge_add_pick)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    installed.forEach { app ->
                        TextButton(onClick = {
                            val ok = GithubAddLive.pick(context, app.packageName, pickRepo.orEmpty(), installed)
                            onMessage(if (ok) bound else invalid)
                            onDismiss()
                        }) {
                            Text(stringResource(R.string.forge_add_app, app.label, app.packageName))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.inventory_detail_back)) }
            },
        )
        return
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.forge_add)) },
        text = {
            val urlLabel = stringResource(R.string.forge_paste_url)
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = urlLabel },
                label = { Text(urlLabel) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = {
                when (val result = GithubAddLive.bind(context, url, installed)) {
                    GithubAddResult.Invalid -> onMessage(invalid)
                    is GithubAddResult.Bound -> {
                        onMessage(context.getString(R.string.forge_add_bound, result.matches.size))
                        onDismiss()
                    }
                    is GithubAddResult.Pick -> pickRepo = result.ownerRepo
                    is GithubAddResult.Watched -> {
                        onMessage(watched)
                        onDismiss()
                    }
                }
            }) { Text(stringResource(R.string.forge_add_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.inventory_detail_back)) }
        },
    )
}
