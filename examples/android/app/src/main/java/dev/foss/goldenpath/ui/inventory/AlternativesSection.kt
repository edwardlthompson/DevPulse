package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import dev.foss.goldenpath.query.PinPreferences
import dev.foss.goldenpath.ui.sources.SourcesScreen
import dev.foss.goldenpath.ui.theme.SpacingSm
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AlternativesSection(app: InstalledApp, inventory: List<InstalledApp>) {
    val context = LocalContext.current
    val pinPrefs = remember { PinPreferences(context) }
    val scope = rememberCoroutineScope()
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
                        .combinedClickable(
                            role = Role.Button,
                            onClick = {
                                StoreListingIntent.open(context, hit.sourceUrl, RemoteReleasedSource.Fdroid, hit.packageName)
                            },
                            onLongClick = { scope.launch { pinPrefs.setPinned(hit.packageName, true) } },
                        ),
                )
            }
        }
        SourcesScreen(urls = urls)
    }
}
