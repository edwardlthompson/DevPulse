package dev.foss.goldenpath.ui.sources

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.ui.theme.SpacingMd

@Composable
fun SourcesScreen(
    urls: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(text = stringResource(R.string.sources_title), style = MaterialTheme.typography.titleMedium)
        Text(text = stringResource(R.string.sources_no_install))
        urls.forEach { url -> Text(text = url) }
    }
}
