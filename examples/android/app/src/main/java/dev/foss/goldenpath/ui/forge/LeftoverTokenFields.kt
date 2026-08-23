package dev.foss.goldenpath.ui.forge

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import dev.foss.goldenpath.R
import dev.foss.goldenpath.index.forge.EncryptedForgeTokenStore
import dev.foss.goldenpath.index.forge.ForgeHost
import dev.foss.goldenpath.index.forge.ForgeRateLimit

@Composable
fun LeftoverTokenFields(store: EncryptedForgeTokenStore?) {
    if (store == null) return
    Column {
        Text(
            text = stringResource(
                R.string.scan_progress,
                ForgeRateLimit.leftoverLeft(),
                ForgeRateLimit.LEFTOVER_SKIP_AFTER,
            ),
            style = MaterialTheme.typography.bodySmall,
        )
        HostTokenRow(ForgeHost.GitLab, store)
        HostTokenRow(ForgeHost.Codeberg, store)
    }
}

@Composable
private fun HostTokenRow(host: ForgeHost, store: EncryptedForgeTokenStore) {
    var draft by remember(host) { mutableStateOf("") }
    OutlinedTextField(
        value = draft,
        onValueChange = { draft = it },
        label = { Text(host.name) },
        visualTransformation = PasswordVisualTransformation(),
    )
    TextButton(onClick = { store.setLeftover(host, draft); draft = "" }) {
        Text(stringResource(R.string.forge_token_save))
    }
}
