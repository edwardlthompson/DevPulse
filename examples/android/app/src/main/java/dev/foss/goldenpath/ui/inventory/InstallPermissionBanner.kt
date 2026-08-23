package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.LifecycleResumeEffect
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.WelcomeNeeds

@Composable
fun InstallPermissionBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(WelcomeNeeds.installGranted(context)) }
    LifecycleResumeEffect(Unit) {
        granted = WelcomeNeeds.installGranted(context)
        onPauseOrDispose { }
    }
    if (!WelcomeNeeds.showInstallBanner(granted)) return
    val label = stringResource(R.string.install_method_failed)
    Text(
        text = label,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
            .fillMaxWidth()
            .clickable { WelcomeNeeds.openInstallSettings(context) }
            .semantics { contentDescription = label },
    )
}
