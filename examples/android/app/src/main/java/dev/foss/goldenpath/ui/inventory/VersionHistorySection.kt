package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.AppRollback
import dev.foss.goldenpath.inventory.AppVersionHistory
import dev.foss.goldenpath.inventory.AppVersionItem
import dev.foss.goldenpath.inventory.AppVersionState
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.inventory.InventoryCopy
import dev.foss.goldenpath.inventory.RemoteReleaseMemory
import dev.foss.goldenpath.inventory.RemoteReleasedSource
import dev.foss.goldenpath.inventory.RollbackResult
import dev.foss.goldenpath.ui.theme.SpacingMd
import dev.foss.goldenpath.ui.theme.SpacingSm
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun VersionHistorySection(
    app: InstalledApp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val revision by RemoteReleaseMemory.revision.collectAsStateWithLifecycle()
    var versions by remember(app.packageName) { mutableStateOf<List<AppVersionItem>>(emptyList()) }
    var busyVersion by remember(app.packageName) { mutableStateOf<String?>(null) }
    var errorMessage by remember(app.packageName) { mutableStateOf<String?>(null) }

    LaunchedEffect(app.packageName, app.versionName, revision) {
        withContext(Dispatchers.IO) {
            val list = AppVersionHistory.query(context, app)
            versions = list
        }
    }

    if (versions.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingSm),
    ) {
        Text(
            text = stringResource(R.string.version_history_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.version_history_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        errorMessage?.let { msg ->
            Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
            versions.forEach { item ->
                VersionHistoryRow(
                    item = item,
                    isBusy = busyVersion == item.versionName,
                    onRollback = {
                        if (busyVersion != null) return@VersionHistoryRow
                        busyVersion = item.versionName
                        errorMessage = null
                        scope.launch {
                            val res = withContext(Dispatchers.IO) {
                                AppRollback.rollback(context, app, item)
                            }
                            busyVersion = null
                            if (res is RollbackResult.Failed) {
                                errorMessage = res.reason
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun VersionHistoryRow(
    item: AppVersionItem,
    isBusy: Boolean,
    onRollback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SpacingSm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.versionName,
                style = MaterialTheme.typography.bodyLarge,
                color = if (item.state == AppVersionState.Current) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            val srcText = if (item.source != RemoteReleasedSource.None) {
                stringResource(InventoryCopy.sourceRes(item.source))
            } else ""
            val dateText = item.releasedAtMs?.let { DateFormat.getDateInstance().format(Date(it)) }.orEmpty()
            val subtitle = listOf(srcText, dateText).filter { it.isNotBlank() }.joinToString(" · ")
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        when {
            isBusy -> {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
            item.state == AppVersionState.Current -> {
                SuggestionChip(
                    onClick = { },
                    label = { Text(stringResource(R.string.version_history_current)) },
                )
            }
            item.state == AppVersionState.Newer -> {
                Text(
                    text = stringResource(R.string.version_history_newer),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            item.state == AppVersionState.Rollback -> {
                if (item.downloadUrl.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.version_history_no_link),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    OutlinedButton(onClick = onRollback) {
                        Text(stringResource(R.string.version_history_rollback))
                    }
                }
            }
        }
    }
}
