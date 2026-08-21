package dev.foss.goldenpath.ui.opportunity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.about.ReleaseTagFetcher
import dev.foss.goldenpath.index.fdroid.FileFdroidCategoryStore
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.UsageStatsAccess
import dev.foss.goldenpath.inventory.UsageStatsManagerCatalog
import dev.foss.goldenpath.opportunity.CategoryGap
import dev.foss.goldenpath.opportunity.FileDevelopNextStore
import dev.foss.goldenpath.opportunity.OpportunityExport
import dev.foss.goldenpath.opportunity.OpportunityFromApps
import dev.foss.goldenpath.opportunity.OpportunityRanker
import dev.foss.goldenpath.opportunity.SelfPulseConfig
import dev.foss.goldenpath.query.PinPreferences
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.theme.SpacingMd
import java.io.File
import java.text.DateFormat
import java.util.Date

@Composable
fun OpportunityScreen(
    gaps: List<CategoryGap>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(text = stringResource(R.string.opportunity_title), style = MaterialTheme.typography.titleMedium)
        if (gaps.isEmpty()) {
            Text(text = stringResource(R.string.opportunity_empty))
        } else {
            gaps.forEach { gap ->
                Text(text = stringResource(R.string.opportunity_gap, gap.category, gap.quietCount))
            }
        }
    }
}

@Composable
fun OpportunityPane(
    apps: List<InstalledApp>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val pins by remember { PinPreferences(context) }.pins.collectAsStateWithLifecycle(emptySet())
    val categories = remember {
        FileFdroidCategoryStore(File(context.filesDir, "fdroid_categories.tsv")).load()
            .mapValues { it.value.category.orEmpty() }
            .filterValues { it.isNotEmpty() }
    }
    val quiet = OpportunityFromApps.quietApps(apps, System.currentTimeMillis(), includePinned = false, pins)
    val gaps = OpportunityFromApps.gaps(apps, System.currentTimeMillis(), includePinned = false, pins, categories)
    val notes = remember { FileDevelopNextStore(File(context.filesDir, "develop_next.tsv")) }
    var noteByPkg by remember { mutableStateOf(notes.load()) }
    val usage = remember {
        if (UsageStatsAccess.isGranted(context)) {
            val end = System.currentTimeMillis()
            UsageStatsManagerCatalog(context.getSystemService(android.app.usage.UsageStatsManager::class.java))
                .usageSince(end - 30L * 86_400_000L, end)
        } else {
            emptyList()
        }
    }
    val titles = OpportunityExport.quietTitles(quiet, usage)
    val repo = remember { ReleaseTagFetcher.loadReleaseRepo(context) }
    var pulse by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(repo) {
        val config = repo?.let { SelfPulseConfig("app.devpulse", it) }
        if (config != null && OpportunityRanker.selfPulseMatches(config, config.packageName)) {
            val latest = ReleaseTagFetcher.fetchLatestRelease(config.repo)
            pulse = latest?.publishedAtMs?.let { DateFormat.getDateInstance().format(Date(it)) }
                ?: latest?.tag
        }
    }
    Column(modifier = modifier.padding(SpacingMd), verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        TextButton(onClick = onBack) { Text(stringResource(R.string.opportunity_close)) }
        OpportunityScreen(gaps = gaps)
        pulse?.let { Text(text = stringResource(R.string.opportunity_self, it)) }
        TextButton(onClick = { OpportunityShare.send(context, titles, gaps, json = false) }) {
            Text(stringResource(R.string.opportunity_export))
        }
        quiet.take(8).forEach { app ->
            OutlinedTextField(
                value = noteByPkg[app.packageName].orEmpty(),
                onValueChange = { value ->
                    noteByPkg = noteByPkg + (app.packageName to value)
                    notes.put(app.packageName, value)
                },
                label = { Text(stringResource(R.string.opportunity_note, app.label)) },
            )
        }
        TextButton(onClick = onBack, modifier = Modifier.bottomInsetPadding()) {
            Text(stringResource(R.string.opportunity_close))
        }
    }
}
