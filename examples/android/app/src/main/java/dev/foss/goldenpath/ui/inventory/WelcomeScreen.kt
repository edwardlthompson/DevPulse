package dev.foss.goldenpath.ui.inventory

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.UsageStatsAccess
import dev.foss.goldenpath.inventory.WelcomeNeed
import dev.foss.goldenpath.inventory.WelcomeNeeds
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.refresh.highRefreshScroll
import dev.foss.goldenpath.ui.theme.SpacingMd

@Composable
fun WelcomeScreen(
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val grants = rememberPermissionGrants()
    val rows = WelcomeNeeds.rows(grants.apps, grants.notify, grants.install, grants.usage)
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .highRefreshScroll()
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(
            text = stringResource(R.string.inventory_rationale_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(text = stringResource(R.string.inventory_rationale_body))
        PermissionRows(rows = rows, retap = false, onClick = { grants.onNeed(it) })
        Button(
            onClick = onContinue,
            enabled = WelcomeNeeds.ready(rows),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.inventory_rationale_ack))
        }
        TextButton(onClick = onSkip, modifier = Modifier.bottomInsetPadding()) {
            Text(stringResource(R.string.inventory_rationale_skip))
        }
    }
}

@Composable
fun rememberPermissionGrants(appsStart: Boolean = false): PermissionGrants {
    val context = LocalContext.current
    var apps by remember { mutableStateOf(appsStart) }
    var notify by remember { mutableStateOf(WelcomeNeeds.notifyGranted(context)) }
    var install by remember { mutableStateOf(WelcomeNeeds.installGranted(context)) }
    var usage by remember { mutableStateOf(UsageStatsAccess.isGranted(context)) }
    val askNotify = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notify = granted || WelcomeNeeds.notifyGranted(context)
    }
    LifecycleResumeEffect(Unit) {
        notify = WelcomeNeeds.notifyGranted(context)
        install = WelcomeNeeds.installGranted(context)
        usage = UsageStatsAccess.isGranted(context)
        onPauseOrDispose { }
    }
    return PermissionGrants(apps, notify, install, usage) { need ->
        when (need) {
            WelcomeNeed.Apps -> apps = true
            WelcomeNeed.Notifications -> if (Build.VERSION.SDK_INT >= 33) {
                askNotify.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            WelcomeNeed.Install -> WelcomeNeeds.openInstallSettings(context)
            WelcomeNeed.Usage -> UsageStatsAccess.openSettings(context)
        }
    }
}

class PermissionGrants(
    val apps: Boolean,
    val notify: Boolean,
    val install: Boolean,
    val usage: Boolean,
    val onNeed: (WelcomeNeed) -> Unit,
)
