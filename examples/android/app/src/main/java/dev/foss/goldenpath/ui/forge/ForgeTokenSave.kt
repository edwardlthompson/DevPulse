package dev.foss.goldenpath.ui.forge

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import dev.foss.goldenpath.R
import dev.foss.goldenpath.index.forge.ForgeTokenStore
import dev.foss.goldenpath.index.forge.GitHubTokenCheck
import dev.foss.goldenpath.index.forge.GitHubTokenClient
import dev.foss.goldenpath.index.forge.GitHubTokenHttp
import dev.foss.goldenpath.index.forge.GitHubTokenOutcome
import dev.foss.goldenpath.index.forge.GitHubTokenVerify
import dev.foss.goldenpath.ui.theme.SpacingSm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ForgeTokenSave(
    store: ForgeTokenStore,
    client: GitHubTokenClient = GitHubTokenHttp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var draft by remember { mutableStateOf("") }
    var present by remember { mutableStateOf(store.getToken() != null) }
    var busy by remember { mutableStateOf(false) }
    var check by remember { mutableStateOf<GitHubTokenCheck?>(null) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        Text(
            text = stringResource(if (present) R.string.forge_token_present else R.string.forge_token_absent),
            style = MaterialTheme.typography.bodySmall,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingSm),
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                enabled = !busy,
                label = { Text(stringResource(R.string.forge_token)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.weight(1f),
            )
            if (present) {
                val verified = stringResource(R.string.forge_token_present)
                Text(
                    text = "✅",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { contentDescription = verified },
                )
            }
        }
        Row {
            TextButton(
                onClick = {
                    busy = true
                    scope.launch {
                        val result = withContext(Dispatchers.IO) { GitHubTokenVerify.connect(draft, client, store) }
                        check = result
                        if (result.outcome == GitHubTokenOutcome.Accepted) {
                            draft = ""
                            present = true
                            val msg = result.hourlyLimit?.let {
                                context.getString(R.string.forge_token_saved, it)
                            } ?: context.getString(R.string.forge_token_saved_unknown)
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                        busy = false
                    }
                },
                enabled = !busy,
            ) { Text(stringResource(if (busy) R.string.forge_token_checking else R.string.forge_token_save)) }
            TextButton(
                onClick = {
                    check = GitHubTokenVerify.disconnect(store)
                    draft = ""
                    present = false
                },
                enabled = !busy && present,
            ) { Text(stringResource(R.string.forge_token_remove)) }
        }
        check?.let { Text(text = outcomeText(it), style = MaterialTheme.typography.bodySmall) }
        Text(text = stringResource(R.string.forge_token_never_log), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun outcomeText(check: GitHubTokenCheck): String = when (check.outcome) {
    GitHubTokenOutcome.Accepted -> check.hourlyLimit?.let {
        stringResource(R.string.forge_token_saved, it)
    } ?: stringResource(R.string.forge_token_saved_unknown)
    GitHubTokenOutcome.Rejected -> stringResource(R.string.forge_token_rejected)
    GitHubTokenOutcome.Unreachable -> stringResource(R.string.forge_token_network)
    GitHubTokenOutcome.Cleared -> stringResource(R.string.forge_token_cleared)
    GitHubTokenOutcome.Blank -> stringResource(R.string.forge_token_blank)
}
