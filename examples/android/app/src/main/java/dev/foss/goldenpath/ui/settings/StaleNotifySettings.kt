package dev.foss.goldenpath.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import dev.foss.goldenpath.notify.StaleNotifyPrefs
import dev.foss.goldenpath.notify.StaleNotifyWorker
import dev.foss.goldenpath.ui.theme.SpacingMd
import kotlinx.coroutines.launch

@Composable
fun StaleNotifySettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { StaleNotifyPrefs(context) }
    val scope = rememberCoroutineScope()
    val enabled by prefs.enabled.collectAsStateWithLifecycle(false)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = stringResource(R.string.stale_notify_title), style = MaterialTheme.typography.titleMedium)
        Text(text = stringResource(R.string.stale_notify_body), style = MaterialTheme.typography.bodySmall)
        PreferenceSwitch(
            label = stringResource(R.string.stale_notify_enable),
            checked = enabled,
            onCheckedChange = { value ->
                scope.launch {
                    prefs.setEnabled(value)
                    StaleNotifyWorker.apply(context, value)
                }
            },
        )
    }
}
