package dev.foss.goldenpath.ui.forge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.index.forge.GitHubTokenGuide
import dev.foss.goldenpath.index.forge.GitHubTokenStep
import dev.foss.goldenpath.ui.theme.SpacingSm

@Composable
fun ForgeTokenGuide(modifier: Modifier = Modifier) {
    val uri = LocalUriHandler.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        Text(
            text = stringResource(R.string.forge_token_guide_title),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.semantics { heading() },
        )
        Text(text = stringResource(R.string.forge_token_why), style = MaterialTheme.typography.bodySmall)
        GitHubTokenGuide.steps.forEach { step ->
            TokenStep(step) { url -> uri.openUri(url) }
        }
        Text(text = stringResource(R.string.forge_token_classic_note), style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = { uri.openUri(GitHubTokenGuide.CLASSIC_URL) }) {
            Text(stringResource(R.string.forge_token_open_classic))
        }
    }
}

@Composable
private fun TokenStep(step: GitHubTokenStep, onOpen: (String) -> Unit) {
    Column {
        Text(text = stringResource(step.textRes), style = MaterialTheme.typography.bodySmall)
        val url = step.url ?: return
        TextButton(onClick = { onOpen(url) }) {
            Text(stringResource(R.string.forge_token_open_step))
        }
    }
}
