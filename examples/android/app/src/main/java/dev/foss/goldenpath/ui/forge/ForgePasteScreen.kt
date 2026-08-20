package dev.foss.goldenpath.ui.forge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.theme.SpacingMd

@Composable
fun ForgePasteScreen(
    packageName: String,
    repoUrl: String,
    token: String,
    onPackageNameChange: (String) -> Unit,
    onRepoUrlChange: (String) -> Unit,
    onTokenChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(text = stringResource(R.string.forge_title), style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = packageName,
            onValueChange = onPackageNameChange,
            label = { Text(stringResource(R.string.forge_package)) },
        )
        OutlinedTextField(
            value = repoUrl,
            onValueChange = onRepoUrlChange,
            label = { Text(stringResource(R.string.forge_paste_url)) },
        )
        OutlinedTextField(
            value = token,
            onValueChange = onTokenChange,
            label = { Text(stringResource(R.string.forge_token)) },
            visualTransformation = PasswordVisualTransformation(),
        )
        Text(text = stringResource(R.string.forge_token_never_log), style = MaterialTheme.typography.bodySmall)
    }
}
