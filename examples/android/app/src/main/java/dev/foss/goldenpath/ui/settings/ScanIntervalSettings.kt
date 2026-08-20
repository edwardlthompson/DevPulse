package dev.foss.goldenpath.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.InventoryPreferences
import dev.foss.goldenpath.inventory.ScanInterval
import dev.foss.goldenpath.inventory.ScanSchedule
import dev.foss.goldenpath.ui.theme.SpacingMd
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScanIntervalSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { InventoryPreferences(context) }
    val scope = rememberCoroutineScope()
    val interval by prefs.scanInterval.collectAsStateWithLifecycle(ScanInterval.OnDemand)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = stringResource(R.string.scan_interval_title), style = MaterialTheme.typography.titleMedium)
        Text(text = stringResource(R.string.scan_interval_body), style = MaterialTheme.typography.bodySmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
            ScanInterval.entries.forEach { mode ->
                FilterChip(
                    selected = interval == mode,
                    onClick = {
                        scope.launch {
                            prefs.setScanInterval(mode)
                            ScanSchedule.apply(context, mode)
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(
                                when (mode) {
                                    ScanInterval.OnDemand -> R.string.scan_interval_on_demand
                                    ScanInterval.Weekly -> R.string.scan_interval_weekly
                                    ScanInterval.Monthly -> R.string.scan_interval_monthly
                                },
                            ),
                        )
                    },
                )
            }
        }
    }
}
