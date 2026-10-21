package dev.foss.goldenpath.ui.settings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.InventoryPreferences
import dev.foss.goldenpath.inventory.RefreshWifiPrefs
import dev.foss.goldenpath.inventory.ScanInterval
import dev.foss.goldenpath.inventory.ScanNextCopy
import dev.foss.goldenpath.inventory.ScanSchedule
import dev.foss.goldenpath.ui.theme.SpacingMd
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScanIntervalSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { InventoryPreferences(context) }
    val scope = rememberCoroutineScope()
    val wifiPrefs = remember { RefreshWifiPrefs(context) }
    val interval by prefs.scanInterval.collectAsStateWithLifecycle(ScanInterval.OnDemand)
    val lastScanAt by prefs.lastScanAtMs.collectAsStateWithLifecycle(null)
    val wifiOnly by wifiPrefs.enabled.collectAsStateWithLifecycle(false)
    val nowMs = System.currentTimeMillis()
    val nextKind = remember(interval, lastScanAt, nowMs) {
        ScanNextCopy.kind(interval, lastScanAt, nowMs)
    }
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
                            ScanSchedule.apply(
                                context,
                                mode,
                                lastScanAtMs = lastScanAt,
                                replace = true,
                            )
                            if (mode != ScanInterval.OnDemand) {
                                requestScanNotifications(context)
                            }
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(
                                when (mode) {
                                    ScanInterval.OnDemand -> R.string.scan_interval_on_demand
                                    ScanInterval.Daily -> R.string.scan_interval_daily
                                    ScanInterval.Weekly -> R.string.scan_interval_weekly
                                    ScanInterval.Monthly -> R.string.scan_interval_monthly
                                },
                            ),
                        )
                    },
                )
            }
        }
        when (val kind = nextKind) {
            ScanNextCopy.Kind.Hidden -> Unit
            ScanNextCopy.Kind.Soon -> Text(
                text = stringResource(R.string.scan_interval_next_soon),
                style = MaterialTheme.typography.bodySmall,
            )
            is ScanNextCopy.Kind.Hours -> Text(
                text = stringResource(R.string.scan_interval_next_hours, kind.hours),
                style = MaterialTheme.typography.bodySmall,
            )
            is ScanNextCopy.Kind.Days -> Text(
                text = stringResource(R.string.scan_interval_next_days, kind.days),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        PreferenceSwitch(
            label = stringResource(R.string.update_prefetch_enable),
            checked = wifiOnly,
            onCheckedChange = { value -> scope.launch { wifiPrefs.setEnabled(value) } },
        )
    }
}

private fun requestScanNotifications(context: Context) {
    if (Build.VERSION.SDK_INT < 33) return
    val granted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
    if (granted) return
    val activity = context as? Activity ?: return
    activity.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 72)
}
