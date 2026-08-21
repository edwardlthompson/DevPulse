package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.alternatives.AlternativesFromCache
import dev.foss.goldenpath.alternatives.SourcesList
import dev.foss.goldenpath.index.fdroid.FileFdroidCategoryStore
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.StoreListingIntent
import dev.foss.goldenpath.ui.sources.SourcesScreen
import dev.foss.goldenpath.ui.theme.SpacingSm
import java.io.File

@Composable
fun AlternativesSection(app: InstalledApp, inventory: List<InstalledApp>) {
    val context = LocalContext.current
    val hits = remember(app.packageName, inventory) {
        runCatching {
            val meta = FileFdroidCategoryStore(File(context.filesDir, "fdroid_categories.tsv")).load()
            AlternativesFromCache.hits(app, inventory, meta, System.currentTimeMillis())
        }.getOrDefault(emptyList())
    }
    val urls = SourcesList.merge(app.latestListings.mapNotNull { it.url } + hits.map { it.sourceUrl })
    Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        Text(text = stringResource(R.string.alternatives_title), style = MaterialTheme.typography.titleMedium)
        if (hits.isEmpty()) {
            Text(text = stringResource(R.string.alternatives_empty))
        } else {
            hits.forEach { hit ->
                val cd = stringResource(R.string.alternatives_open, hit.title)
                Text(
                    text = hit.title,
                    modifier = Modifier
                        .semantics { contentDescription = cd }
                        .clickable(role = Role.Button) {
                            StoreListingIntent.open(context, hit.sourceUrl, RemoteReleasedSource.Fdroid, hit.packageName)
                        },
                )
            }
        }
        SourcesScreen(urls = urls)
    }
}
