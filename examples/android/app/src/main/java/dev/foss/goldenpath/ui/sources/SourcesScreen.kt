package dev.foss.goldenpath.ui.sources

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.StoreListingIntent
import dev.foss.goldenpath.ui.theme.SpacingMd

@Composable
fun SourcesScreen(
    urls: List<String>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier.padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(text = stringResource(R.string.sources_title), style = MaterialTheme.typography.titleMedium)
        Text(text = stringResource(R.string.sources_no_install))
        urls.forEach { url ->
            val cd = stringResource(R.string.inventory_open_listing, url)
            Text(
                text = url,
                modifier = Modifier
                    .semantics { contentDescription = cd }
                    .clickable(role = Role.Button) {
                        StoreListingIntent.open(context, url, RemoteReleasedSource.Fdroid)
                    },
            )
        }
    }
}
