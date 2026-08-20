package dev.foss.goldenpath.ui.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.index.play.PlayLookup
import dev.foss.goldenpath.index.play.PlayLookupStatus
import dev.foss.goldenpath.scan.ScanDetail
import dev.foss.goldenpath.ui.detail.PlayStatusRow
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.refresh.highRefreshScroll
import dev.foss.goldenpath.ui.theme.SpacingMd
import java.text.DateFormat
import java.util.Date

@Composable
fun ScanDetailScreen(
    detail: ScanDetail,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val app = detail.item.app
    val installed = app.installedAtMs?.let { DateFormat.getDateInstance().format(Date(it)) }
    val badgeCd = badgeContentDescription(detail.item.staleness.badge)
    var notes by remember(app.packageName) { mutableStateOf(detail.notes) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .highRefreshScroll()
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(text = stringResource(R.string.scan_detail_title), style = MaterialTheme.typography.headlineSmall)
        Text(text = app.label, style = MaterialTheme.typography.titleLarge)
        Text(text = app.packageName)
        Text(
            text = if (installed == null) {
                stringResource(R.string.inventory_updated_unknown)
            } else {
                stringResource(R.string.inventory_updated, installed)
            },
        )
        Text(text = stringResource(R.string.staleness_installed))
        Text(text = badgeCd, modifier = Modifier.semantics { contentDescription = badgeCd })
        Text(text = stringResource(R.string.scan_detail_remotes))
        PlayStatusRow(lookup = PlayLookup(null, null, PlayLookupStatus.UnknownCheckManually))
        if (detail.item.staleness.compatibilityWarning) {
            Text(text = stringResource(R.string.scan_compat_warning))
        }
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.scan_detail_notes)) },
        )
        TextButton(onClick = onBack, modifier = Modifier.bottomInsetPadding()) {
            Text(stringResource(R.string.scan_close))
        }
    }
}
