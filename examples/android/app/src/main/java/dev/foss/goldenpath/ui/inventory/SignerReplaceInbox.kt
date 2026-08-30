package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.IgnoredUpdates
import dev.foss.goldenpath.inventory.InventoryCopy
import dev.foss.goldenpath.inventory.SignerReplaceHold
import dev.foss.goldenpath.inventory.SignerReplaceQueue
import dev.foss.goldenpath.inventory.SignerReplaceStore
import dev.foss.goldenpath.ui.theme.SpacingMd
import dev.foss.goldenpath.ui.theme.SpacingSm

@Composable
fun SignerReplaceInbox() {
    val context = LocalContext.current
    val rev by SignerReplaceQueue.revision.collectAsStateWithLifecycle(0)
    val holds = remember(rev) { SignerReplaceQueue.rows }
    if (holds.isEmpty()) return
    var show by remember { mutableStateOf(false) }
    TextButton(onClick = { show = true }) {
        Text(text = stringResource(R.string.signer_replace_list, holds.size))
    }
    if (show) {
        AlertDialog(
            onDismissRequest = { show = false },
            title = { Text(stringResource(R.string.signer_replace_list, holds.size)) },
            text = { SigningIssueBlock(holds = holds, onReplace = { hold ->
                IgnoredUpdates.drop(hold.packageName, context.filesDir)
                SignerReplaceStore.save(context.filesDir, hold)
                show = false
            }) },
            confirmButton = {
                TextButton(onClick = { show = false }) {
                    Text(text = stringResource(R.string.about_close))
                }
            },
        )
    }
}

@Composable
fun SigningIssueBlock(
    holds: List<SignerReplaceHold>,
    onReplace: (SignerReplaceHold) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (holds.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.signer_replace_list_body),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = SpacingSm),
        )
        holds.forEach { hold ->
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = SpacingMd)) {
                Text(text = hold.label, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = stringResource(InventoryCopy.sourceRes(hold.source)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = { onReplace(hold) }) {
                    Text(text = stringResource(R.string.signer_replace_confirm))
                }
            }
        }
    }
}
